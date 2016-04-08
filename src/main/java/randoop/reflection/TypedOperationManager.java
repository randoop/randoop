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
import randoop.types.RandoopTypeException;

/**
 * {@code TypeOperationManager} creates and categorizes types and typed operations collected by
 * visiting classes and class members.
 * The class also provides basic translation methods for input type tuples.
 *
 * @see OperationExtractor
 */
public class TypedOperationManager {

  private final ModelCollections collections;

  public TypedOperationManager(ModelCollections collections) {
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
  public void createTypedOperation(CallableOperation op, GeneralType declaringType, GenericTypeTuple inputTypes, GeneralType outputType) {
    if (declaringType.isGeneric()) {
      GenericType genericClassType = (GenericType)declaringType;
      GenericOperation genericOp = new GenericOperation(op, declaringType, inputTypes, outputType);
      collections.addGenericOperation(genericClassType, genericOp);
    } else {
      ConcreteType concreteClassType = (ConcreteType)declaringType;
      if (inputTypes.isGeneric() || outputType.isGeneric()) {
        assert op.isConstructorCall() || op.isMethodCall()
                : "expected either constructor or method call, got " + op.toString();
        GenericOperation genericOp = new GenericOperation(op, declaringType, inputTypes, outputType);
        collections.addGenericOperation(concreteClassType, genericOp);
      } else {
        ConcreteTypeTuple concreteInputTypes = inputTypes.makeConcrete();
        ConcreteType concreteOutputType = (ConcreteType)outputType;
        ConcreteOperation concreteOp = new ConcreteOperation(op, concreteClassType, concreteInputTypes, concreteOutputType);
        collections.addConcreteOperation(concreteClassType, concreteOp);
      }
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
  GenericTypeTuple getInputTypes(Type[] genericParameterTypes) throws RandoopTypeException {
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
  GenericTypeTuple getInputTypes(GeneralType declaringType, Type[] genericParameterTypes) throws RandoopTypeException {
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

  public GeneralType getClassType(Class<?> c) throws RandoopTypeException {
    GeneralType classType;
    if (c.getTypeParameters().length > 0) {
      GenericType genericClassType = GenericType.forClass(c);
      collections.addGenericClassType(genericClassType);
      classType = genericClassType;
    } else {
      ConcreteType concreteClassType = ConcreteType.forClass(c);
      collections.addConcreteClassType(concreteClassType);
      classType = concreteClassType;
    }
    return classType;
  }
}
