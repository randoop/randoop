package randoop.field;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import randoop.sequence.Variable;

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
  public List<Class<?>> getSetTypes() {
    return new ArrayList<>();
  }

  /**
   * {@inheritDoc}
   * 
   * @return list with just the declaring type.
   */
  @Override
  public List<Class<?>> getAccessTypes() {
    List<Class<?>> types = new ArrayList<>();
    types.add(getDeclaringClass());
    return types;
  }

  @Override
  public String toCode(List<Variable> inputVars) {
    return inputVars.get(0) + "." + getName();
  }

}
