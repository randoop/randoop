package randoop.operation;

import randoop.field.AccessibleField;
import randoop.field.GenericAccessibleField;
import randoop.types.ConcreteType;
import randoop.types.GeneralType;
import randoop.types.GenericSimpleType;
import randoop.types.GenericTypeTuple;
import randoop.types.Substitution;

public class GenericFieldGet extends GenericOperation {

  private GenericAccessibleField field;

  public GenericFieldGet(GenericAccessibleField field) {
    super(field.getDeclaringType(), field.getAccessTypes(), field.getType());
    this.field = field;
  }

  @Override
  public FieldGet instantiate(Substitution substitution) {
    return new FieldGet(field.instantiate(substitution));
  }

}
