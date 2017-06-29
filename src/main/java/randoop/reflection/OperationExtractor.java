package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import randoop.operation.ConstructorCall;
import randoop.operation.EnumConstant;
import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.InstantiatedType;
import randoop.types.JavaTypes;
import randoop.types.NonParameterizedType;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeTuple;
import randoop.util.Log;

/**
 * OperationExtractor is a {@link ClassVisitor} that creates a collection of {@link Operation}
 * objects for a particular {@link ClassOrInterfaceType} through its visit methods as called by
 * {@link ReflectionManager#apply(Class)}. Allows types of operations of an {@link InstantiatedType}
 * to be instantiated using the substitution of the type.
 *
 * @see ReflectionManager
 * @see ClassVisitor
 */
public class OperationExtractor extends DefaultClassVisitor {

  /** The predicate that implements reflection policy for collecting operations */
  private final ReflectionPredicate predicate;

  /** The predicate to test visibility */
  private final VisibilityPredicate visibilityPredicate;

  /** The collection of operations */
  private final Collection<TypedOperation> operations;

  /** The class type of the declaring class for the collected operations */
  private ClassOrInterfaceType classType;

  /** The list of {@code Pattern} objects to omit matching operations */
  private List<Pattern> omitPatterns;

  /**
   * Creates a visitor object that collects the {@link TypedOperation} objects corresponding to
   * members of the class type and satisfying the given predicate.
   *
   * @param classType the declaring classtype for collected operations
   * @param operations the collection of operations
   * @param predicate the reflection predicate
   * @param visibilityPredicate the predicate for test visibility
   */
  public OperationExtractor(
      ClassOrInterfaceType classType,
      Collection<TypedOperation> operations,
      ReflectionPredicate predicate,
      List<Pattern> omitPatterns,
      VisibilityPredicate visibilityPredicate) {
    this.classType = classType;
    this.operations = operations;
    this.predicate = predicate;
    this.visibilityPredicate = visibilityPredicate;
    if (omitPatterns != null) {
      this.omitPatterns = omitPatterns;
    } else {
      this.omitPatterns = new ArrayList<>();
    }
  }

  /**
   * Creates a visitor object that collects the {@link TypedOperation} objects corresponding to
   * members of the class type and satisfying the given predicate.
   *
   * @param classType the declaring classtype for collected operation
   * @param operations the collection of operations
   * @param predicate the reflection predicate
   * @param visibilityPredicate the predicate for testing visibility
   */
  public OperationExtractor(
      ClassOrInterfaceType classType,
      Collection<TypedOperation> operations,
      ReflectionPredicate predicate,
      VisibilityPredicate visibilityPredicate) {
    this(classType, operations, predicate, new ArrayList<Pattern>(), visibilityPredicate);
  }

  /**
   * Adds an operation to the collection of this extractor. If the declaring class type is an {@link
   * InstantiatedType}, then the substitution for that class is applied to the types of the
   * operation, and this instantiated operation is returned.
   *
   * @param operation the {@link TypedClassOperation}
   */
  private void addOperation(TypedClassOperation operation) {
    if (operation != null) {
      if (!classType.isGeneric() && operation.getDeclaringType().isGeneric()) {
        // if the declaring class is generic, then need substitution to instantiate type arguments
        Substitution<ReferenceType> substitution =
            classType.getInstantiatingSubstitution(operation.getDeclaringType());
        if (substitution == null) { //no unifying substitution found
          return;
        }
        operation = operation.apply(substitution);
        if (operation == null) { //will be null if instantiation failed
          return;
        }
      }
      operations.add(operation);
    }
  }

  /**
   * Creates a {@link ConstructorCall} object for the {@link Constructor}.
   *
   * @param constructor a {@link Constructor} object to be represented as an {@link Operation}
   */
  @Override
  public void visit(Constructor<?> constructor) {
    assert constructor.getDeclaringClass().equals(classType.getRuntimeClass())
        : "classType "
            + classType
            + " and declaring class "
            + constructor.getDeclaringClass().getName()
            + " should be same";
    if (!predicate.test(constructor)) {
      return;
    }
    TypedClassOperation operation = TypedOperation.forConstructor(constructor);
    if (!omit(operation)) {
      addOperation(operation);
    }
  }

  /**
   * Creates a {@link MethodCall} object for the {@link Method}.
   *
   * @param method a {@link Method} object to be represented as an {@link Operation}
   */
  @Override
  public void visit(Method method) {
    if (!predicate.test(method)) {
      return;
    }
    TypedClassOperation operation = TypedOperation.forMethod(method);
    if (classType.isSubtypeOf(operation.getDeclaringType()) && operation.isStatic()) {
      /*
       * if this classType inherits this static method, but declaring class is not public, then
       * consider method to have classType as declaring class
       */
      int declaringClassMods =
          method.getDeclaringClass().getModifiers() & Modifier.classModifiers();
      if (!Modifier.isPublic(declaringClassMods)) {
        operation = getOperationForType(operation, classType);
      }
    }
    if (!omit(operation) && !omit(classType, operation, method)) {
      addOperation(operation);
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
    if (!predicate.test(field)) {
      return;
    }

    ClassOrInterfaceType declaringType = ClassOrInterfaceType.forClass(field.getDeclaringClass());

    int mods = field.getModifiers() & Modifier.fieldModifiers();
    if (!visibilityPredicate.isVisible(field.getDeclaringClass())) {
      if (Modifier.isStatic(mods) && Modifier.isFinal(mods)) {
        //XXX this is a stop-gap to handle potentially ambiguous inherited constants
        /* An static final field of a non-public class may be accessible via a subclass, but only
         * if the field is not ambiguously inherited in the subclass. Without knowing for sure
         * whether there are two inherited fields with the same name, we cannot decide which case
         * is presented. So, assuming that there is an ambiguity and bailing on type.
         */
        return;
      }
      if (!(declaringType.isGeneric() && classType.isInstantiationOf(declaringType))) {
        declaringType = classType;
      }
    }

    addOperation(TypedOperation.createGetterForField(field, declaringType));
    if (!(Modifier.isFinal(mods))) {
      addOperation(TypedOperation.createSetterForField(field, declaringType));
    }
  }

  /**
   * Creates a {@link EnumConstant} object for the {@link Enum}.
   *
   * @param e an {@link Enum} object to be represented as an {@link Operation}
   */
  @Override
  public void visit(Enum<?> e) {
    ClassOrInterfaceType enumType = new NonParameterizedType(e.getDeclaringClass());
    assert !enumType.isGeneric() : "type of enum class cannot be generic";
    EnumConstant op = new EnumConstant(e);
    addOperation(new TypedClassOperation(op, enumType, new TypeTuple(), enumType));
  }

  /**
   * Indicates whether an omit patterns matches the {@link TypedClassOperation#getRawSignature()}
   * for the given operation.
   *
   * @param operation the constructor or method call to match against the omit patterns of this
   *     extractor
   * @return true if the signature matches an omit pattern, and false otherwise.
   */
  private boolean omit(TypedClassOperation operation) {
    if (omitPatterns.isEmpty()) {
      return false;
    }
    String signature = operation.getRawSignature();
    for (Pattern pattern : omitPatterns) {
      boolean result = pattern.matcher(signature).find();
      if (Log.isLoggingOn()) {
        Log.logLine(
            String.format(
                "Comparing '%s' against pattern '%s' = %b%n", signature, pattern, result));
      }
      if (result) {
        return true;
      }
    }
    return false;
  }

  /**
   * Indicates whether an omit pattern matches the raw signature of the method or the same method in
   * a supertype.
   *
   * @param classType the
   * @param operation the operation for the method
   * @param method the reflection object for the method
   * @return true if the signature of the method in the current class or a super class matches an
   *     omit pattern, false otherwise
   */
  private boolean omit(
      ClassOrInterfaceType classType, TypedClassOperation operation, Method method) {
    if (omitPatterns.isEmpty()) {
      return false;
    }

    ClassOrInterfaceType declaringType = operation.getDeclaringType();
    if (!classType.equals(declaringType)) {

      // search from classType to declaring type
      ClassOrInterfaceType type = classType;

      while (type.isSubtypeOf(declaringType)) {
        // this conversion is safe because we known all subtypes of the declaring class of the
        // operation have the method
        TypedClassOperation superTypeOperation = getOperationForType(operation, type);
        if (omit(superTypeOperation)) {
          return true;
        }
        type = type.getSuperclass();
      }
    }

    // otherwise, search super types of declaring type that have the method as a member
    // for any that match pattern
    ClassOrInterfaceType supertype = classType.getSuperclass();
    if (supertype != null
        && omitSupertypeMethod(method.getName(), method.getParameterTypes(), supertype)) {
      return true;
    }

    for (ClassOrInterfaceType interfaceType : classType.getInterfaces()) {
      if (omitSupertypeMethod(method.getName(), method.getParameterTypes(), interfaceType)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Indicates whether an omit pattern matches the raw signature of the method from the supertype.
   *
   * <p>This method is necessary when searching for matches from supertypes of the declaring class
   * of an operation.
   *
   * @see #omit(ClassOrInterfaceType, TypedClassOperation, Method)
   * @param methodName the method name
   * @param parameterTypes the {@code Class} types of the method parameters
   * @param supertype the supertype class to search
   * @return true if the supertype has a matching method, false otherwise
   */
  private boolean omitSupertypeMethod(
      String methodName, Class<?>[] parameterTypes, ClassOrInterfaceType supertype) {
    TypedClassOperation operation;
    if (!supertype.equals(JavaTypes.OBJECT_TYPE)) {
      Method superclassMethod = getMethod(methodName, parameterTypes, supertype.getRuntimeClass());
      if (superclassMethod != null) {
        operation = TypedOperation.forMethod(superclassMethod);
        if (operation != null) {
          if (omit(operation)) {
            return true;
          }
          if (omit(supertype, operation, superclassMethod)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Creates an operation with the same name, input types and output type as the given operation,
   * but having the given type as the owning class. This is a valid object as long as type is a
   * subtype of the declaring class of the operation.
   *
   * @param operation the original operation, non-null
   * @param type a subtype of the declaring class of the operation to substitute into the operation,
   *     non-null
   * @return a new operation with {@code type} substituted for the declaring type of {@code
   *     operation}
   */
  private static TypedClassOperation getOperationForType(
      TypedClassOperation operation, ClassOrInterfaceType type) {
    return new TypedClassOperation(
        operation.getOperation(), type, operation.getInputTypes(), operation.getOutputType());
  }

  /**
   * Returns the {@code java.lang.reflect.Method} with the name and arguments in the given class.
   *
   * @param methodName the method name
   * @param parameterTypes the parameter types of the method
   * @param typeClass the {@code Class} of the method
   * @return the {@code Method} object with the name and parameter types from {@code typeClass},
   *     null if there is no such method
   */
  private static Method getMethod(
      String methodName, Class<?>[] parameterTypes, Class<?> typeClass) {
    try {
      return typeClass.getMethod(methodName, parameterTypes);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }
}
