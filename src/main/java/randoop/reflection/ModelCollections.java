package randoop.reflection;

import java.lang.reflect.Constructor;

import randoop.operation.ConcreteOperation;
import randoop.operation.GenericOperation;
import randoop.types.ConcreteType;
import randoop.types.GenericClassType;
import randoop.types.GenericType;

/**
 * Abstract class for representing collections of types and operations for a set of classes.
 * Includes:
 * <ul>
 *   <li>concrete class types,</li>
 *   <li>generic class types and corresponding operations,</li>
 *   <li>generic operations (aka, generic methods and constructors), and</li>
 *   <li>concrete operations.</li>
 * </ul>
  * Implementing classes may not implement all of these corresponding methods.
 */
public abstract class ModelCollections {

  /**
   * Adds a concrete type to the collection.
   *
   * @param type  the {@link ConcreteType} to add to the class types collection
   */
  public void addConcreteClassType(ConcreteType type) { }

  /**
   * Adds a generic type to the collection.
   *
   * @param type  the {@link GenericClassType} to add to the class types collection
   */
  public void addGenericClassType(GenericClassType type) { }

  /**
   * Adds an operation to a generic type to the generic class type collection.
   *
   * @param declaringType  the type
   * @param operation  the operation
   */
  public void addGenericOperation(GenericClassType declaringType, GenericOperation operation) { }

  /**
   * Adds a generic operation from a concrete declaring type.
   *
   * @param declaringType  the concrete declaring type
   * @param operation  the generic operation
   */
  public void addGenericOperation(ConcreteType declaringType, GenericOperation operation) { }

  /**
   * Adds a concrete operation from a generic declaring type.
   *
   * @param declaringType  the concrete declaring type
   * @param operation  the generic operation
   */
  public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) { }

}
