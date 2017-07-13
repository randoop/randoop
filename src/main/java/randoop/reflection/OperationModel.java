package randoop.reflection;

import static randoop.main.GenInputsAbstract.ClassLiteralsMode;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import randoop.Globals;
import randoop.contract.CompareToAntiSymmetric;
import randoop.contract.CompareToEquals;
import randoop.contract.CompareToReflexive;
import randoop.contract.CompareToSubs;
import randoop.contract.CompareToTransitive;
import randoop.contract.EqualsHashcode;
import randoop.contract.EqualsReflexive;
import randoop.contract.EqualsReturnsNormally;
import randoop.contract.EqualsSymmetric;
import randoop.contract.EqualsToNullRetFalse;
import randoop.contract.EqualsTransitive;
import randoop.contract.ObjectContract;
import randoop.generation.ComponentManager;
import randoop.main.ClassNameErrorHandler;
import randoop.main.GenInputsAbstract;
import randoop.operation.MethodCall;
import randoop.operation.OperationParseException;
import randoop.operation.OperationParser;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.test.ContractSet;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.util.Log;
import randoop.util.MultiMap;

/**
 * {@code OperationModel} represents the information context from which tests are generated. The
 * model includes:
 *
 * <ul>
 *   <li>classes under test,
 *   <li>operations of all classes,
 *   <li>any atomic code sequences derived from command-line arguments, and
 *   <li>the contracts or oracles used to generate tests.
 * </ul>
 *
 * <p>This class manages all information about generic classes internally, and instantiates any type
 * variables in operations before returning them.
 */
public class OperationModel {

  /** The set of class declaration types for this model */
  private Set<ClassOrInterfaceType> classTypes;

  /** The count of classes under test for this model */
  private int classCount;

  /** The set of input types for this model */
  private Set<Type> inputTypes;

  /** The set of class objects used in the covered-class test filter */
  private final LinkedHashSet<Class<?>> coveredClasses;

  /** Map for singleton sequences of literals extracted from classes. */
  private MultiMap<ClassOrInterfaceType, Sequence> classLiteralMap;

  /** Set of singleton sequences for values from TestValue annotated fields. */
  private Set<Sequence> annotatedTestValues;

  /** Set of object contracts used to generate tests. */
  private ContractSet contracts;

  /** Set of concrete operations extracted from classes */
  private final Set<TypedOperation> operations;

  /** Create an empty model of test context. */
  private OperationModel() {
    // TreeSet here for deterministic coverage in the systemTest runNaiveCollectionsTest()
    classTypes = new TreeSet<>();
    inputTypes = new TreeSet<>();
    classLiteralMap = new MultiMap<>();
    annotatedTestValues = new LinkedHashSet<>();
    contracts = new ContractSet();
    contracts.add(EqualsReflexive.getInstance());
    contracts.add(EqualsSymmetric.getInstance());
    contracts.add(EqualsHashcode.getInstance());
    contracts.add(EqualsToNullRetFalse.getInstance());
    contracts.add(EqualsReturnsNormally.getInstance());
    contracts.add(EqualsTransitive.getInstance());
    contracts.add(CompareToReflexive.getInstance());
    contracts.add(CompareToAntiSymmetric.getInstance());
    contracts.add(CompareToEquals.getInstance());
    contracts.add(CompareToSubs.getInstance());
    contracts.add(CompareToTransitive.getInstance());

    coveredClasses = new LinkedHashSet<>();
    operations = new TreeSet<>();
    classCount = 0;
  }

  /**
   * Factory method to construct an operation model for a particular set of classes
   *
   * @param visibility the {@link randoop.reflection.VisibilityPredicate} to test accessibility of
   *     classes and class members
   * @param reflectionPredicate the reflection predicate to determine which classes and class
   *     members are used
   * @param classnames the names of classes under test
   * @param coveredClassnames the names of classes to be tested by covered class heuristic
   * @param methodSignatures the signatures of methods to be added to the model
   * @param errorHandler the handler for bad file name errors
   * @param literalsFileList the list of literals file names
   * @return the operation model for the parameters
   * @throws OperationParseException if a method signature is ill-formed
   * @throws NoSuchMethodException if an attempt is made to load a non-existent method
   */
  public static OperationModel createModel(
      VisibilityPredicate visibility,
      ReflectionPredicate reflectionPredicate,
      Set<String> classnames,
      Set<String> coveredClassnames,
      Set<String> methodSignatures,
      ClassNameErrorHandler errorHandler,
      List<String> literalsFileList)
      throws OperationParseException, NoSuchMethodException {

    OperationModel model = new OperationModel();

    model.addClassTypes(
        visibility,
        reflectionPredicate,
        classnames,
        coveredClassnames,
        errorHandler,
        literalsFileList);

    model.addOperations(model.classTypes, visibility, reflectionPredicate);
    model.addOperations(methodSignatures);
    model.addObjectConstructor();

    return model;
  }

  /**
   * Adds literals to the component manager, by parsing any literals files specified by the user.
   * Includes literals at different levels indicated by {@link ClassLiteralsMode}.
   *
   * @param compMgr the component manager
   * @param literalsFile the list of literals file names
   * @param literalsLevel the level of literals to add
   */
  public void addClassLiterals(
      ComponentManager compMgr, List<String> literalsFile, ClassLiteralsMode literalsLevel) {

    // Add a (1-element) sequence corresponding to each literal to the component
    // manager.

    for (String filename : literalsFile) {
      MultiMap<ClassOrInterfaceType, Sequence> literalmap;
      if (filename.equals("CLASSES")) {
        literalmap = classLiteralMap;
      } else {
        literalmap = LiteralFileReader.parse(filename);
      }

      for (ClassOrInterfaceType type : literalmap.keySet()) {
        Package pkg = (literalsLevel == ClassLiteralsMode.PACKAGE ? type.getPackage() : null);
        for (Sequence seq : literalmap.getValues(type)) {
          switch (literalsLevel) {
            case CLASS:
              compMgr.addClassLevelLiteral(type, seq);
              break;
            case PACKAGE:
              assert pkg != null;
              compMgr.addPackageLevelLiteral(pkg, seq);
              break;
            case ALL:
              compMgr.addGeneratedSequence(seq);
              break;
            default:
              throw new Error(
                  "Unexpected error in GenTests -- please report at https://github.com/randoop/randoop/issues");
          }
        }
      }
    }
  }

  /**
   * Gets observer methods from the set of signatures.
   *
   * @param observerSignatures the set of method signatures
   * @return the map to observer methods from their declaring class type
   * @throws OperationParseException if a method signature cannot be parsed
   */
  public MultiMap<Type, TypedOperation> getObservers(Set<String> observerSignatures)
      throws OperationParseException {
    // Populate observer_map from observers file.
    MultiMap<Type, TypedOperation> observerMap = new MultiMap<>();
    for (String sig : observerSignatures) {
      TypedClassOperation operation = MethodCall.parse(sig);
      Type outputType = operation.getOutputType();
      if (outputType.isPrimitive() || outputType.isString() || outputType.isEnum()) {
        observerMap.add(operation.getDeclaringType(), operation);
      }
    }
    return observerMap;
  }

  /**
   * Returns the set of types for classes under test.
   *
   * @return the set of class types
   */
  public Set<ClassOrInterfaceType> getClassTypes() {
    return classTypes;
  }

  /**
   * Returns the set of identified {@code Class<?>} objects for the covered class heuristic.
   *
   * @return the set of covered classes
   */
  public Set<Class<?>> getCoveredClasses() {
    return coveredClasses;
  }

  /**
   * Returns the set of input types that occur as parameters in classes under test.
   *
   * @see TypeExtractor
   * @return the set of input types that occur in classes under test
   */
  public Set<Type> getInputTypes() {
    //TODO this is not used, should it be? or should it even be here?
    return inputTypes;
  }

  /**
   * Indicate whether the model has class types.
   *
   * @return true if the model has class types, and false if the class type set is empty
   */
  public boolean hasClasses() {
    return classCount > 0;
  }

  public List<TypedOperation> getOperations() {
    return new ArrayList<>(operations);
  }

  /**
   * Returns all {@link ObjectContract} objects for this run of Randoop. Includes Randoop defaults
   * and {@link randoop.CheckRep} annotated methods.
   *
   * @return the list of contracts
   */
  public ContractSet getContracts() {
    return contracts;
  }

  public Set<Sequence> getAnnotatedTestValues() {
    return annotatedTestValues;
  }

  public void log() {
    if (!Log.isLoggingOn()) {
      return;
    }

    try {
      GenInputsAbstract.log.write("Operations: " + Globals.lineSep);
      for (TypedOperation t : this.operations) {
        GenInputsAbstract.log.write(t.toString());
        GenInputsAbstract.log.write(Globals.lineSep);
        GenInputsAbstract.log.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Gathers class types to be used in a run of Randoop and adds them to this {@code
   * OperationModel}. Specifically, collects types for classes-under-test, objects for covered-class
   * heuristic, concrete input types, annotated test values, and literal values. Also collects
   * annotated test values, and class literal values used in test generation.
   *
   * @param visibility the visibility predicate
   * @param reflectionPredicate the predicate to determine which reflection objects are used
   * @param classnames the names of classes-under-test
   * @param coveredClassnames the names of classes used in covered-class heuristic
   * @param errorHandler the handler for bad class names
   * @param literalsFileList the list of literals file names
   */
  private void addClassTypes(
      VisibilityPredicate visibility,
      ReflectionPredicate reflectionPredicate,
      Set<String> classnames,
      Set<String> coveredClassnames,
      ClassNameErrorHandler errorHandler,
      List<String> literalsFileList) {
    ReflectionManager mgr = new ReflectionManager(visibility);
    mgr.add(new DeclarationExtractor(this.classTypes, reflectionPredicate));
    mgr.add(new TypeExtractor(this.inputTypes, visibility));
    mgr.add(new TestValueExtractor(this.annotatedTestValues));
    mgr.add(new CheckRepExtractor(this.contracts));
    if (literalsFileList.contains("CLASSES")) {
      mgr.add(new ClassLiteralExtractor(this.classLiteralMap));
    }

    // Collect classes under test
    Set<Class<?>> visitedClasses = new LinkedHashSet<>();
    for (String classname : classnames) {
      Class<?> c = null;
      try {
        c = TypeNames.getTypeForName(classname);
      } catch (ClassNotFoundException e) {
        errorHandler.handle(classname);
      } catch (Throwable e) {
        errorHandler.handle(classname, e.getCause());
      }
      // Note that c could be null if errorHandler just warns on bad names
      if (c != null && !visitedClasses.contains(c)) {
        visitedClasses.add(c);

        // ignore interfaces and non-visible classes
        if (!visibility.isVisible(c)) {
          System.out.println(
              "Ignoring non-visible " + c + " specified via --classlist or --testclass.");
        } else if (c.isInterface()) {
          System.out.println("Ignoring " + c + " specified via --classlist or --testclass.");
        } else {
          if (Modifier.isAbstract(c.getModifiers()) && !c.isEnum()) {
            System.out.println(
                "Ignoring abstract " + c + " specified via --classlist or --testclass.");
          } else {
            mgr.apply(c);
          }
          if (coveredClassnames.contains(classname)) {
            coveredClasses.add(c);
          }
        }
      }
    }
    classCount = this.classTypes.size();

    // Collect covered classes
    for (String classname : coveredClassnames) {
      if (!classnames.contains(classname)) {
        Class<?> c = null;
        try {
          c = TypeNames.getTypeForName(classname);
        } catch (ClassNotFoundException e) {
          errorHandler.handle(classname);
        } catch (Throwable e) {
          errorHandler.handle(classname, e.getCause());
        }
        if (c != null) {
          if (!visibility.isVisible(c)) {
            System.out.println(
                "Ignoring non-visible " + c + " specified as --require-covered-classes target");
          } else if (c.isInterface()) {
            System.out.println("Ignoring " + c + " specified as --require-covered-classes target.");
          } else {
            coveredClasses.add(c);
          }
        }
      }
    }
  }

  /**
   * Iterates through a set of simple and instantiated class types and uses reflection to extract
   * the operations that satisfy both the visibility and reflection predicates, and then adds them
   * to the operation set of this model.
   *
   * @param concreteClassTypes the declaring class types for the operations
   * @param visibility the visibility predicate
   * @param reflectionPredicate the reflection predicate
   */
  private void addOperations(
      Set<ClassOrInterfaceType> concreteClassTypes,
      VisibilityPredicate visibility,
      ReflectionPredicate reflectionPredicate) {
    ReflectionManager mgr = new ReflectionManager(visibility);
    for (ClassOrInterfaceType classType : concreteClassTypes) {
      mgr.apply(
          new OperationExtractor(classType, operations, reflectionPredicate, visibility),
          classType.getRuntimeClass());
    }
  }

  /**
   * Create operations obtained by parsing method signatures and add each to this model.
   *
   * @param methodSignatures the set of method signatures
   * @throws OperationParseException if any signature is invalid
   */
  // TODO collect input types from added methods
  // TODO add operation conditions
  private void addOperations(Set<String> methodSignatures) throws OperationParseException {
    for (String sig : methodSignatures) {
      TypedOperation operation = OperationParser.parse(sig);
      operations.add(operation);
    }
  }

  /** Creates and adds the Object class default constructor call to the concrete operations. */
  private void addObjectConstructor() {
    Constructor<?> objectConstructor = null;
    try {
      objectConstructor = Object.class.getConstructor();
    } catch (NoSuchMethodException e) {
      System.err.println("Something is wrong. Please report: unable to load Object()");
      System.exit(1);
    }
    TypedClassOperation operation = TypedOperation.forConstructor(objectConstructor);
    classTypes.add(operation.getDeclaringType());
    operations.add(operation);
  }
}
