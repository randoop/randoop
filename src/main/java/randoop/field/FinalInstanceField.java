package randoop.field;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import randoop.sequence.Variable;
import randoop.types.ConcreteType;

/**
 * FinalInstanceField implements getter methods for final instance fields of a
 * class.
 */
public class FinalInstanceField extends AccessibleField {

  private static final long serialVersionUID = 6214863094040724681L;

  public FinalInstanceField(Field field) {
    super(field);
  }

  /**
   * {@inheritDoc}
   *
   * @return empty list since a final field may not be set
   */
  @Override
  public List<ConcreteType> getSetTypes() {
    return new ArrayList<>();
  }

  /**
   * {@inheritDoc}
   *
   * @return list with just the declaring type.
   */
  @Override
  public List<ConcreteType> getAccessTypes() {
    List<ConcreteType> types = new ArrayList<>();
    types.add(getDeclaringType());
    return types;
  }

  @Override
  public String toCode(List<Variable> inputVars) {
    return inputVars.get(0) + "." + getName();
  }
}
