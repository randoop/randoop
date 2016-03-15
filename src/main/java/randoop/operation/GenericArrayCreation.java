package randoop.operation;

import java.util.List;

import randoop.sequence.Variable;
import randoop.types.GeneralType;
import randoop.types.GenericType;
import randoop.types.GenericTypeTuple;
import randoop.types.Substitution;

public class GenericArrayCreation extends GenericOperation {

  public GenericArrayCreation(GenericTypeTuple inputTypes, GenericType outputType) {
    super(inputTypes, outputType);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    // TODO Auto-generated method stub

  }

  @Override
  public String toParseableString() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public GeneralType getDeclaringType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArrayCreation instantiate(Substitution substitution) {
    // TODO Auto-generated method stub
    return null;
  }

}
