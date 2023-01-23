package randoop.sequence;

import java.util.Objects;
import randoop.types.ReferenceType;

/**
 * Represents a {@link ReferenceType} value created by the execution of a {@link Statement}.
 * Includes the type.
 */
public final class ReferenceValue {

  /** The type of this value. */
  private final ReferenceType type;

  /** The {@link Object} reference of this value. */
  private final Object value;

  /**
   * Create the value object given its type and {@link Object} reference.
   *
   * @param type the type of this value
   * @param value the {@link Object} reference to this value
   */
  ReferenceValue(ReferenceType type, Object value) {
    this.type = type;
    this.value = value;
  }

  /**
   * Indicates whether two {@link ReferenceValue} objects are equal. Note: tests the {@code Object}
   * value of each object by identity.
   *
   * @param obj the object to test for equality
   * @return true if parameter has the same type and identical value to this object, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ReferenceValue)) {
      return false;
    }
    ReferenceValue refValue = (ReferenceValue) obj;
    return this.type.equals(refValue.type) && this.value == refValue.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, System.identityHashCode(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }

  /**
   * Returns the {@link Object} reference of this value.
   *
   * @return the reference to the value
   */
  public Object getObjectValue() {
    return value;
  }

  /**
   * Returns the type of this value.
   *
   * @return the type of this value
   */
  public ReferenceType getType() {
    return type;
  }
}
