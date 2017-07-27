package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import randoop.operation.ConstructorCall;
import randoop.operation.EnumConstant;
import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.InstantiatedType;
import randoop.types.NonParameterizedType;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeTuple;

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

  /** The type of the declaring class for the collected operations */
  private ClassOrInterfaceType classType;

  /** The predicate to test whether to omit an operation */
  private OmitMethodsPredicate omitPredicate;

  /**
   * Creates a visitor object that collects the {@link TypedOperation} objects corresponding to
   * members of the class satisfying the given visibility and reflection predicates and that don't
   * violate the omit method predicate.
   *
   * @param classType the declaring class for collected operations
   * @param operations the collection of operations, will be side-effected
   * @param predicate the reflection predicate
   * @param omitPredicate the list of {@code Pattern} objects for omitting methods, may be null
   * @param visibilityPredicate the predicate for test visibility
   */
  public OperationExtractor(
      ClassOrInterfaceType classType,
      Collection<TypedOperation> operations,
      ReflectionPredicate predicate,
      OmitMethodsPredicate omitPredicate,
      VisibilityPredicate visibilityPredicate) {
    this.classType = classType;
    this.operations = operations;
    this.predicate = predicate;
    this.visibilityPredicate = visibilityPredicate;
    this.omitPredicate = omitPredicate;
  }

  /**
   * Creates a visitor object that collects the {@link TypedOperation} objects corresponding to
   * members of the class satisfying the given visibility and reflection predicates.
   *
   * @param classType the declaring class for collected operations
   * @param operations the collection of operations, will be side-effected
   * @param predicate the reflection predicate
   * @param visibilityPredicate the predicate for test visibility
   */
  public OperationExtractor(
      ClassOrInterfaceType classType,
      Collection<TypedOperation> operations,
      ReflectionPredicate predicate,
      VisibilityPredicate visibilityPredicate) {
    this(classType, operations, predicate, OmitMethodsPredicate.NO_OMISSION, visibilityPredicate);
  }

  /**
   * Adds an operation to the collection of this extractor. If the declaring class is an {@link
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
    if (!omitPredicate.shouldOmit(operation)) {
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
      // If this classType inherits this static method, but declaring class is not public, then
      // consider method to have classType as declaring class.
      int declaringClassMods =
          method.getDeclaringClass().getModifiers() & Modifier.classModifiers();
      if (!Modifier.isPublic(declaringClassMods)) {
        operation = operation.getOperationForType(classType);
      }
    }
    if (!omitPredicate.shouldOmit(classType, operation, method)) {
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
}
