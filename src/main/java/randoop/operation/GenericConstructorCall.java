package randoop.operation;

import randoop.types.*;

import java.lang.reflect.Constructor;

/**
 * {@code GenericConstructorCall} represents a (would be) call to a constructor with type parameters.
 * Used as a placeholder in harvesting types before instantiating types are known.
 */
public class GenericConstructorCall extends GenericOperation {

  /** The reflective constructor */
  private final Constructor<?> constructor;

  /**
   * Creates a generic constructor call object for the given constructor with the given types.
   *
   * @param constructor  the reflective constructor object
   * @param declaringType  the type of the class for the constructor
   * @param inputTypes  the input parameters for the constructor
   * @param outputType  the output type of the constructor
     */
  public GenericConstructorCall(Constructor<?> constructor, GeneralType declaringType, GenericTypeTuple inputTypes, GenericType outputType) {
    super(declaringType, inputTypes, outputType);
    this.constructor = constructor;
  }

  /**
   * {@inheritDoc}
   *
   * @return the concrete {@link ConstructorCall} formed by instantiating the type parameters of this
   *         generic constructor
     */
  @Override
  public ConstructorCall instantiate(Substitution substitution) {
    ConcreteTypeTuple inputTypes = this.getInputTypes().instantiate(substitution);
    ConcreteType outputType = this.getOutputType().instantiate(substitution);
    ConcreteType declaringType = this.getDeclaringType().instantiate(substitution);
    // TODO Auto-generated method stub
    return new ConstructorCall(this.constructor, declaringType, inputTypes, outputType);
  }

}
