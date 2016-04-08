package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.contract.EqualsHashcode;
import randoop.contract.EqualsReflexive;
import randoop.contract.EqualsSymmetric;
import randoop.contract.EqualsToNullRetFalse;
import randoop.contract.ObjectContract;
import randoop.generation.ComponentManager;
import randoop.main.ClassNameErrorHandler;
import randoop.operation.ConcreteOperation;
import randoop.operation.ConstructorCall;
import randoop.operation.GenericOperation;
import randoop.operation.MethodCall;
import randoop.operation.OperationParseException;
import randoop.operation.OperationParser;
import randoop.sequence.Sequence;
import randoop.types.ConcreteType;
import randoop.types.GeneralType;
import randoop.types.GenericType;
import randoop.types.GenericTypeTuple;
import randoop.types.RandoopTypeException;
import randoop.types.TypeNames;
import randoop.util.MultiMap;

import static randoop.main.GenInputsAbstract.ClassLiteralsMode;

/**
 * {@code OperationModel} represents the information context from which tests are generated.
 * The model includes:
 * <ul>
 *   <li>classes under test,</li>
 *   <li>operations of all classes,</li>
 *   <li>any atomic code sequences derived from command-line arguments, and </li>
 *   <li>the contracts or oracles used to generate tests.</li>
 * </ul>
 * <p>
 * This class manages all information about generic classes internally, and instantiates any
 * type variables in operations before returning them.
 */
public class OperationModel extends ModelCollections {

  /** The set of class objects used in the exercised-class test filter */
  private final LinkedHashSet<Class<?>> exercisedClasses;

  /** Map for singleton sequences of literals extracted from classes. */
  private MultiMap<ConcreteType, Sequence> classLiteralMap;

  /** Set of singleton sequences for values from TestValue annotated fields. */
  private Set<Sequence> annotatedTestValues;

  /** Set of object contracts used to generate tests. */
  private Set<ObjectContract> contracts;

  /** Set of concrete operations extracted from classes */
  private Set<ConcreteOperation> operations;

  /** Set of generic operations extracted from classes */
  private final Set<GenericOperation> genericOperations;

  /** Set of concrete class types extracted/constructed from classes */
  private Set<ConcreteType> classTypes;

  private MultiMap<GenericType, GenericOperation> genericClassTypes;


  /**
   * Create an empty model of test context.
   */
  private OperationModel() {
    classLiteralMap = new MultiMap<>();
    annotatedTestValues = new LinkedHashSet<>();
    contracts = new LinkedHashSet<>();
    exercisedClasses = new LinkedHashSet<>();
    operations = new LinkedHashSet<>();
    genericOperations = new LinkedHashSet<>();
    classTypes = new LinkedHashSet<>();
    genericClassTypes = new MultiMap<>();
  }

  /**
   * Factory method to construct an operation model for a particular set of classes
   *
   * @param visibility
   *          the {@link randoop.reflection.VisibilityPredicate} to test
   *          accessibility of classes and class members.
   * @param reflectionPredicate  the reflection predicate to determine which classes and
   *                             class members are used
   * @param classnames  the names of classes under test
   * @param exercisedClassnames  the names of classes to be tested by exercised heuristic
   * @param literalsFileList  the list of literals file names
   * @return the operation model for the parameters
   */
  public static OperationModel createModel(
      VisibilityPredicate visibility,
      ReflectionPredicate reflectionPredicate,
      Set<String> classnames,
      Set<String> exercisedClassnames,
      Set<String> methodSignatures,
      ClassNameErrorHandler errorHandler,
      List<String> literalsFileList) throws OperationParseException, NoSuchMethodException {

    // TODO make sure adding Object constructor

    Set<Class<?>> visitedClasses = new LinkedHashSet<>();
    Set<ConcreteType> inputTypes = new LinkedHashSet<>();

    OperationModel model = new OperationModel();
    ReflectionManager mgr = new ReflectionManager(visibility);
    ClassVisitor opExtractor = new OperationExtractor(new TypedOperationManager(model), reflectionPredicate);
    mgr.add(opExtractor);
    mgr.add(new InputTypeExtractor(inputTypes));
    mgr.add(new TestValueExtractor(model.annotatedTestValues));
    mgr.add(new CheckRepExtractor(model.contracts));
    if (literalsFileList.contains("CLASSES")) {
      mgr.add(new ClassLiteralExtractor(model.classLiteralMap));
    }

    // Collect classes under test
    for (String classname : classnames) {
      Class<?> c = null;
      try {
        c = TypeNames.getTypeForName(classname);
      } catch (ClassNotFoundException e) {
        errorHandler.handle(classname);
      }
      // Note that c could be null if errorHandler just warns on bad names
      if (c != null && ! visitedClasses.contains(c)) {
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
          if (exercisedClassnames.contains(classname)) {
            model.addExercisedClass(c);
          }
        }
      }
    }

    // Collect exercised classes
    for (String classname : exercisedClassnames) {
      if (!classnames.contains(classname)) {
        Class<?> c = null;
        try {
          c = TypeNames.getTypeForName(classname);
        } catch (ClassNotFoundException e) {
          errorHandler.handle(classname);
        }
        assert c != null;

        if (!visibility.isVisible(c)) {
          System.out.println(
              "Ignorning non-visible " + c + " specified as include-if-class-exercised target");
        } else if (c.isInterface()) {
          System.out.println("Ignoring " + c + " specified as include-if-class-exercised target.");
        } else {
          model.addExercisedClass(c);
        }
      }
    }

    model.addDefaultContracts();
    model.addOperations(methodSignatures);
    model.addObjectConstructor();

    return model;
  }

  /**
   * Creates and adds the Object class default constructor call to the concrete operations.
   */
  private void addObjectConstructor()  {
    Constructor<?> objectConstructor = null;
    try {
      objectConstructor = Object.class.getConstructor();
    } catch (NoSuchMethodException e) {
      System.out.println("Something is really wrong. Please report unable to load Object()");
      System.exit(1);
    }
    try {
      operations.add(getConcreteOperation(objectConstructor));
    } catch (RandoopTypeException e) {
      System.out.println("Something is really wrong. Please report type error in reading object constructor.");
      System.exit(1);
    }
  }

  /**
   * Adds literals to the component manager, by parsing any literals files
   * specified by the user.
   * Includes literals at different levels indicated by {@link ClassLiteralsMode}.
   *
   * @param compMgr  the component manager
   * @param literalsFile  the list of literals file names
   * @param literalsLevel  the level of literals to add
   */
  public void addClassLiterals(
      ComponentManager compMgr, List<String> literalsFile, ClassLiteralsMode literalsLevel) {

    // Add a (1-element) sequence corresponding to each literal to the component
    // manager.

    for (String filename : literalsFile) {
      MultiMap<ConcreteType, Sequence> literalmap;
      if (filename.equals("CLASSES")) {
        literalmap = classLiteralMap;
      } else {
        literalmap = LiteralFileReader.parse(filename);
      }

      for (ConcreteType type : literalmap.keySet()) {
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
   * @param observerSignatures  the set of method signatures
   * @return the map to observer methods from their declaring class type
   * @throws OperationParseException if a method signature cannot be parsed
   */
  public MultiMap<ConcreteType, ConcreteOperation> getObservers(Set<String> observerSignatures) throws OperationParseException {
    // Populate observer_map from observers file.
    MultiMap<ConcreteType, ConcreteOperation> observerMap = new MultiMap<>();
    for (String sig: observerSignatures) {
      ModelCollections observerManager = new ObserverCollections(observerMap);
      MethodCall.parse(sig, new TypedOperationManager(observerManager));
    }
    return observerMap;
  }

  /**
   * Adds a class to the set of classes for the exercised-class heuristic.
   *
   * @param c  the class to add
   */
  private void addExercisedClass(Class<?> c) {
    exercisedClasses.add(c);
  }

  /**
   * Returns the set of identified {@code Class<?>} objects for the exercised class heuristic.
   *
   * @return the set of exercised classes
   */
  public Set<Class<?>> getExercisedClasses() {
    return exercisedClasses;
  }

  /**
   * Returns the set of types for concrete (non-generic) classes in this model.
   * Includes all instantiated generic classes.
   *
   * @return the set of concrete types for the classes in this model
   */
  public Set<ConcreteType> getClasses() {
    return classTypes;
  }

  /**
   * Indicate whether the model has class types.
   *
   * @return true if the model has class types, and false if the class type set is empty
   */
  public boolean hasClasses() {
    return ! classTypes.isEmpty();
  }

  public List<ConcreteOperation> getConcreteOperations() {
    return new ArrayList<>(operations);
  }

  private void addOperations(Set<String> methodSignatures) throws OperationParseException {
    for (String sig : methodSignatures) {
      OperationParser.parse(sig, new TypedOperationManager(this));
    }
  }

  /**
   * Add Randoop's default contracts for test generation.
   * <p>
   *   Note: if you add to this list, also update the Javadoc for check_object_contracts.
   */
  private void addDefaultContracts() {
    contracts.add(new EqualsReflexive());
    contracts.add(new EqualsSymmetric());
    contracts.add(new EqualsHashcode());
    contracts.add(new EqualsToNullRetFalse());
  }

  /**
   * Returns all {@link ObjectContract} objects for this run of Randoop.
   * Includes Randoop defaults and {@link randoop.CheckRep} annotated methods.
   *
   * @return the list of contracts
   */
  public Set<ObjectContract> getContracts() {
    return contracts;
  }

  public ConcreteOperation getConcreteOperation(Constructor<?> constructor) throws RandoopTypeException {
    ConcreteType declaringType = ConcreteType.forClass(constructor.getDeclaringClass());
    ConstructorCall op = new ConstructorCall(constructor);
    List<GeneralType> paramTypes = new ArrayList<>();
    for (Type t : constructor.getGenericParameterTypes()) {
      paramTypes.add(GeneralType.forType(t));
    }
    GenericTypeTuple inputTypes = new GenericTypeTuple(paramTypes);
    return new ConcreteOperation(op, declaringType, inputTypes.makeConcrete(), declaringType);
  }

  public Set<Sequence> getAnnotatedTestValues() {
    return annotatedTestValues;
  }

  /*
   * ModelCollections methods
   */
  @Override
  public void addConcreteClassType(ConcreteType type) {
    classTypes.add(type);
  }

  @Override
  public void addGenericOperation(GenericType declaringType, GenericOperation operation) {
    genericClassTypes.add(declaringType, operation);
  }

  @Override
  public void addGenericOperation(ConcreteType declaringType, GenericOperation operation) {
    genericOperations.add(operation);
  }

  @Override
  public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
    operations.add(operation);
  }

  /**
   * {@code ObserverCollections} is a {@link ModelCollections} implementation that stores
   * observer operations in a {@link MultiMap} provided to the constructor.
   */
  private class ObserverCollections extends ModelCollections {

    /** The map of types to observers */
    private final MultiMap<ConcreteType, ConcreteOperation> observerMap;

    /**
     * Creates an observer collection that stores observers in the given map.
     *
     * @param observerMap  the map to which this object adds observers
     */
    ObserverCollections(MultiMap<ConcreteType, ConcreteOperation> observerMap) {
      this.observerMap = observerMap;
    }

    /**
     * {@inheritDoc}
     * Adds an observer operation for the given declaring type to the observer map.
     */
    @Override
    public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
      ConcreteType outputType = operation.getOutputType();
      if (outputType.isPrimitive() || outputType.isString() || outputType.isEnum()) {
        observerMap.add(declaringType, operation);
      }
    }

    @Override
    public void addGenericOperation(GenericType declaringType, GenericOperation operation) {
      System.out.println("Got a generic observer: " + operation);
    }

  }

}
