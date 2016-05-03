package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
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
import randoop.operation.MethodCall;
import randoop.operation.OperationParseException;
import randoop.operation.OperationParser;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GeneralType;
import randoop.types.ParameterBound;
import randoop.types.ParameterizedType;
import randoop.types.RandoopTypeException;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeArgument;
import randoop.types.TypeNames;
import randoop.types.TypeVariable;
import randoop.types.WildcardArgument;
import randoop.util.MultiMap;
import randoop.util.Randomness;

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
  private MultiMap<ClassOrInterfaceType, Sequence> classLiteralMap;

  /** Set of singleton sequences for values from TestValue annotated fields. */
  private Set<Sequence> annotatedTestValues;

  /** Set of object contracts used to generate tests. */
  private Set<ObjectContract> contracts;

  /** Set of concrete operations extracted from classes */
  private Set<TypedOperation> operations;

  /** Set of generic operations extracted from classes */
  private final Set<TypedOperation> genericOperations;

  /** Set of concrete class types extracted/constructed from classes */
  private Set<GeneralType> classTypes;

  private MultiMap<ParameterizedType, TypedOperation> genericClassTypes;


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
      List<String> literalsFileList) throws OperationParseException, NoSuchMethodException, RandoopTypeException {

    // TODO make sure adding Object constructor

    Set<Class<?>> visitedClasses = new LinkedHashSet<>();
    Set<GeneralType> inputTypes = new LinkedHashSet<>();

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
    model.refineGenericClassTypes(inputTypes);
    return model;
  }

  private void refineGenericClassTypes(Set<GeneralType> inputTypes) throws RandoopTypeException {
    for (ParameterizedType classType : genericClassTypes.keySet()) {
      List<Substitution<ReferenceType>> substitutions = getSubstitutions(inputTypes, classType);
      assert  substitutions.size() > 0 : "didn't find types to satisfy bounds on generic";
      Substitution<ReferenceType> substitution = Randomness.randomMember(substitutions);
      ParameterizedType refinedClassType = classType.apply(substitution);
      if (! refinedClassType.isGeneric()) {
        classTypes.add(refinedClassType);
      }
      for (TypedOperation operation : genericClassTypes.getValues(classType)) {
        TypedOperation op = operation.apply(substitution);
        if (op.isGeneric()) {
          genericOperations.add(op);
        } else {
          operations.add(op);
        }

      }
    }
  }

  private List<Substitution<ReferenceType>> getSubstitutions(Set<GeneralType> inputTypes, ParameterizedType classType) throws RandoopTypeException {
    List<TypeArgument> typeArguments = classType.getTypeArguments();
    TypeTupleSet candidateSet = new TypeTupleSet();
    for (TypeArgument typeArgument : typeArguments) {
      List<ReferenceType> candidateTypes = selectCandidates(typeArgument, inputTypes);
      candidateSet.extend(candidateTypes);
    }
    return candidateSet.filter(classType.getTypeArguments());
  }

  /**
   * Selects all input types that potentially satisfies the upper bound.
   * If the bound is concrete, then returned list exactly satisfies the bound.
   * If the bound is generic, then the types are convertible a la Class.isAssignableFrom.
   * Otherwise, the input types are returned as a list.
   *
   * @param argument  the type arguments
   * @param inputTypes  the set of input types to test
   * @return the list of candidate types to included in tested tuples
   */
  private List<ReferenceType> selectCandidates(TypeArgument argument, Set<GeneralType> inputTypes) {

    if (argument instanceof ReferenceArgument) {
      ReferenceType referenceType = ((ReferenceArgument)argument).getReferenceType();
      if (referenceType instanceof TypeVariable) {
        return selectCandidates(((TypeVariable)referenceType).getTypeBound(), inputTypes);
      }
      // otherwise argument is a particular class, though may be generic
    }

    if (argument instanceof WildcardArgument) {
      ReferenceType bound = ((WildcardArgument) argument).getBoundType();
      for (GeneralType type : inputTypes) {
        // if bound is generic, can be instantiated
        // if bound is concrete, then equal
      }
    }

    throw new IllegalArgumentException("unknown argument type " + argument);
  }

  private List<ReferenceType> selectCandidates(ParameterBound bound, Set<GeneralType> inputTypes) {
    List<ReferenceType> typeList = new ArrayList<>();
    for (GeneralType inputType : inputTypes) {
      if (inputType.isReferenceType() && bound.isSatisfiedBy(inputType)) {
        typeList.add((ReferenceType)inputType);
      }
    }
    return typeList;
  }

  /**
   * Creates and adds the Object class default constructor call to the concrete operations.
   */
  private void addObjectConstructor() throws RandoopTypeException {
    Constructor<?> objectConstructor = null;
    try {
      objectConstructor = Object.class.getConstructor();
    } catch (NoSuchMethodException e) {
      System.out.println("Something is wrong. Please report unable to load Object()");
      System.exit(1);
    }
    TypedClassOperation operation = TypedOperation.forConstructor(objectConstructor);
    addConcreteClassType(operation.getDeclaringType());
    addConcreteOperation(operation.getDeclaringType(), operation);
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
   * @param observerSignatures  the set of method signatures
   * @return the map to observer methods from their declaring class type
   * @throws OperationParseException if a method signature cannot be parsed
   */
  public MultiMap<GeneralType, TypedOperation> getObservers(Set<String> observerSignatures) throws OperationParseException {
    // Populate observer_map from observers file.
    MultiMap<GeneralType, TypedOperation> observerMap = new MultiMap<>();
    for (String sig: observerSignatures) {
      TypedClassOperation operation = MethodCall.parse(sig);
      GeneralType outputType = operation.getOutputType();
      if (outputType.isPrimitive() || outputType.isString() || outputType.isEnum()) {
        observerMap.add(operation.getDeclaringType(), operation);
      }
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
  public Set<GeneralType> getClasses() {
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

  public List<TypedOperation> getConcreteOperations() {
    return new ArrayList<>(operations);
  }

  // TODO collect input types from added methods
  private void addOperations(Set<String> methodSignatures) throws OperationParseException {
    TypedOperationManager manager = new TypedOperationManager(this);
    for (String sig : methodSignatures) {
      TypedOperation operation = OperationParser.parse(sig);
      manager.addOperation((TypedClassOperation)operation);
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

  public Set<Sequence> getAnnotatedTestValues() {
    return annotatedTestValues;
  }

  /*
   * ModelCollections methods
   */
  @Override
  public void addConcreteClassType(ClassOrInterfaceType type) {
    classTypes.add(type);
  }

  @Override
  public void addOperationToGenericType(ParameterizedType declaringType, TypedOperation operation) {
    genericClassTypes.add(declaringType, operation);
  }

  @Override
  public void addGenericOperation(ClassOrInterfaceType declaringType, TypedOperation operation) {
    genericOperations.add(operation);
  }

  @Override
  public void addConcreteOperation(ClassOrInterfaceType declaringType, TypedOperation operation) {
    operations.add(operation);
  }

}
