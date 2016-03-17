package randoop.operation;

import randoop.types.*;

import java.lang.reflect.Constructor;

public class GenericConstructorCall extends GenericOperation {

  private final Constructor<?> constructor;

  public GenericConstructorCall(Constructor<?> constructor, GeneralType declaringType, GenericTypeTuple inputTypes, GenericType outputType) {
    super(declaringType, inputTypes, outputType);
    this.constructor = constructor;
  }

  @Override
  public ConstructorCall instantiate(Substitution substitution) {
    ConcreteTypeTuple inputTypes = this.getInputTypes().instantiate(substitution);
    ConcreteType outputType = this.getOutputType().instantiate(substitution);
    ConcreteType declaringType = this.getDeclaringType().instantiate(substitution);
    // TODO Auto-generated method stub
    return new ConstructorCall(this.constructor, declaringType, inputTypes, outputType);
  }

}
