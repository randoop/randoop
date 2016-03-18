package randoop.operation;

import randoop.types.ConcreteArrayType;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeTuple;
import randoop.types.GenericArrayType;
import randoop.types.GenericType;
import randoop.types.GenericTypeTuple;
import randoop.types.Substitution;

/**
 * {@code GenericArrayCreation} represents an operation that creates a generic
 * array.  Not possible in actual code, this is a placeholder during type harvesting
 * until the type parameters can be instantiated.
 */
public class GenericArrayCreation extends GenericOperation {

  /** The length of the array to be created by this operation */
  private final int length;

  /**
   * Creates a generic array creation operation.
   *
   * @param length  the length of the array to be created by this operation
   * @param arrayType  the type of the array to be created
   * @param inputTypes  the input types needed to create the array
   */
  public GenericArrayCreation(int length, GenericArrayType arrayType, GenericTypeTuple inputTypes) {
    super(arrayType, inputTypes, arrayType);
    this.length = length;
  }


  @Override
  public ArrayCreation instantiate(Substitution substitution) {
    ConcreteArrayType arrayType = (ConcreteArrayType)getDeclaringType().instantiate(substitution);
    ConcreteTypeTuple inputTypes = getInputTypes().instantiate(substitution);
    return new ArrayCreation(length, arrayType, inputTypes);
  }

}
