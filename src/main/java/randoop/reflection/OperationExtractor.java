package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import randoop.field.AccessibleField;
import randoop.operation.ConstructorCall;
import randoop.operation.EnumConstant;
import randoop.operation.FieldGet;
import randoop.operation.FieldSet;
import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;
import randoop.types.InstantiatedType;
import randoop.types.SimpleClassOrInterfaceType;
import randoop.types.TypeTuple;

/**
 * OperationExtractor is a {@link ClassVisitor} that creates a collection of
 * {@link Operation} objects for a particular {@link ClassOrInterfaceType} through its visit
 * methods as called by {@link ReflectionManager#apply(Class)}.
 * Allows types of operations of an {@link InstantiatedType} to be instantiated using the subsitution
 * of the type.
 *
 * @see ReflectionManager
 * @see ClassVisitor
 *
 */
public class OperationExtractor extends DefaultClassVisitor {

  /** The predicate that implements reflection policy for collecting operations */
  private final ReflectionPredicate predicate;

  /** The collection of operations */
  private final Collection<TypedOperation> operations;

  /** The class type of the declaring class for the collected operations */
  private ClassOrInterfaceType classType;

  /**
   * Creates a visitor object that collects Operation objects corresponding to
   * class members visited by {@link ReflectionManager}.
   */
  /**
   * Creates a visitor object that collects the {@link TypedOperation} objects corresponding to
   * members of the class type and satisfying the given predicate.
   *
   * @param classType  the declaring classtype for collected operations
   * @param operations  the collection of operations
   * @param predicate  the reflection predicate
   */
  public OperationExtractor(ClassOrInterfaceType classType, Collection<TypedOperation> operations, ReflectionPredicate predicate) {
    this.classType = classType;
    this.operations = operations;
    this.predicate = predicate;
  }

  /**
   * Adds an operation to the collection of this extractor.
   * If the declaring class type is an {@link InstantiatedType}, then the substitution for that
   * class is applied to the types of the operation, and this instantiated operation is returned.
   *
   * @param operation  the {@link TypedOperation}
   */
  private void addOperation(TypedClassOperation operation) {
    if (classType.isParameterized()) {
      operations.add(operation.apply(((InstantiatedType)classType).getTypeSubstitution()));
    } else  {
      operations.add(operation);
    }
  }

  /**
   * Creates a {@link ConstructorCall} object for the {@link Constructor}.
   *
   * @param c
   *          a {@link Constructor} object to be represented as an
   *          {@link Operation}.
   */
  @Override
  public void visit(Constructor<?> c) {
    assert c.getDeclaringClass().equals(classType.getRuntimeClass())
            : "classType " + classType + " and declaring class " + c.getDeclaringClass().getName() + " should be same";
    if (! predicate.test(c)) {
      return;
    }

    addOperation(TypedOperation.forConstructor(c));
  }
  
  /**
   * Creates a {@link MethodCall} object for the {@link Method}.
   *
   * @param method
   *          a {@link Method} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Method method) {
    if (! predicate.test(method)) {
      return;
    }
    addOperation(TypedOperation.forMethod(method));
  }

  /**
   * Adds the {@link Operation} objects corresponding to getters and setters
   * appropriate to the kind of field.
   *
   * @param field
   *          a {@link Field} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Field field) {
    assert field.getDeclaringClass().isAssignableFrom(classType.getRuntimeClass())
            : "classType " + classType + " should be assignable from " + field.getDeclaringClass().getName();
    if (! predicate.test(field)) {
      return;
    }
    GeneralType fieldType;
    fieldType = GeneralType.forType(field.getGenericType());
    List<GeneralType> setInputTypeList = new ArrayList<>();
    List<GeneralType> getInputTypeList = new ArrayList<>();

    AccessibleField accessibleField = new AccessibleField(field, classType);

    if (! accessibleField.isStatic()) {
      getInputTypeList.add(classType);
      setInputTypeList.add(classType);
    }

    addOperation(new TypedClassOperation(new FieldGet(accessibleField), classType, new TypeTuple(getInputTypeList), fieldType));
    if (! accessibleField.isFinal()) {
      setInputTypeList.add(fieldType);
      addOperation(new TypedClassOperation(new FieldSet(accessibleField), classType, new TypeTuple(setInputTypeList), ConcreteTypes.VOID_TYPE));
    }
  }

  /**
   * Creates a {@link EnumConstant} object for the {@link Enum}.
   *
   * @param e
   *          an {@link Enum} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Enum<?> e) {
    ClassOrInterfaceType enumType = new SimpleClassOrInterfaceType(e.getDeclaringClass());
    assert ! enumType.isGeneric() : "type of enum class cannot be generic";
    EnumConstant op = new EnumConstant(e);
    addOperation(new TypedClassOperation(op, enumType, new TypeTuple(), enumType));
  }

}
