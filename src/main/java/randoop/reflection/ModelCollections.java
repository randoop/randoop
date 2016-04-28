package randoop.reflection;

import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ParameterizedType;

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
   * @param type  the {@link ClassOrInterfaceType} to add to the class types collection
   */
  public void addConcreteClassType(ClassOrInterfaceType type) { }

  /**
   * Adds a generic type to the collection.
   *
   * @param type  the {@link ParameterizedType} to add to the class types collection
   */
  void addGenericClassType(ParameterizedType type) { }

  /**
   * Adds an operation to a generic type to the generic class type collection.
   *
   * @param declaringType  the type
   * @param operation  the operation
   */
  public void addOperationToGenericType(ParameterizedType declaringType, TypedOperation operation) { }

  /**
   * Adds a generic operation from a concrete declaring type.
   *
   * @param declaringType  the concrete declaring type
   * @param operation  the generic operation
   */
  public void addGenericOperation(ClassOrInterfaceType declaringType, TypedOperation operation) { }

  /**
   * Adds a concrete operation from a concrete declaring type.
   *
   * @param declaringType  the concrete declaring type
   * @param operation  the generic operation
   */
  public void addConcreteOperation(ClassOrInterfaceType declaringType, TypedOperation operation) { }

}
