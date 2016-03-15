package randoop.operation;

import randoop.field.AccessibleField;
import randoop.types.ConcreteType;
import randoop.types.GeneralType;
import randoop.types.GenericSimpleType;
import randoop.types.GenericTypeTuple;
import randoop.types.Substitution;

public class GenericFieldGet extends GenericOperation {

  private AccessibleField field;

  public GenericFieldGet(AccessibleField field, GenericSimpleType outputType) {
    super(new GenericTypeTuple(), outputType);
    this.field = field;
  }

  @Override
  public GeneralType getDeclaringType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FieldGet instantiate(Substitution substitution) {
    ConcreteType concreteOutputType = this.getOutputType().instantiate(substitution);
    return new FieldGet(field, concreteOutputType);
  }

}
