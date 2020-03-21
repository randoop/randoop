package randoop.reflection;

import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import randoop.condition.ExecutableSpecification;
import randoop.condition.SpecificationCollection;
import randoop.main.RandoopBug;
import randoop.operation.ConstructorCall;
import randoop.operation.EnumConstant;
import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.NonParameterizedType;
import randoop.types.Substitution;
import randoop.types.TypeTuple;
import randoop.util.Log;

/**
 * OperationExtractor is a {@link ClassVisitor} that creates a collection of {@link TypedOperation}
 * objects for a particular {@link ClassOrInterfaceType} through its visit methods as called by
 * {@link ReflectionManager#apply(Class)}.
 *
 * @see ReflectionManager
 * @see ClassVisitor
 */
public class OperationExtractor extends DefaultClassVisitor {

  /** Whether to produce debugging output to the Randoop log. */
  private static boolean debug = false;

  /** The type of the declaring class for the collected operations. */
  private ClassOrInterfaceType classType;

  /**
   * The operations collected by the extractor, and those omitted. This is the product of applying
   * the visitor.
   */
  private final OperationsAndOmitted operations = new OperationsAndOmitted();

  /** The reflection policy for collecting operations. */
  private final ReflectionPredicate reflectionPredicate;

  /** The predicate to test whether to omit an operation. */
  private OmitMethodsPredicate omitPredicate;

  /** The predicate to test visibility. */
  private final VisibilityPredicate visibilityPredicate;

  /** The specifications (pre/post/throws-conditions). */
  private final SpecificationCollection operationSpecifications;

  /**
   * Returns the operations in the class.
   *
   * @param classType the declaring class for collected operations
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(ClassOrInterfaceType classType) {
    return operations(
        classType,
        new DefaultReflectionPredicate(),
        OmitMethodsPredicate.NO_OMISSION,
        IS_PUBLIC,
        null);
  }

  /**
   * Returns the operations in the class.
   *
   * @param classTypes the declaring classes for collected operations
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(Collection<ClassOrInterfaceType> classTypes) {
    return operations(
        classTypes,
        new DefaultReflectionPredicate(),
        OmitMethodsPredicate.NO_OMISSION,
        IS_PUBLIC,
        null);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classType the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param visibilityPredicate the predicate for test visibility
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      ClassOrInterfaceType classType,
      ReflectionPredicate reflectionPredicate,
      VisibilityPredicate visibilityPredicate) {
    return operations(
        classType,
        reflectionPredicate,
        OmitMethodsPredicate.NO_OMISSION,
        visibilityPredicate,
        null);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classTypes the declaring classes for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param visibilityPredicate the predicate for test visibility
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      Collection<ClassOrInterfaceType> classTypes,
      ReflectionPredicate reflectionPredicate,
      VisibilityPredicate visibilityPredicate) {
    return operations(
        classTypes,
        reflectionPredicate,
        OmitMethodsPredicate.NO_OMISSION,
        visibilityPredicate,
        null);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classType the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitPredicate the list of {@code Pattern} objects for omitting methods, may be null
   * @param visibilityPredicate the predicate for test visibility
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      ClassOrInterfaceType classType,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitPredicate,
      VisibilityPredicate visibilityPredicate) {
    return operations(classType, reflectionPredicate, omitPredicate, visibilityPredicate, null);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classTypes the declaring classes for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitPredicate the list of {@code Pattern} objects for omitting methods, may be null
   * @param visibilityPredicate the predicate for test visibility
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      Collection<ClassOrInterfaceType> classTypes,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitPredicate,
      VisibilityPredicate visibilityPredicate) {
    return operations(classTypes, reflectionPredicate, omitPredicate, visibilityPredicate, null);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classType the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitMethodsPredicate the list of {@code Pattern} objects for omitting methods, may be
   *     null
   * @param visibilityPredicate the predicate for test visibility
   * @param operationSpecifications the specifications (pre/post/throws-conditions)
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      ClassOrInterfaceType classType,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitMethodsPredicate,
      VisibilityPredicate visibilityPredicate,
      SpecificationCollection operationSpecifications) {
    return operations(
        Collections.singletonList(classType),
        reflectionPredicate,
        omitMethodsPredicate,
        visibilityPredicate,
        operationSpecifications);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classTypes the declaring classes for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitMethodsPredicate the list of {@code Pattern} objects for omitting methods, may be
   *     null
   * @param visibilityPredicate the predicate for test visibility
   * @param operationSpecifications the specifications (pre/post/throws-conditions)
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      Collection<ClassOrInterfaceType> classTypes,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitMethodsPredicate,
      VisibilityPredicate visibilityPredicate,
      SpecificationCollection operationSpecifications) {
    OperationsAndOmitted operationsAndOmitted =
        operationsAndOmitted(
            classTypes,
            reflectionPredicate,
            omitMethodsPredicate,
            visibilityPredicate,
            operationSpecifications);
    return new ArrayList<>(operationsAndOmitted.getOperations());
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classTypes the declaring classes for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitMethodsPredicate the list of {@code Pattern} objects for omitting methods, may be
   *     null
   * @param visibilityPredicate the predicate for test visibility
   * @param operationSpecifications the specifications (pre/post/throws-conditions)
   * @return the operations in the class that sastisfy the given predicates
   */
  public static OperationsAndOmitted operationsAndOmitted(
      Collection<ClassOrInterfaceType> classTypes,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitMethodsPredicate,
      VisibilityPredicate visibilityPredicate,
      SpecificationCollection operationSpecifications) {

    OperationsAndOmitted result = new OperationsAndOmitted();
    ReflectionManager mgr = new ReflectionManager(visibilityPredicate);
    for (ClassOrInterfaceType classType : classTypes) {
      OperationExtractor extractor =
          new OperationExtractor(
              classType,
              reflectionPredicate,
              omitMethodsPredicate,
              visibilityPredicate,
              operationSpecifications);
      mgr.apply(extractor, classType.getRuntimeClass());
      result.union(extractor.operations);
    }
    return result;
  }

  /**
   * Converts a list of classes to a list of ClassOrInterfaceType.
   *
   * @param classes a list of Class objects
   * @return a list of ClassOrInterfaceType objects
   */
  public static List<ClassOrInterfaceType> classListToTypeList(List<Class<?>> classes) {
    List<ClassOrInterfaceType> result = new ArrayList<>();
    for (Class<?> c : classes) {
      result.add(ClassOrInterfaceType.forClass(c));
    }
    return result;
  }

  /**
   * Creates a visitor object that collects the {@link TypedOperation} objects corresponding to
   * members of the class satisfying the given visibility and reflection predicates and that don't
   * violate the omit method predicate.
   *
   * <p>Once created this visitor should only be applied to members of {@code
   * classType.getRuntimeType()}.
   *
   * @param classType the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitPredicate the list of {@code Pattern} objects for omitting methods, may be null
   * @param visibilityPredicate the predicate for test visibility
   * @param operationSpecifications the specifications (pre/post/throws-conditions)
   */
  public OperationExtractor(
      ClassOrInterfaceType classType,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitPredicate,
      VisibilityPredicate visibilityPredicate,
      SpecificationCollection operationSpecifications) {
    this.classType = classType;
    this.reflectionPredicate = reflectionPredicate;
    this.omitPredicate = omitPredicate;
    this.visibilityPredicate = visibilityPredicate;
    this.operationSpecifications = operationSpecifications;
  }

  /**
   * Creates a visitor object that collects the {@link TypedOperation} objects corresponding to
   * members of the class satisfying the given visibility and reflection predicates.
   *
   * @param classType the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param visibilityPredicate the predicate for test visibility
   */
  public OperationExtractor(
      ClassOrInterfaceType classType,
      ReflectionPredicate reflectionPredicate,
      VisibilityPredicate visibilityPredicate) {
    this(
        classType,
        reflectionPredicate,
        OmitMethodsPredicate.NO_OMISSION,
        visibilityPredicate,
        null);
  }

  /**
   * Creates a visitor object that collects the {@link TypedOperation} objects corresponding to
   * members of the class satisfying the given visibility and reflection predicates.
   *
   * @param classType the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitPredicate the list of {@code Pattern} objects for omitting methods, may be null
   * @param visibilityPredicate the predicate for test visibility
   */
  public OperationExtractor(
      ClassOrInterfaceType classType,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitPredicate,
      VisibilityPredicate visibilityPredicate) {
    this(classType, reflectionPredicate, omitPredicate, visibilityPredicate, null);
  }

  /**
   * Updates the operation types in the case that {@code operation.getDeclaringType()} is generic,
   * but {@code classType} is not. Constructs a {@link Substitution} that unifies the generic
   * declaring type with {@code classType} or a superType.
   *
   * @param operation the operation to instantiate
   * @return operation instantiated to match {@code classType} if the declaring type is generic and
   *     {@code classType} is not; the unmodified operation otherwise
   * @throws RandoopBug if there is no substitution that unifies the declaring type with {@code
   *     classType} or a supertype
   */
  private TypedClassOperation instantiateTypes(TypedClassOperation operation) {
    if (!classType.isGeneric() && operation.getDeclaringType().isGeneric()) {
      Substitution substitution =
          classType.getInstantiatingSubstitution(operation.getDeclaringType());
      if (substitution == null) { // No unifying substitution found
        throw new RandoopBug(
            String.format(
                "Type %s for operation %s is not a subtype of an instantiation of declaring class of method %s",
                classType, operation, operation.getDeclaringType()));
      }
      operation = operation.substitute(substitution);
      if (operation == null) {
        // No more details available because formal parameter {@code operation} was overwritten.
        throw new RandoopBug("Instantiation of operation failed");
      }
    }

    return operation;
  }

  /**
   * Ensures that field {@code classType} of this object is a subtype of {@code
   * operation.getDeclaringType()}; throws an exception if not.
   *
   * @param operation the operation for which types are to be checked
   * @throws RandoopBug if field {@code classType} of this is not a subtype of {@code
   *     operation.getDeclaringType()}
   */
  // TODO: poor name
  private void assertIsSubtypeOf(TypedClassOperation operation) {
    ClassOrInterfaceType declaringType = operation.getDeclaringType();
    if (!classType.isSubtypeOf(declaringType)) {
      throw new RandoopBug(
          String.format(
              "Incompatible receiver type for operation %s:%n  %s%nis not a subtype of%n  %s",
              operation, Log.toStringAndClass(classType), Log.toStringAndClass(declaringType)));
    }
  }

  /**
   * Creates a {@link ConstructorCall} object for the {@link Constructor}.
   *
   * @param constructor a {@link Constructor} object to be represented as an {@link Operation}
   */
  @Override
  public void visit(Constructor<?> constructor) {
    if (debug) {
      System.out.println("OperationExtractor.visit: constructor=" + constructor);
    }
    assert constructor.getDeclaringClass().equals(classType.getRuntimeClass())
        : "classType "
            + classType
            + " and declaring class "
            + constructor.getDeclaringClass().getName()
            + " should be same";
    if (!reflectionPredicate.test(constructor)) {
      return;
    }
    TypedClassOperation operation = instantiateTypes(TypedOperation.forConstructor(constructor));
    if (debug) {
      Log.logPrintf(
          "OperationExtractor.visit: operation=%s for constructor %s%n", operation, constructor);
    }
    assertIsSubtypeOf(operation);
    if (omitPredicate.shouldOmit(operation)) {
      operations.addOmittedOperation(operation);
    } else {
      if (debug) {
        System.out.printf(
            "OperationExtractor.visit: add operation %s%n", Log.toStringAndClass(operation));
      }
      if (operationSpecifications != null) {
        ExecutableSpecification execSpec =
            operationSpecifications.getExecutableSpecification(constructor);
        if (!execSpec.isEmpty()) {
          operation.setExecutableSpecification(execSpec);
        }
      }
      operations.addOperation(operation);
    }
  }

  /**
   * Creates a {@link MethodCall} object for the {@link Method}.
   *
   * <p>The created operation has the declaring class of {@code method} as the declaring type. An
   * exception is a static method for which the declaring class is not public, in which case {@link
   * #classType} is used as the declaring class.
   *
   * @param method a {@link Method} object to be represented as an {@link Operation}
   */
  @Override
  public void visit(Method method) {
    if (debug) {
      System.out.println("OperationExtractor.visit: method=" + method);
    }
    if (!reflectionPredicate.test(method)) {
      return;
    }
    TypedClassOperation operation = instantiateTypes(TypedOperation.forMethod(method));
    if (debug) {
      System.out.println("OperationExtractor.visit: operation=" + operation);
    }
    assertIsSubtypeOf(operation);

    if (omitPredicate.shouldOmit(operation)) {
      operations.addOmittedOperation(operation);
    } else {
      if (operationSpecifications != null) {
        ExecutableSpecification execSpec =
            operationSpecifications.getExecutableSpecification(method);
        if (!execSpec.isEmpty()) {
          operation.setExecutableSpecification(execSpec);
        }
      }
      if (debug) {
        System.out.println("OperationExtractor.visit: add operation " + operation);
      }
      operations.addOperation(operation);
    }
  }

  /**
   * Adds the {@link Operation} objects corresponding to getters and setters appropriate to the kind
   * of field.
   *
   * @param field a {@link Field} object to be represented as an {@link Operation}
   */
  @Override
  public void visit(Field field) {
    assert field.getDeclaringClass().isAssignableFrom(classType.getRuntimeClass())
        : "classType "
            + classType
            + " should be assignable from "
            + field.getDeclaringClass().getName();
    if (!reflectionPredicate.test(field)) {
      return;
    }

    ClassOrInterfaceType declaringType = ClassOrInterfaceType.forClass(field.getDeclaringClass());

    int mods = field.getModifiers() & Modifier.fieldModifiers();
    if (!visibilityPredicate.isVisible(field.getDeclaringClass())) {
      if (Modifier.isStatic(mods) && Modifier.isFinal(mods)) {
        // XXX This is a stop-gap to handle potentially ambiguous inherited constants.
        // A static final field of a non-public class may be accessible via a subclass, but only
        // if the field is not ambiguously inherited in the subclass. Without knowing for sure
        // whether there are two inherited fields with the same name, we cannot decide which case
        // is presented. So, assuming that there is an ambiguity and bailing on type.
        return;
      }
      if (!(declaringType.isGeneric() && classType.isInstantiationOf(declaringType))) {
        declaringType = classType;
      }
    }

    TypedClassOperation getter =
        instantiateTypes(TypedOperation.createGetterForField(field, declaringType));
    assertIsSubtypeOf(getter);
    if (getter != null) {
      operations.addOperation(getter);
    }
    if (!Modifier.isFinal(mods)) {
      TypedClassOperation operation =
          instantiateTypes(TypedOperation.createSetterForField(field, declaringType));
      if (operation != null) {
        operations.addOperation(operation);
      }
    }
  }

  /**
   * Creates a {@link EnumConstant} object for the {@link Enum}.
   *
   * @param e an {@link Enum} object to be represented as an {@link Operation}
   */
  @Override
  public void visit(Enum<?> e) {
    ClassOrInterfaceType enumType = NonParameterizedType.forClass(e.getDeclaringClass());
    assert !enumType.isGeneric() : "type of enum class cannot be generic";
    EnumConstant op = new EnumConstant(e);
    TypedClassOperation operation =
        new TypedClassOperation(op, enumType, new TypeTuple(), enumType);
    operations.addOperation(operation);
  }

  /**
   * Returns the operations of {@link #classType}.
   *
   * @return the operations collected for the class
   */
  public Collection<TypedOperation> getOperations() {
    return operations.getOperations();
  }

  /**
   * Returns the operations of {@link #classType} that were omitted.
   *
   * @return the operations omitted from the class
   */
  public Collection<TypedOperation> getOmittedOperations() {
    return operations.getOmittedOperations();
  }

  /** Two lists of operations: those that are included and those that are omitted. */
  public static class OperationsAndOmitted {
    /** The included operations. */
    private Collection<TypedOperation> operations;
    /** The omitted operations. */
    private Collection<TypedOperation> omittedOperations;
    /** Whether the operations have been filtered. */
    private boolean filtered = false;

    /** Create an empty OperationsAndOmitted. */
    OperationsAndOmitted() {
      this.operations = new TreeSet<>();
      this.omittedOperations = new TreeSet<>();
    }
    /**
     * @param operations the included operations
     * @param omittedOperations the omitted operations
     */
    OperationsAndOmitted(
        Collection<TypedOperation> operations, Collection<TypedOperation> omittedOperations) {
      this.operations = new TreeSet<>(operations);
      this.omittedOperations = new TreeSet<>(omittedOperations);
    }

    /**
     * Return the operations in this.
     *
     * @return the operations in this
     */
    public Collection<TypedOperation> getOperations() {
      filterOperations();
      return operations;
    }

    /**
     * Return the omitted operations in this.
     *
     * @return the omitted operations in this
     */
    public Collection<TypedOperation> getOmittedOperations() {
      filterOperations();
      return omittedOperations;
    }

    /**
     * Add an operation to this.
     *
     * @param op the operation to add to this
     */
    public void addOperation(TypedOperation op) {
      operations.add(op);
      filtered = false;
    }

    /**
     * Add an omitted operation to this.
     *
     * @param op the omitted operation to add to this
     */
    public void addOmittedOperation(TypedOperation op) {
      omittedOperations.add(op);
      filtered = false;
    }

    /**
     * Union in the given OperationsAndOmitted. Side-effects this.
     *
     * @param other the other OperationsAndOmitted
     */
    void union(OperationsAndOmitted other) {
      operations.addAll(other.operations);
      omittedOperations.addAll(other.omittedOperations);
      filtered = false;
    }

    /**
     * For any overridden implementation of an omitted operation, moves it from operations to
     * omittedOperations.
     */
    public void filterOperations() {
      if (filtered == true) {
        return;
      }
      filtered = true;

      List<Method> omittedMethods = new ArrayList<>();
      for (TypedOperation omitted : omittedOperations) {
        Executable methodOrConstructor = (Executable) omitted.getOperation().getReflectionObject();
        if (methodOrConstructor instanceof Method) {
          omittedMethods.add((Method) methodOrConstructor);
        }
      }
      Collection<Method> overriddenMethods = overriddenMethods(omittedMethods);
      for (TypedOperation op : new ArrayList<>(operations)) {
        // Possibly move op from operations to omittedOperations
        Executable methodOrConstructor = (Executable) op.getOperation().getReflectionObject();
        if (methodOrConstructor instanceof Method
            && overriddenMethods.contains(methodOrConstructor)) {
          operations.remove(op);
          omittedOperations.add(op);
        }
      }
    }
  }

  /// TODO: Move the below into a utility class such as ReflectionPlume.java.
  /// TODO: This implementation could be made more efficient, but it probably isn't a bottleneck.

  /**
   * Returns all the methods that the given method overrides.
   *
   * @param m a method
   * @return all the methods that {@code m} overrides
   */
  public static Collection<Method> overriddenMethods(Method m) {
    List<Method> result = new ArrayList<>();
    for (Class<?> c : getSuperTypes(m.getDeclaringClass())) {
      try {
        result.add(c.getMethod(m.getName(), m.getParameterTypes()));
      } catch (NoSuchMethodException e) {
        // nothing to do
      }
    }
    return result;
  }

  /**
   * Returns all the methods that the given methods override.
   *
   * @param methods a collection of methods
   * @return all the methods that are overridden by any of the given methods
   */
  public static Collection<Method> overriddenMethods(Collection<Method> methods) {
    Set<Method> result = new HashSet<>();
    for (Method m : methods) {
      result.addAll(overriddenMethods(m));
    }
    return result;
  }

  /**
   * Return the set of all of the supertypes of the given type.
   *
   * @param c a class
   * @return the set of all supertypes of the given type
   */
  public static Collection<Class<?>> getSuperTypes(Class<?> c) {
    Collection<Class<?>> supertypes = new ArrayList<>();
    Class<?> superclass = c.getSuperclass();
    if (superclass != null) {
      supertypes.add(superclass);
      supertypes.addAll(getSuperTypes(superclass));
    }
    for (Class<?> interfaceType : c.getInterfaces()) {
      supertypes.add(interfaceType);
      supertypes.addAll(getSuperTypes(interfaceType));
    }
    return supertypes;
  }
}
