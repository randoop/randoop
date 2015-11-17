package randoop.operation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import randoop.Variable;

/**
 * InstanceField represents an instance field of a class.
 * This means that to be set, the field must be qualified
 * by the object.
 * 
 * @author bjkeller
 *
 */
public class InstanceField extends PublicField {

  private static final long serialVersionUID = -1879588478257403865L;

  public InstanceField(Field field) {
    super(field);
  }

  /**
   * getSetTypes returns a list consisting of the declaring class
   * and the type of the field. These are types needed to set
   * an instance field.
   */
  @Override
  public List<Class<?>> getSetTypes() {
    List<Class<?>> types = new ArrayList<>();
    types.add(getDeclaringClass());
    types.add(getType());
    return types;
  }

  @Override
  public String toCode(List<Variable> inputVars) {
    return inputVars.get(0).getName() + "." + getName();
  }

  /**
   * getAccessTypes return list of types needed to access field.
   * Should be singleton list of declaring class.
   */
  @Override
  public List<Class<?>> getAccessTypes() {
    List<Class<?>> types = new ArrayList<>();
    types.add(getDeclaringClass());
    return types;
  }
  


}
