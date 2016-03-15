package randoop.operation;

import randoop.types.GeneralType;
import randoop.types.GenericType;
import randoop.types.GenericTypeTuple;
import randoop.types.Substitution;

public class GenericConstructorCall extends GenericOperation {

  public GenericConstructorCall(GenericTypeTuple inputTypes, GenericType outputType) {
    super(inputTypes, outputType);
    // TODO Auto-generated constructor stub
  }

  @Override
  public GeneralType getDeclaringType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConstructorCall instantiate(Substitution substitution) {
    // TODO Auto-generated method stub
    return null;
  }

}
