package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import randoop.BugInRandoopException;
import randoop.sequence.Variable;

/**
 * PublicField is an abstract class representing a public field of a class object,
 * which can be an instance field, a static field, or a static final field.
 * Each is implemented as a separate class.
 * Meant to be adapted by either {@link FieldSetter} or {@link FieldGetter} for use as
 * a {@link Operation}.
 *
 * @see InstanceField
 * @see StaticField
 * @see StaticFinalField
 *
 * @author bjkeller
 *
 */
public abstract class PublicField implements Serializable {

  private static final long serialVersionUID = -8083487154339374578L;

  private Field field;

  /**
   * PublicField sets the {@link Field} object for the public field.
   *
   * @param field
   */
  public PublicField(Field field) {
    this.field = field;
  }

  /**
   * getSetTypes returns a list of types needed to set a field.
   * What is returned depends on the implementing class.
   *
   * @return list of types needed to set the field.
   */
  public abstract List<Class<?>> getSetTypes();

  /**
   * getAccessTypes returns a list of types needed to access the field.
   *
   * @return a singleton list with declaring class, or an empty list
   */
  public abstract List<Class<?>> getAccessTypes();

  /**
   * getDeclaringClass returns the class in which the field is a member.
   *
   * @return class where field is declared.
   */
  public Class<?> getDeclaringClass() {
    return field.getDeclaringClass();
  }

  /**
   * getType returns the type of the values held by the field.
   *
   * @return object representing type of field.
   */
  public Class<?> getType() {
    return field.getType();
  }

  /**
   * getName returns the declared name of the field.
   * @return unqualified name of the field.
   */
  public String getName() {
    return field.getName();
  }

  /**
   * toCode translates field into a string representing fully qualified
   * name.
   *
   * @param inputVars - list of input variables
   * @return string representing code representation of field.
   */
  public abstract String toCode(List<Variable> inputVars);

  /**
   * toParseableString returns a string descriptor of a field that can be parsed by
   * {@link PublicFieldParser#parse(String)}.
   *
   * @return String for type-field pair describing field.
   */
  public String toParseableString() {
    return field.getType().getName() + ":" + field.getDeclaringClass().getName() + "." + field.getName();
  }

  @Override
  public String toString() {
    return toParseableString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PublicField) {
      PublicField f = (PublicField)obj;
      return this.field.equals(f.field);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return field.hashCode();
  }

  /**
   * getValue uses reflection to return the value of the field for the given object.
   * Suppresses exceptions that occur because PublicField was not correctly initialized.
   *
   * @param object - instance to which field belongs, or null if field is static.
   * @return reference to value of field.
   * @throws BugInRandoopException if field access throws {@link IllegalArgumentException} or {@link IllegalAccessException}.
   */
  public Object getValue(Object object) {
    Object ret = null;
    try {
      ret = field.get(object);
    } catch (IllegalArgumentException e) {
      throw new BugInRandoopException("Field access to object of wrong type: " + e.getMessage());
    } catch (IllegalAccessException e) {
      throw new BugInRandoopException("Access control violation for field: " + e.getMessage());
    }
    return ret;
  }

  /**
   * setValue uses reflection to set the value of the field for the given object.
   * Suppresses exceptions that occur because setup was incorrect.
   *
   * @param object - instance to which field belongs, or null if static.
   * @param value - new value to assign to field
   * @throws BugInRandoopException if field access throws {@link IllegalArgumentException} or {@link IllegalAccessException}.
   */
  public void setValue(Object object, Object value) {
    try {
      field.set(object, value);
    } catch (IllegalArgumentException e) {
      throw new BugInRandoopException("Field set to object of wrong type: " + e.getMessage());
    } catch (IllegalAccessException e) {
      throw new BugInRandoopException("Access control violation for field: " + e.getMessage());
    }
  }

  /**
   * writeReplace creates {@link Serializable} version of field.
   *
   * @return representation of field.
   * @see SerializablePublicField
   */
  protected Object writeReplace() throws ObjectStreamException {
    return new SerializablePublicField(field);
  }

}
