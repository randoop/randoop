package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
import randoop.types.SimpleClassOrInterfaceType;
import randoop.types.TypeTuple;

/**
 * OperationExtractor is a {@link ClassVisitor} that creates a collection of
 * {@link Operation} objects through its visit methods as called by
 * {@link ReflectionManager#apply(Class)}.
 *
 * @see ReflectionManager
 * @see ClassVisitor
 *
 */
public class OperationExtractor extends DefaultClassVisitor {

  private final TypedOperationManager manager;
  private final ReflectionPredicate predicate;
  private final Stack<ClassOrInterfaceType> typeStack;

  /** The current class type */
  private ClassOrInterfaceType classType;

  /**
   * Creates a visitor object that collects Operation objects corresponding to
   * class members visited by {@link ReflectionManager}. Stores
   * {@link Operation} objects in an ordered collection to ensure they are
   * strictly ordered once flattened to a list. This is needed to guarantee
   * determinism between Randoop runs with the same classes and parameters.
   */
  public OperationExtractor(TypedOperationManager manager, ReflectionPredicate predicate) {
    this.manager = manager;
    this.predicate = predicate;
    this.typeStack = new Stack<>();
    this.classType = null;
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

    manager.addOperation(TypedOperation.forConstructor(c));
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
    if (! predicate.test(method)) {
      return;
    }
    final TypedClassOperation operation = TypedOperation.forMethod(method);
    System.out.println("adding " + operation);
    manager.addOperation(operation);
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

    manager.addOperation(new TypedClassOperation(new FieldGet(accessibleField), classType, new TypeTuple(getInputTypeList), fieldType));
    if (! accessibleField.isFinal()) {
      setInputTypeList.add(fieldType);
      manager.addOperation(new TypedClassOperation(new FieldSet(accessibleField), classType, new TypeTuple(setInputTypeList), ConcreteTypes.VOID_TYPE));
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
    manager.addOperation(new TypedClassOperation(op, enumType, new TypeTuple(), enumType));
  }

  @Override
  public void visitBefore(Class<?> c) {
    typeStack.push(classType);
    if (! predicate.test(c)) {
      return;
    }
    classType = ClassOrInterfaceType.forClass(c);
    manager.addClassType(classType);
  }

  @Override
  public void visitAfter(Class<?> c) {
    assert ! typeStack.isEmpty() : "call to visitAfter not paired with call to visitBefore";
    classType = typeStack.pop();
  }
}
