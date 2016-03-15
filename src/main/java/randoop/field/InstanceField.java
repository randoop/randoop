package randoop.field;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import randoop.sequence.Variable;
import randoop.types.ConcreteType;

/**
 * InstanceField represents an instance field of a class. This means that to be
 * set, the field must be qualified by the object.
 *
 */
public class InstanceField extends AccessibleField {

  private static final long serialVersionUID = -1879588478257403865L;

  public InstanceField(Field field) {
    super(field);
  }

  /**
   * Returns a list consisting of the declaring class and the type of the field.
   * These are types needed to set an instance field.
   */
  @Override
  public List<ConcreteType> getSetTypes() {
    List<ConcreteType> types = new ArrayList<>();
    types.add(getDeclaringType());
    types.add(getType());
    return types;
  }

  @Override
  public String toCode(List<Variable> inputVars) {
    return inputVars.get(0).getName() + "." + getName();
  }

  /**
   * Returns list of types needed to access field. Should be singleton list of
   * declaring class.
   */
  @Override
  public List<ConcreteType> getAccessTypes() {
    List<ConcreteType> types = new ArrayList<>();
    types.add(getDeclaringType());
    return types;
  }
}
