package randoop.operation;

import randoop.types.GeneralType;
import randoop.types.GenericTypeTuple;
import randoop.types.Substitution;

public class GenericFieldSet extends GenericOperation {

  public GenericFieldSet(GenericTypeTuple inputTypes) {
    super(inputTypes, GeneralType.forClass(void.class));
    // TODO Auto-generated constructor stub
  }

  @Override
  public GeneralType getDeclaringType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FieldSet instantiate(Substitution substitution) {
    // TODO Auto-generated method stub
    return null;
  }

}
