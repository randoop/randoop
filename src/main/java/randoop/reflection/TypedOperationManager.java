package randoop.reflection;

import randoop.operation.TypedClassOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ParameterizedType;

/**
 * {@code TypeOperationManager}  categorizes types and typed operations collected by
 * visiting classes and class members.
 *
 * @see OperationExtractor
 */
public class TypedOperationManager {

  private final ModelCollections collections;

  public TypedOperationManager(ModelCollections collections) {
    this.collections = collections;
  }

  /**
   * Adds a {@link randoop.operation.TypedClassOperation} to the appropriate collection.
   * Classifies the operation based on whether the declaring type, input types, or output type are
   * generic:
   * <ul>
   *   <li>If the declaring type is generic, then saved to the map for generic types.</li>
   *   <li>If at least one of input or output types are generic then saved to the set of generic operations.</li>
   *   <li>If none are generic then it is saved to the set of concrete operations.</li>
   * </ul>
   *
   * @param operation  the operation
   */
  public void addOperation(TypedClassOperation operation) {
    assert operation != null;
    if (operation.getDeclaringType().isGeneric()) {
      collections.addOperationToGenericType((ParameterizedType) operation.getDeclaringType(), operation);
    } else {
      if (operation.isGeneric()) {
        assert operation.isConstructorCall() || operation.isMethodCall()
                : "expected either constructor or method call, got " + operation.toString();
        collections.addGenericOperation(operation.getDeclaringType(), operation);
      } else {
        collections.addConcreteOperation(operation.getDeclaringType(), operation);
      }
    }
  }

  /**
   * Adds the given class type to either the generic or concrete classes of the model depending
   * on whether the type is generic or not.
   *
   * @param classType  the class type
   */
  public void addClassType(ClassOrInterfaceType classType) {
    if (classType.isGeneric()) {
      collections.addGenericClassType((ParameterizedType)classType);
    } else {
      collections.addConcreteClassType(classType);
    }
  }
}
