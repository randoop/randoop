package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import org.plumelib.util.CollectionsPlume;
import org.plumelib.util.StringsPlume;
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

  /** The operations collected by the extractor. This is the product of applying the visitor. */
  private final Collection<TypedOperation> operations = new TreeSet<>();

  /** The reflection policy for collecting operations. */
  private final ReflectionPredicate reflectionPredicate;

  /** The predicate to test whether to omit an operation. */
  private OmitMethodsPredicate omitPredicate;

  /** The predicate to test accessibility. */
  private final AccessibilityPredicate accessibilityPredicate;

  /** The specifications (pre/post/throws-conditions). */
  private final SpecificationCollection operationSpecifications;

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param clazz the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param accessibilityPredicate the predicate for test accessibility
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      Class<?> clazz,
      ReflectionPredicate reflectionPredicate,
      AccessibilityPredicate accessibilityPredicate) {
    return operations(
        clazz, reflectionPredicate, OmitMethodsPredicate.NO_OMISSION, accessibilityPredicate, null);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classType the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param accessibilityPredicate the predicate for test accessibility
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      ClassOrInterfaceType classType,
      ReflectionPredicate reflectionPredicate,
      AccessibilityPredicate accessibilityPredicate) {
    return operations(
        classType,
        reflectionPredicate,
        OmitMethodsPredicate.NO_OMISSION,
        accessibilityPredicate,
        null);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classTypes the declaring classes for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param accessibilityPredicate the predicate for test accessibility
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      Collection<ClassOrInterfaceType> classTypes,
      ReflectionPredicate reflectionPredicate,
      AccessibilityPredicate accessibilityPredicate) {
    return operations(
        classTypes,
        reflectionPredicate,
        OmitMethodsPredicate.NO_OMISSION,
        accessibilityPredicate,
        null);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param clazz the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitPredicate the list of {@code Pattern} objects for omitting methods, may be null
   * @param accessibilityPredicate the predicate for test accessibility
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      Class<?> clazz,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitPredicate,
      AccessibilityPredicate accessibilityPredicate) {
    return operations(clazz, reflectionPredicate, omitPredicate, accessibilityPredicate, null);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classType the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitPredicate the list of {@code Pattern} objects for omitting methods, may be null
   * @param accessibilityPredicate the predicate for test accessibility
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      ClassOrInterfaceType classType,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitPredicate,
      AccessibilityPredicate accessibilityPredicate) {
    return operations(classType, reflectionPredicate, omitPredicate, accessibilityPredicate, null);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classTypes the declaring classes for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitPredicate the list of {@code Pattern} objects for omitting methods, may be null
   * @param accessibilityPredicate the predicate for test accessibility
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      Collection<ClassOrInterfaceType> classTypes,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitPredicate,
      AccessibilityPredicate accessibilityPredicate) {
    return operations(classTypes, reflectionPredicate, omitPredicate, accessibilityPredicate, null);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param clazz the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitMethodsPredicate the list of {@code Pattern} objects for omitting methods, may be
   *     null
   * @param accessibilityPredicate the predicate for test accessibility
   * @param operationSpecifications the specifications (pre/post/throws-conditions)
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      Class<?> clazz,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitMethodsPredicate,
      AccessibilityPredicate accessibilityPredicate,
      SpecificationCollection operationSpecifications) {
    return operations(
        ClassOrInterfaceType.forClass(clazz),
        reflectionPredicate,
        omitMethodsPredicate,
        accessibilityPredicate,
        operationSpecifications);
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classType the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitMethodsPredicate the list of {@code Pattern} objects for omitting methods, may be
   *     null
   * @param accessibilityPredicate the predicate for test accessibility
   * @param operationSpecifications the specifications (pre/post/throws-conditions)
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      ClassOrInterfaceType classType,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitMethodsPredicate,
      AccessibilityPredicate accessibilityPredicate,
      SpecificationCollection operationSpecifications) {
    ReflectionManager mgr = new ReflectionManager(accessibilityPredicate);
    OperationExtractor extractor =
        new OperationExtractor(
            classType,
            reflectionPredicate,
            omitMethodsPredicate,
            accessibilityPredicate,
            operationSpecifications);
    mgr.apply(extractor, classType.getRuntimeClass());
    return new ArrayList<>(extractor.getOperations());
  }

  /**
   * Returns the operations in the class that satisfy the given predicates.
   *
   * @param classTypes the declaring classes for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitMethodsPredicate the list of {@code Pattern} objects for omitting methods, may be
   *     null
   * @param accessibilityPredicate the predicate for test accessibility
   * @param operationSpecifications the specifications (pre/post/throws-conditions)
   * @return the operations in the class that sastisfy the given predicates
   */
  public static List<TypedOperation> operations(
      Collection<ClassOrInterfaceType> classTypes,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitMethodsPredicate,
      AccessibilityPredicate accessibilityPredicate,
      SpecificationCollection operationSpecifications) {
    Collection<TypedOperation> result = new TreeSet<>();
    ReflectionManager mgr = new ReflectionManager(accessibilityPredicate);
    for (ClassOrInterfaceType classType : classTypes) {
      OperationExtractor extractor =
          new OperationExtractor(
              classType,
              reflectionPredicate,
              omitMethodsPredicate,
              accessibilityPredicate,
              operationSpecifications);
      mgr.apply(extractor, classType.getRuntimeClass());
      result.addAll(extractor.getOperations());
    }
    return new ArrayList<>(result);
  }

  /**
   * Converts a list of classes to a list of ClassOrInterfaceType.
   *
   * @param classes a list of Class objects
   * @return a list of ClassOrInterfaceType objects
   */
  public static List<ClassOrInterfaceType> classListToTypeList(List<Class<?>> classes) {
    return CollectionsPlume.mapList(ClassOrInterfaceType::forClass, classes);
  }

  /**
   * Creates a visitor object that collects the {@link TypedOperation} objects corresponding to
   * members of the class satisfying the given accessibility and reflection predicates and that
   * don't violate the omit method predicate.
   *
   * <p>Once created this visitor should only be applied to members of {@code
   * classType.getRuntimeType()}.
   *
   * @param classType the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitPredicate the list of {@code Pattern} objects for omitting methods, may be null
   * @param accessibilityPredicate the predicate for test accessibility
   * @param operationSpecifications the specifications (pre/post/throws-conditions)
   */
  public OperationExtractor(
      ClassOrInterfaceType classType,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitPredicate,
      AccessibilityPredicate accessibilityPredicate,
      SpecificationCollection operationSpecifications) {
    this.classType = classType;
    this.reflectionPredicate = reflectionPredicate;
    this.omitPredicate = omitPredicate;
    this.accessibilityPredicate = accessibilityPredicate;
    this.operationSpecifications = operationSpecifications;
  }

  /**
   * Creates a visitor object that collects the {@link TypedOperation} objects corresponding to
   * members of the class satisfying the given accessibility and reflection predicates.
   *
   * @param classType the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param accessibilityPredicate the predicate for test accessibility
   */
  public OperationExtractor(
      ClassOrInterfaceType classType,
      ReflectionPredicate reflectionPredicate,
      AccessibilityPredicate accessibilityPredicate) {
    this(
        classType,
        reflectionPredicate,
        OmitMethodsPredicate.NO_OMISSION,
        accessibilityPredicate,
        null);
  }

  /**
   * Creates a visitor object that collects the {@link TypedOperation} objects corresponding to
   * members of the class satisfying the given accessibility and reflection predicates.
   *
   * @param classType the declaring class for collected operations
   * @param reflectionPredicate the reflection predicate
   * @param omitPredicate the list of {@code Pattern} objects for omitting methods, may be null
   * @param accessibilityPredicate the predicate for test accessibility
   */
  public OperationExtractor(
      ClassOrInterfaceType classType,
      ReflectionPredicate reflectionPredicate,
      OmitMethodsPredicate omitPredicate,
      AccessibilityPredicate accessibilityPredicate) {
    this(classType, reflectionPredicate, omitPredicate, accessibilityPredicate, null);
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
                "Type %s for operation %s is not a subtype of an instantiation of declaring class"
                    + " of method %s",
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
  private void checkSubTypes(TypedClassOperation operation) {
    ClassOrInterfaceType declaringType = operation.getDeclaringType();
    if (!classType.isSubtypeOf(declaringType)) {
      throw new RandoopBug(
          String.format(
              "Incompatible receiver type for operation %s:%n  %s%nis not a subtype of%n  %s",
              operation,
              StringsPlume.toStringAndClass(classType),
              StringsPlume.toStringAndClass(declaringType)));
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
      Log.logPrintln("OperationExtractor.visit: constructor=" + constructor);
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
    checkSubTypes(operation);
    if (!omitPredicate.shouldOmit(operation)) {
      if (operationSpecifications != null) {
        ExecutableSpecification execSpec =
            operationSpecifications.getExecutableSpecification(constructor);
        if (!execSpec.isEmpty()) {
          operation.setExecutableSpecification(execSpec);
        }
      }
      if (debug) {
        Log.logPrintln(
            "OperationExtractor.visit: add operation " + StringsPlume.toStringAndClass(operation));
      }
      operations.add(operation);
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
      Log.logPrintln("OperationExtractor.visit: method=" + method);
    }
    if (!reflectionPredicate.test(method)) {
      return;
    }
    TypedClassOperation operation = instantiateTypes(TypedOperation.forMethod(method));
    if (debug) {
      Log.logPrintln("OperationExtractor.visit: operation=" + operation);
    }
    checkSubTypes(operation);

    if (operation.isStatic()) {
      // If this classType inherits this static method, but declaring class is not public, then
      // consider method to have classType as declaring class.
      int declaringClassMods =
          method.getDeclaringClass().getModifiers() & Modifier.classModifiers();
      if (!Modifier.isPublic(declaringClassMods)) {
        operation = operation.getOperationForType(classType);
        if (debug) {
          Log.logPrintln("OperationExtractor.visit: operation changed to " + operation);
        }
      }
    }

    // The declaring type of the method is not necessarily the classType, but may want to omit
    // method in classType. So, create operation with the classType as declaring type for omit
    // search.
    if (omitPredicate.shouldOmit(operation.getOperationForType(classType))) {
      Log.logPrintln("omitPreditate omits " + operation.getOperationForType(classType));
      return;
    }

    if (operationSpecifications != null) {
      ExecutableSpecification execSpec = operationSpecifications.getExecutableSpecification(method);
      if (!execSpec.isEmpty()) {
        operation.setExecutableSpecification(execSpec);
      }
    }
    if (debug) {
      Log.logPrintln("OperationExtractor.visit: add operation " + operation);
    }
    operations.add(operation);
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
    if (!accessibilityPredicate.isAccessible(field.getDeclaringClass())) {
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
    checkSubTypes(getter);
    if (getter != null) {
      operations.add(getter);
    }
    if (!Modifier.isFinal(mods)) {
      TypedClassOperation operation =
          instantiateTypes(TypedOperation.createSetterForField(field, declaringType));
      if (operation != null) {
        operations.add(operation);
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
    operations.add(operation);
  }

  /**
   * Returns the {@link TypedOperation} objects collected for {@link #classType}.
   *
   * <p>Should be called after all members of the class are visited.
   *
   * @return the collection of operations collected for the class
   */
  public Collection<TypedOperation> getOperations() {
    return operations;
  }
}
