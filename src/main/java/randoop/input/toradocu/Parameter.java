package randoop.input.toradocu;

import java.util.Objects;

/**
 * This is a Toradocu class borrowed to allow deserialization of JSON.
 */
public final class Parameter {

  /** The type of the parameter. */
  private final Type type;
  /** The name of the parameter. */
  private final String name;
  /** True if this parameter is nullable, false if nonnull, and null if unspecified. */
  private final Boolean nullable;

  /**
   *
   * Constructs a parameter with the given type and name.
   *
   * @param type the type of the parameter including its dimension
   * @param name the name of the parameter
   * @param nullable true if the parameter is nullable, false if nonnull and null if unspecified
   * @throws NullPointerException if type or name is null
   */
  public Parameter(Type type, String name, Boolean nullable) {
    this.type = type;
    this.name = name;
    this.nullable = nullable;
  }

  /**
   * Constructs a parameter with the given type and name.
   *
   * @param type the type of the parameter including its dimension
   * @param name the name of the parameter
   */
  public Parameter(Type type, String name) {
    this(type, name, null);
  }

  /**
   * Returns the name of the parameter.
   *
   * @return the name of the parameter
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the type of the parameter.
   *
   * @return the type of the parameter
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns {@code true} if the parameter is nullable, {@code false} if it is nonnull, or {@code
   * null} if its nullability is unspecified.
   *
   * @return {@code true} if the parameter is nullable, {@code false} if it is nonnull, or {@code
   *     null} if its nullability is unspecified
   */
  public Boolean getNullability() {
    return nullable;
  }

  /**
   * Returns true if this {@code Parameter} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Parameter)) return false;

    Parameter that = (Parameter) obj;
    return type.equals(that.type)
        && name.equals(that.name)
        && Objects.equals(nullable, that.nullable);
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(type, name, nullable);
  }

  /**
   * Returns a string representation of this parameter. The returned string is in the format "TYPE
   * NAME" where TYPE is the fully qualified parameter type and NAME is the name of the parameter.
   *
   * @return a string representation of this parameter
   */
  @Override
  public String toString() {
    return type + " " + name;
  }
}
