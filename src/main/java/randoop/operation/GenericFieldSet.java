package randoop.operation;

import randoop.field.GenericAccessibleField;
import randoop.types.ConcreteType;
import randoop.types.Substitution;

public class GenericFieldSet extends GenericOperation {

  private final GenericAccessibleField field;

  public GenericFieldSet(GenericAccessibleField field) {
    super(field.getDeclaringType(), field.getSetTypes(), ConcreteType.forClass(void.class));
    this.field = field;
  }

  @Override
  public FieldSet instantiate(Substitution substitution) {
    return new FieldSet(field.instantiate(substitution));
  }
}
