package randoop.reflection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import randoop.operation.CallableOperation;
import randoop.operation.ConcreteOperation;
import randoop.operation.GenericOperation;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeTuple;
import randoop.types.GeneralType;
import randoop.types.GenericType;
import randoop.types.GenericTypeTuple;

/**
 * {@code TypeOperationManager} creates and categorizes types and typed operations collected by
 * visiting classes and class members.
 * The class also provides basic translation methods for input type tuples.
 *
 * @see OperationParseVisitor
 * @see OperationExtractor
 */
class TypedOperationManager {

  private final ModelCollections collections;

  TypedOperationManager(ModelCollections collections) {
    this.collections = collections;
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
  void createTypedOperation(CallableOperation op, GeneralType declaringType, GenericTypeTuple inputTypes, GeneralType outputType) {
    if (declaringType.isGeneric()) {
      GenericOperation genericOp = new GenericOperation(op, declaringType, inputTypes, outputType);
      collections.addGenericClassType((GenericType)declaringType, genericOp);
    } else if (inputTypes.isGeneric() || outputType.isGeneric()) {
      assert op.isConstructorCall() || op.isMethodCall()
              : "expected either constructor or method call, got " + op.toString();
      GenericOperation genericOp = new GenericOperation(op, declaringType, inputTypes, outputType);
      collections.addGenericOperation(genericOp);
    } else {
      ConcreteType concreteClassType = (ConcreteType)declaringType;
      ConcreteTypeTuple concreteInputTypes = inputTypes.makeConcrete();
      ConcreteType concreteOutputType = (ConcreteType)outputType;
      ConcreteOperation concreteOp = new ConcreteOperation(op, concreteClassType, concreteInputTypes, concreteOutputType);
      collections.addConcreteOperation(concreteOp);
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
  GenericTypeTuple getInputTypes(Type[] genericParameterTypes) {
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
  GenericTypeTuple getInputTypes(GeneralType declaringType, Type[] genericParameterTypes) {
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

  GeneralType getClassType(Class<?> c) {
    GeneralType classType = GeneralType.forClass(c);
    if (classType instanceof ConcreteType) {
      collections.addConcreteClassType((ConcreteType)classType);
    }
    return classType;
  }
}
