package randoop.operation;

import randoop.types.*;

import java.lang.reflect.Method;

/**
 * A {@link GenericMethodCall} is an {@link Operation} that represents
 * a generic placeholder for a {@link MethodCall}.
 */
public class GenericMethodCall extends GenericOperation {

  /** The reflective method object for this operation */
  private final Method method;

  public GenericMethodCall(Method method, GeneralType declaringType, GenericTypeTuple inputTypes, GenericType outputType) {
    super(declaringType, inputTypes, outputType);
    this.method = method;
  }

  /**
   * {@inheritDoc}
   *
   * @return the {@link MethodCall} that instantiates this {@code GenericMethodCall} by the subsitution
     */
  @Override
  public MethodCall instantiate(Substitution substitution) {
    ConcreteTypeTuple inputTypes = this.getInputTypes().instantiate(substitution);
    ConcreteType outputType = this.getOutputType().instantiate(substitution);
    ConcreteType declaringType = this.getDeclaringType().instantiate(substitution);
    return new MethodCall(this.method, declaringType, inputTypes, outputType);
  }

}
