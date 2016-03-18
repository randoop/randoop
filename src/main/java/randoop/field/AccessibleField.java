package randoop.field;

import java.io.ObjectStreamException;
import java.lang.reflect.Field;
import java.util.List;

import randoop.BugInRandoopException;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.Variable;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeTuple;

/**
 * AccessibleField is an abstract class representing an accessible field of a class
 * object, which can be an instance field, a static field, or a static final
 * field. Each is implemented as a separate class. Meant to be adapted by either
 * {@link randoop.operation.FieldSet FieldSet} or
 * {@link randoop.operation.FieldGet FieldGet} for use as a
 * {@link randoop.operation.Operation Operation}.
 *
 * @see InstanceField
 * @see StaticField
 * @see StaticFinalField
 *
 */
public abstract class AccessibleField {

  private final ConcreteType declaringType;
  private final ConcreteType valueType;
  private Field field;

  /**
   * Create the public field object for the given {@code Field}.
   *
   * @param field
   *          the field.
   */
  public AccessibleField(Field field, ConcreteType declaringType, ConcreteType valueType) {
    this.declaringType = declaringType;
    this.valueType = valueType;
    this.field = field;
    this.field.setAccessible(true);
  }

  /**
   * Returns a list of types needed to set a field. What is returned depends on
   * the implementing class.
   *
   * @return list of types needed to set the field.
   */
  public abstract ConcreteTypeTuple getSetTypes();

  /**
   * Returns a list of types needed to access the field.
   *
   * @return a singleton list with declaring class, or an empty list
   */
  public abstract ConcreteTypeTuple getAccessTypes();

  /**
   * Returns the class in which the field is a member.
   *
   * @return class where field is declared.
   */
  public ConcreteType getDeclaringType() {
    return declaringType;
  }

  /**
   * Returns the type of the values held by the field.
   *
   * @return object representing type of field.
   */
  public ConcreteType getType() {
    return valueType;
  }

  /**
   * Returns the declared name of the field.
   *
   * @return unqualified name of the field.
   */
  public String getName() {
    return field.getName();
  }

  /**
   * Translates field into a string representing fully qualified name.
   *
   * @param inputVars
   *          list of input variables
   * @return string representing code representation of field.
   */
  public abstract String toCode(List<Variable> inputVars);

  /**
   * Returns a string descriptor of a field that can be parsed by
   * {@link FieldParser#parse(String)}.
   *
   * @return String for type-field pair describing field.
   */
  public String toParseableString() {
    return field.getType().getName()
        + ":"
        + field.getDeclaringClass().getName()
        + "."
        + field.getName();
  }

  /**
   * Uses {@link AccessibleField#toParseableString()} to create string
   * representation.
   */
  @Override
  public String toString() {
    return toParseableString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AccessibleField) {
      AccessibleField f = (AccessibleField) obj;
      return this.field.equals(f.field);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return field.hashCode();
  }

  /**
   * Uses reflection to return the value of the field for the given object.
   * Suppresses exceptions that occur because PublicField was not correctly
   * initialized.
   *
   * @param object
   *          - instance to which field belongs, or null if field is static.
   * @return reference to value of field.
   * @throws BugInRandoopException
   *           if field access throws {@link IllegalArgumentException} or
   *           {@link IllegalAccessException}.
   */
  public Object getValue(Object object) {
    Object ret = null;
    try {
      ret = field.get(object);
    } catch (IllegalArgumentException e) {
      throw new BugInRandoopException("Field access to object of wrong type: " + e.getMessage());
    } catch (IllegalAccessException e) {
      throw new BugInRandoopException(
          "Access control violation for field: " + field.getName() + "; " + e.getMessage());
    }
    return ret;
  }

  /**
   * Uses reflection to set the value of the field for the given object.
   * Suppresses exceptions that occur because setup was incorrect.
   *
   * @param object
   *          - instance to which field belongs, or null if static.
   * @param value
   *          - new value to assign to field
   * @throws BugInRandoopException
   *           if field access throws {@link IllegalArgumentException} or
   *           {@link IllegalAccessException}.
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
   * isStatic returns the default that a field is not static.
   *
   * @return false (default for a field).
   */
  public boolean isStatic() {
    return false;
  }

  /**
   * satisfies checks whether the enclosed {@link Field} object satisfies the
   * given predicate.
   *
   * @param predicate
   *          the {@link ReflectionPredicate} to check this.field against.
   * @return true if this.field satisfies predicate.canUse(field).
   */
  public boolean satisfies(ReflectionPredicate predicate) {
    return predicate.test(field);
  }
}
