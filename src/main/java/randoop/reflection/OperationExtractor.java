package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import randoop.field.AccessibleField;
import randoop.operation.ConstructorCall;
import randoop.operation.EnumConstant;
import randoop.operation.FieldGet;
import randoop.operation.FieldSet;
import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.types.ConcreteType;
import randoop.types.GeneralType;
import randoop.types.GenericTypeTuple;

/**
 * OperationExtractor is a {@link ClassVisitor} that creates a collection of
 * {@link Operation} objects through its visit methods as called by
 * {@link ReflectionManager#apply(Class)}.
 *
 * @see ReflectionManager
 * @see ClassVisitor
 *
 */
public class OperationExtractor implements ClassVisitor {

  private final TypedOperationManager manager;

  /** The current class type */
  private GeneralType classType;

  /**
   * Creates a visitor object that collects Operation objects corresponding to
   * class members visited by {@link ReflectionManager}. Stores
   * {@link Operation} objects in an ordered collection to ensure they are
   * strictly ordered once flattened to a list. This is needed to guarantee
   * determinism between Randoop runs with the same classes and parameters.
   */
  public OperationExtractor(TypedOperationManager manager) {
    this.manager = manager;
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
    ConstructorCall op = new ConstructorCall(c);
    GenericTypeTuple inputTypes = manager.getInputTypes(c.getGenericParameterTypes());
    manager.createTypedOperation(op, classType, inputTypes, classType);
  }

  /**
   * Creates a {@link MethodCall} object for the {@link Method}.
   *
   * @param method
   *          a {@link Method} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Method method) {
    assert method.getDeclaringClass().isAssignableFrom(classType.getRuntimeClass())
            : "classType " + classType + " should be assignable to declaring class " + method.getDeclaringClass().getName();

    MethodCall op = new MethodCall(method);
    GenericTypeTuple inputTypes;
    if (! Modifier.isStatic(method.getModifiers() & Modifier.methodModifiers())) {
      inputTypes = manager.getInputTypes(classType, method.getGenericParameterTypes());
    } else {
      inputTypes = manager.getInputTypes(method.getGenericParameterTypes());
    }
    GeneralType outputType = GeneralType.forType(method.getGenericReturnType());

    manager.createTypedOperation(op, classType, inputTypes, outputType);
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

    GeneralType fieldType = GeneralType.forType(field.getGenericType());
    List<GeneralType> setInputTypeList = new ArrayList<>();
    List<GeneralType> getInputTypeList = new ArrayList<>();

    AccessibleField accessibleField = new AccessibleField(field, classType);

    if (! accessibleField.isStatic()) {
      getInputTypeList.add(classType);
      setInputTypeList.add(classType);
    }

    manager.createTypedOperation(new FieldGet(accessibleField), classType, new GenericTypeTuple(getInputTypeList), fieldType);
    if (! accessibleField.isFinal()) {
      setInputTypeList.add(fieldType);
      manager.createTypedOperation(new FieldSet(accessibleField), classType, new GenericTypeTuple(setInputTypeList), ConcreteType.VOID_TYPE);
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
    ConcreteType enumType = ConcreteType.forClass(e.getDeclaringClass());
    assert ! enumType.isGeneric() : "type of enum class cannot be generic";
    EnumConstant op = new EnumConstant(e);
    manager.createTypedOperation(op, enumType, new GenericTypeTuple(), enumType);
  }

  @Override
  public void visitBefore(Class<?> c) {
    classType = manager.getClassType(c);
  }

  @Override
  public void visitAfter(Class<?> c) {
    // nothing to do here
  }


}
