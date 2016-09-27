package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import randoop.operation.MethodCall;
import randoop.operation.OperationParseException;
import randoop.operation.OperationParser;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.test.ContractSet;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.ParameterBound;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeVariable;
import randoop.util.Log;
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
public class OperationModel {

  /** The set of class declaration types for this model */
  private Set<ClassOrInterfaceType> classDeclarationTypes;

  private Set<ClassOrInterfaceType> concreteClassTypes;

  /** The set of input types for this model */
  private Set<Type> inputTypes;
  // TODO decide if should only collect ReferenceTypes.

  /** The set of class objects used in the exercised-class test filter */
  private final LinkedHashSet<Class<?>> exercisedClasses;

  /** Map for singleton sequences of literals extracted from classes. */
  private MultiMap<ClassOrInterfaceType, Sequence> classLiteralMap;

  /** Set of singleton sequences for values from TestValue annotated fields. */
  private Set<Sequence> annotatedTestValues;

  /** Set of object contracts used to generate tests. */
  private ContractSet contracts;

  /** Set of concrete operations extracted from classes */
  private Set<TypedOperation> operations;

  /**
   * Create an empty model of test context.
   *
   * <i>Note:</i> public because used in tests, but use
   * {@link #createModel(VisibilityPredicate, ReflectionPredicate, Set, Set, Set, ClassNameErrorHandler, List)}
   * instead.
   */
  public OperationModel() {
    classDeclarationTypes = new LinkedHashSet<>();
    concreteClassTypes = new LinkedHashSet<>();
    inputTypes = new LinkedHashSet<>();
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

    exercisedClasses = new LinkedHashSet<>();
    operations = new TreeSet<>();
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
   * @param methodSignatures  the signatures of methods to be added to the model
   * @param errorHandler  the handler for bad file name errors
   * @param literalsFileList  the list of literals file names
   * @return the operation model for the parameters
   * @throws OperationParseException if a method signature is ill-formed
   * @throws NoSuchMethodException if an attempt is made to load a non-existent method
   */
  public static OperationModel createModel(
      VisibilityPredicate visibility,
      ReflectionPredicate reflectionPredicate,
      Set<String> classnames,
      Set<String> exercisedClassnames,
      Set<String> methodSignatures,
      ClassNameErrorHandler errorHandler,
      List<String> literalsFileList)
      throws OperationParseException, NoSuchMethodException {

    OperationModel model = new OperationModel();

    model.addClassTypes(
        visibility,
        reflectionPredicate,
        classnames,
        exercisedClassnames,
        errorHandler,
        literalsFileList);
    model.instantiateGenericClassTypes();
    model.addOperations(model.concreteClassTypes, visibility, reflectionPredicate);
    model.addOperations(methodSignatures);
    model.addObjectConstructor();

    return model;
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
  public Set<ClassOrInterfaceType> getConcreteClasses() {
    return concreteClassTypes;
  }

  /**
   * Indicate whether the model has class types.
   *
   * @return true if the model has class types, and false if the class type set is empty
   */
  public boolean hasClasses() {
    return !classDeclarationTypes.isEmpty();
  }

  public List<TypedOperation> getConcreteOperations() {
    return new ArrayList<>(operations);
  }

  /**
   * Returns all {@link ObjectContract} objects for this run of Randoop.
   * Includes Randoop defaults and {@link randoop.CheckRep} annotated methods.
   *
   * @return the list of contracts
   */
  public ContractSet getContracts() {
    return contracts;
  }

  public Set<Sequence> getAnnotatedTestValues() {
    return annotatedTestValues;
  }

  /**
   * Gathers class types to be used in a run of Randoop and adds them to this {@code OperationModel}.
   * Specifically, collects types for classes-under-test, objects for exercised-class heuristic,
   * concrete input types, annotated test values, and literal values.
   * Also collects annotated test values, and class literal values used in test generation.
   *
   * @param visibility  the visibility predicate
   * @param reflectionPredicate  the predicate to determine which reflection objects are used
   * @param classnames  the names of classes-under-test
   * @param exercisedClassnames  the names of classes used in exercised-class heuristic
   * @param errorHandler  the handler for bad class names
   * @param literalsFileList  the list of literals file names
   */
  private void addClassTypes(
      VisibilityPredicate visibility,
      ReflectionPredicate reflectionPredicate,
      Set<String> classnames,
      Set<String> exercisedClassnames,
      ClassNameErrorHandler errorHandler,
      List<String> literalsFileList) {
    ReflectionManager mgr = new ReflectionManager(visibility);
    mgr.add(new DeclarationExtractor(this.classDeclarationTypes, reflectionPredicate));
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
          if (exercisedClassnames.contains(classname)) {
            exercisedClasses.add(c);
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
              "Ignoring non-visible " + c + " specified as include-if-class-exercised target");
        } else if (c.isInterface()) {
          System.out.println("Ignoring " + c + " specified as include-if-class-exercised target.");
        } else {
          exercisedClasses.add(c);
        }
      }
    }
  }

  /**
   * Selects instantiations of the generic class declarations in this model from the collected set
   * of instantiated input types.
   */
  private void instantiateGenericClassTypes() {
    for (ClassOrInterfaceType classType : classDeclarationTypes) {
      if (classType.isGeneric()) {
        List<TypeVariable> typeParameters = classType.getTypeParameters();
        List<Substitution<ReferenceType>> substitutions = getSubstitutions(typeParameters);
        if (substitutions.size() > 0) {
          Substitution<ReferenceType> substitution = Randomness.randomMember(substitutions);
          ClassOrInterfaceType refinedClassType = classType.apply(substitution);
          if (!refinedClassType.isGeneric()) {
            concreteClassTypes.add(refinedClassType);
          }
        } else {
          if (Log.isLoggingOn()) {
            Log.logLine("Didn't find types to satisfy bounds on generic type: " + classType);
          }
        }
      } else {
        concreteClassTypes.add(classType);
      }
    }
  }

  /**
   * Constructs substitutions for the given list of type parameters that are candidates for
   * instantiating a generic class type.
   *
   * @param typeParameters  the type parameters to be instantiated
   * @return candidate substitutions for the given type parameters
   */
  private List<Substitution<ReferenceType>> getSubstitutions(List<TypeVariable> typeParameters) {
    TypeTupleSet candidateSet = new TypeTupleSet();
    for (TypeVariable typeArgument : typeParameters) {
      List<ReferenceType> candidateTypes = selectCandidates(typeArgument);
      candidateSet.extend(candidateTypes);
    }
    return candidateSet.filter(typeParameters);
  }

  /**
   * Selects all input types that potentially satisfies the bounds on the argument.
   * If a bound has another type parameter, then the default bound is tested.
   *
   * @param argument  the type arguments
   * @return the list of candidate types to include in tested tuples
   */
  private List<ReferenceType> selectCandidates(TypeVariable argument) {
    ParameterBound lowerBound = selectLowerBound(argument);
    ParameterBound upperBound = selectUpperBound(argument);

    List<TypeVariable> typeVariableList = new ArrayList<>();
    typeVariableList.add(argument);
    List<ReferenceType> typeList = new ArrayList<>();
    for (Type inputType : inputTypes) {
      if (inputType.isReferenceType()) {
        ReferenceType inputRefType = (ReferenceType) inputType;
        Substitution<ReferenceType> substitution =
            Substitution.forArgs(typeVariableList, inputRefType);
        if (lowerBound.isLowerBound(inputRefType, substitution)
            && upperBound.isUpperBound(inputRefType, substitution)) {
          typeList.add(inputRefType);
        }
      }
    }
    return typeList;
  }

  /**
   * Chooses the upper bound of the given argument to test in {@link #selectCandidates(TypeVariable)}.
   * If the bound contains a type parameter other than the given argument, then the bound for
   * the {@code Object} type is returned.
   *
   * @param argument  the type argument
   * @return the upperbound of the argument if no other type parameter is needed, the {@code Object}
   * bound otherwise
   */
  private ParameterBound selectUpperBound(TypeVariable argument) {
    ParameterBound upperBound = argument.getUpperTypeBound();
    List<TypeVariable> parameters = upperBound.getTypeParameters();
    if (parameters.isEmpty() || (parameters.size() == 1 && parameters.contains(argument))) {
      return upperBound;
    }
    return ParameterBound.forType(JavaTypes.OBJECT_TYPE);
  }

  /**
   * Chooses the lower bound of the given argument to be tested in {@link #selectCandidates(TypeVariable)}.
   * If the bound has a type parameter other than the given argument, then the
   * {@link JavaTypes#NULL_TYPE}
   * is return as the bound.
   *
   * @param argument  the type argument
   * @return the lower bound of the argument if no other type parameter is needed, the {@link JavaTypes#NULL_TYPE}
   * otherwise
   */
  private ParameterBound selectLowerBound(TypeVariable argument) {
    ParameterBound lowerBound = argument.getLowerTypeBound();
    List<TypeVariable> parameters = lowerBound.getTypeParameters();
    if (parameters.isEmpty() || (parameters.size() == 1 && parameters.contains(argument))) {
      return lowerBound;
    }
    return ParameterBound.forType(JavaTypes.NULL_TYPE);
  }

  /**
   * Iterates through a set of simple and instantiated class types and uses reflection to extract
   * the operations that satisfy both the visibility and reflection predicates, and then adds them
   * to the operation set of this model.
   *
   * @param concreteClassTypes  the declaring class types for the operations
   * @param visibility  the visibility predicate
   * @param reflectionPredicate  the reflection predicate
   */
  private void addOperations(
      Set<ClassOrInterfaceType> concreteClassTypes,
      VisibilityPredicate visibility,
      ReflectionPredicate reflectionPredicate) {
    Set<TypedOperation> operationSet = new LinkedHashSet<>();
    ReflectionManager mgr = new ReflectionManager(visibility);
    for (ClassOrInterfaceType classType : concreteClassTypes) {
      mgr.apply(
          new OperationExtractor(classType, operationSet, reflectionPredicate, this),
          classType.getRuntimeClass());
    }
    for (TypedOperation operation : operationSet) {
      addOperation(operation);
    }
  }

  /**
   * Create operations obtained by parsing method signatures and add each to this model.
   *
   * @param methodSignatures  the set of method signatures
   * @throws OperationParseException if any signature is invalid
   */
  // TODO collect input types from added methods
  private void addOperations(Set<String> methodSignatures) throws OperationParseException {
    for (String sig : methodSignatures) {
      TypedOperation operation = OperationParser.parse(sig);
      addOperation(operation);
    }
  }

  /**
   * Creates and adds the Object class default constructor call to the concrete operations.
   */
  private void addObjectConstructor() {
    Constructor<?> objectConstructor = null;
    try {
      objectConstructor = Object.class.getConstructor();
    } catch (NoSuchMethodException e) {
      System.out.println("Something is wrong. Please report: unable to load Object()");
      System.exit(1);
    }
    TypedClassOperation operation = TypedOperation.forConstructor(objectConstructor);
    concreteClassTypes.add(operation.getDeclaringType());
    addOperation(operation);
  }

  /**
   * Instantiates and adds the given {@link TypedOperation} to this model.
   * Any type parameters of the operation are first instantiated by selecting from the input types
   * of this model.
   * Then, if the operation has wildcard types, capture conversion is applied, and any created
   * type variables are instantiated.
   *
   * @param operation the operation to instantiate and add to this model
   */
  private void addOperation(TypedOperation operation) {
    operation = instantiateOperationTypes(operation);

    // Note: capture conversion needs all type variables to be instantiated first
    if (operation != null && operation.hasWildcardTypes()) {
      operation = instantiateOperationTypes(operation.applyCaptureConversion());
    }
    if (operation == null) {
      return;
    }
    operations.add(operation);
  }

  /**
   * Selects an instantiation of the generic types of an operation, and returns a new operation with
   * the types instantiated.
   *
   * @param operation  the operation
   * @return the operation with generic types instantiated
   */
  private TypedOperation instantiateOperationTypes(TypedOperation operation) {
    List<TypeVariable> typeParameters = operation.getTypeParameters();
    if (typeParameters.isEmpty()) {
      return operation;
    }
    List<Substitution<ReferenceType>> substitutions = getSubstitutions(typeParameters);
    if (substitutions.isEmpty()) {
      return null;
    }
    Substitution<ReferenceType> substitution = Randomness.randomMember(substitutions);
    return operation.apply(substitution);
  }

  /**
   * Selects an instantiation of a generic operation, and returns a new operation with the types
   * instantiated.
   *
   * @param operation  the operation
   * @param substitution  the substitution for class type parameters
   * @return the operation with generic types instantiated
   */
  TypedClassOperation instantiateOperationTypes(
      TypedClassOperation operation, Substitution<ReferenceType> substitution) {
    List<TypeVariable> typeParameters = operation.getTypeParameters();
    if (substitution != null) {
      typeParameters.removeAll(substitution.getVariables());
    } else {
      substitution = new Substitution<>();
    }
    if (!typeParameters.isEmpty()) {
      List<Substitution<ReferenceType>> substitutions = getSubstitutions(typeParameters);
      if (substitutions.isEmpty()) {
        if (Log.isLoggingOn()) {
          Log.logLine("Unable to instantiate types for operation " + operation);
        }
        return null;
      }
      Substitution<ReferenceType> operationTypeSubstitution =
          Randomness.randomMember(substitutions);
      substitution = substitution.extend(operationTypeSubstitution);
    }
    return operation.apply(substitution);
  }
}
