package randoop.reflection;

import randoop.operation.ConcreteOperation;
import randoop.operation.GenericOperation;
import randoop.types.ConcreteType;
import randoop.types.GenericType;

/**
 * Interface for representing collections of model types and operations, including
 * <ul>
 *   <li>concrete class types,</li>
 *   <li>generic class types and corresponding operations,</li>
 *   <li>generic operations (aka, generic methods and constructors), and</li>
 *   <li>concrete operations.</li>
 * </ul>
  * Implementing classes may not implement all of the corresponding methods.
 */
public interface ModelCollections {

  /**
   * Adds a concrete type to the collection.
   *
   * @param type  the {@link ConcreteType} to add to the class types collection.
   */
  void addConcreteClassType(ConcreteType type);

  /**
   * Adds an operation to a generic type to the generic class type collection.
   *
   * @param type  the type
   * @param operation  the operation
   */
  void addGenericClassType(GenericType type, GenericOperation operation);

  /**
   *
   * @param operation
   */
  void addGenericOperation(GenericOperation operation);
  void addConcreteOperation(ConcreteOperation operation);
}
