package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import randoop.field.AccessibleField;
import randoop.operation.CallableOperation;
import randoop.operation.ConcreteOperation;
import randoop.operation.ConstructorCall;
import randoop.operation.EnumConstant;
import randoop.operation.FieldGet;
import randoop.operation.FieldSet;
import randoop.operation.GenericOperation;
import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.types.ConcreteSimpleType;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeTuple;
import randoop.types.GeneralType;
import randoop.types.GenericClassType;
import randoop.types.GenericType;
import randoop.types.GenericTypeTuple;
import randoop.util.MultiMap;

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

  /** The set of concrete class types encountered */
  private final Set<ConcreteType> classTypes;

  /** The map of generic types to operations */
  private final MultiMap<GenericType, GenericOperation> genericClassTypes;

  /** The set of generic operations (methods and constructors) encountered */
  private final Set<GenericOperation> genericOperations;

  /** The set of concrete operations encountered */
  private Set<ConcreteOperation> operations;

  /** The current class type */
  private GeneralType classType;

  /**
   * Creates a visitor object that collects Operation objects corresponding to
   * class members visited by {@link ReflectionManager}. Stores
   * {@link Operation} objects in an ordered collection to ensure they are
   * strictly ordered once flattened to a list. This is needed to guarantee
   * determinism between Randoop runs with the same classes and parameters.
   */
  public OperationExtractor(Set<ConcreteType> classTypes, Set<ConcreteOperation> operations, MultiMap<GenericType, GenericOperation> genericClassTypes, Set<GenericOperation> genericOperations) {
    this.classTypes = classTypes;
    this.operations = operations;
    this.genericClassTypes = genericClassTypes;
    this.genericOperations = genericOperations;
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
    GenericTypeTuple inputTypes = getInputTypes(c.getGenericParameterTypes());
    createTypedOperation(op, classType, inputTypes, classType);
  }

  /**
   * Creates a {@link MethodCall} object for the {@link Method}.
   *
   * @param method
   *          a {@link Method} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Method method) {
    assert method.getDeclaringClass().equals(classType.getRuntimeClass())
            : "classType " + classType + " and declaring class " + method.getDeclaringClass().getName() + " should be same";

    MethodCall op = new MethodCall(method);
    GenericTypeTuple inputTypes;
    if (! Modifier.isStatic(method.getModifiers() & Modifier.methodModifiers())) {
      inputTypes = getInputTypes(classType, method.getGenericParameterTypes());
    } else {
      inputTypes = getInputTypes(method.getGenericParameterTypes());
    }
    GeneralType outputType = GeneralType.forType(method.getGenericReturnType());

    createTypedOperation(op, classType, inputTypes, outputType);
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
    assert field.getDeclaringClass().equals(classType.getRuntimeClass())
            : "classType " + classType + " and declaring class " + field.getDeclaringClass().getName() + " should be same";

    GeneralType fieldType = GeneralType.forType(field.getGenericType());
    List<GeneralType> setInputTypeList = new ArrayList<>();
    List<GeneralType> getInputTypeList = new ArrayList<>();

    AccessibleField accessibleField = new AccessibleField(field);

    if (! accessibleField.isStatic()) {
      getInputTypeList.add(classType);
      setInputTypeList.add(classType);
    }

    createTypedOperation(new FieldGet(accessibleField), classType, new GenericTypeTuple(getInputTypeList), fieldType);
    if (! accessibleField.isFinal()) {
      setInputTypeList.add(fieldType);
      createTypedOperation(new FieldSet(accessibleField), classType, new GenericTypeTuple(setInputTypeList), ConcreteType.VOID_TYPE);
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
    assert e.getDeclaringClass().equals(classType.getRuntimeClass())
            : "classType " + classType + " and declaring class " + e.getDeclaringClass().getName() + " should be same";
    assert ! classType.isGeneric() : "type of enum class cannot be generic";
    EnumConstant op = new EnumConstant(e);
    createTypedOperation(op, classType, new GenericTypeTuple(), classType);
  }

  @Override
  public void visitBefore(Class<?> c) {
    if (c.getTypeParameters().length > 0) { // c is generic
      classType = new GenericClassType(c);
    } else {
      ConcreteType type = new ConcreteSimpleType(c);
      classTypes.add(type);
      classType = type;
    }
  }

  @Override
  public void visitAfter(Class<?> c) {
    // nothing to do here
  }

  /**
   * Creates a {@link randoop.operation.TypedOperation} for the given operation with type information,
   * and then saves it to the appropriate grouping.
   * Classifies the operation based on whether the declaring type, input types, or output type are
   * generic:
   * <ul>
   *   <li>If the declaring type is generic, then saved to the map for generic types.</li>
   *   <li>If at least one of input or output types are generic then saved to the set of generic operations.</li>
   *   <li>If none are generic then it is saved to the set of concrete operations.</li>
   * </ul>
   *
   * @param op  the operation
   * @param declaringType  the declaring type for the operation
   * @param inputTypes  the types of inputs to operation
   * @param outputType  the output type of operation
   */
  private void createTypedOperation(CallableOperation op, GeneralType declaringType, GenericTypeTuple inputTypes, GeneralType outputType) {
    if (declaringType.isGeneric()) {
      GenericOperation genericOp = new GenericOperation(op, declaringType, inputTypes, outputType);
      genericClassTypes.add((GenericType)classType, genericOp);
    } else if (inputTypes.isGeneric() || outputType.isGeneric()) {
      assert op.isConstructorCall() || op.isMethodCall()
              : "expected either constructor or method call, got " + op.toString();
      GenericOperation genericOp = new GenericOperation(op, declaringType, inputTypes, outputType);
      genericOperations.add(genericOp);
    } else {
      ConcreteType concreteClassType = (ConcreteType)declaringType;
      ConcreteTypeTuple concreteInputTypes = inputTypes.makeConcrete();
      ConcreteType concreteOutputType = (ConcreteType)outputType;
      ConcreteOperation concreteOp = new ConcreteOperation(op, concreteClassType, concreteInputTypes, concreteOutputType);
      operations.add(concreteOp);
    }
  }

  /**
   * Creates input type tuple for an operator that does not require declaring class as first input.
   * This includes constructor and static method calls.
   * @see #getInputTypes(GeneralType, Type[])
   *
   * @param genericParameterTypes  the array of reflective generic parameter types of operation
   * @return the input tuple for the given types and type variables
   */
  private GenericTypeTuple getInputTypes(Type[] genericParameterTypes) {
    return getInputTypes(null, genericParameterTypes);
  }

  /**
   * Creates the input type tuple for an operator given the declaring type, parameter types, and
   * type parameters.
   * If the declaring type is non-null, it is placed at the beginning of the generated tuple to
   * indicate that the operator requires an instance of the declaring class.
   *
   * @param declaringType  the declaring type for the operation
   * @param genericParameterTypes  the array of reflective generic parameter types of the operation
   * @return the input tuple for the given types and type variables.
   */
  private GenericTypeTuple getInputTypes(GeneralType declaringType, Type[] genericParameterTypes) {
    List<GeneralType> paramTypes = new ArrayList<>();

    if (declaringType != null) {
      paramTypes.add(declaringType);
    }

    for (Type t : genericParameterTypes) {
      paramTypes.add(GeneralType.forType(t));
    }

    // XXX I'm flying dangerously here -- not really sure what is going to happen when have generic method/constructor

    return new GenericTypeTuple(paramTypes);
  }
}
