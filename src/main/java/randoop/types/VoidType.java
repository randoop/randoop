package randoop.types;

/**
 * Represents {@code void}. Technically, {@code void} is used to indicate that a method has no
 * return values, and is not a type. However, we need to pretend that it is to be able to represent
 * typed operations.
 *
 * <p>The decision to have {@code void} be a separate "type" is counter to the fact that the
 * reflection method {@code Class.isPrimitive()} returns true for {@code void}.
 */
public class VoidType extends Type {

  private static final VoidType value = new VoidType();

  private VoidType() {}

  public static VoidType getVoidType() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof VoidType)) {
      return false;
    }
    return obj == value;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public Class<?> getRuntimeClass() {
    return void.class;
  }

  @Override
  public String getName() {
    return "void";
  }

  @Override
  public String getSimpleName() {
    return this.getName();
  }

  @Override
  public boolean isVoid() {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns false, since {@code void} is not a subtype of any type
   */
  @Override
  public boolean isSubtypeOf(Type otherType) {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Return false because cannot assign to void.
   */
  @Override
  public boolean isAssignableFrom(Type sourceType) {
    return false;
  }
}
