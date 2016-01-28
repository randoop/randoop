package randoop.field;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import randoop.sequence.Variable;

/**
 * StaticFinalField represents a public static final field of a class.
 * @see AccessibleField
 *
 */
public class StaticFinalField extends AccessibleField {

  private static final long serialVersionUID = 5759286052853148769L;

  public StaticFinalField(Field field) {
    super(field);
  }

  /**
   * getSetTypes returns the list of types needed to set the field, 
   * which is empty for a static final (or constant) field.
   */
  @Override
  public List<Class<?>> getSetTypes() {
    return new ArrayList<>();
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
 
  /**
   * isStatic is a predicate to indicate whether field is declared as static.
   * @return true, as object is {@link StaticFinalField}.
   */
  @Override
  public boolean isStatic() {
    return true;
  }
}
