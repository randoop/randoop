package randoop.operation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import randoop.sequence.Variable;

/**
 * PublicStaticField represents a public static field of a class. 
 * @see PublicField
 * @author bjkeller
 *
 */
public class StaticField extends PublicField {

  private static final long serialVersionUID = 240655978039880972L;

  public StaticField(Field field) {
    super(field);
  }

  /**
   * getSetTypes returns a list containing just the field type,
   * which is the only type needed to set the field.
   */
  @Override
  public List<Class<?>> getSetTypes() {
    List<Class<?>> types = new ArrayList<>();
    types.add(getType());
    return types; 
  }

  /**
   * toCode returns a String representation of the code to access the
   * field. Should be qualified class followed by field name.
   */
  @Override
  public String toCode(List<Variable> inputVars) {
    return getDeclaringClass().getName() + "." + getName();
  }

  /**
   * getAccessTypes return list of types needed to access field.
   * Should be empty.
   */
  @Override
  public List<Class<?>> getAccessTypes() {
   return new ArrayList<>();
  }
  
  @Override
  public boolean isStatic() {
    return true;
  }

}
