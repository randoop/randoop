package randoop.condition.specification;

import java.util.Objects;

/**
 * The specification for a {@link ThrowsSpecification} that specifies that an exception should be
 * thrown. For use as a post-condition in a {@link ReturnSpecification}.
 */
public class ThrowsSpecification extends Specification {

  /** The type of the expected exception */
  private final String exceptionTypeName;

  /**
   * Creates a {@link ThrowsSpecification} representing an excepted exception.
   *
   * @param description the description of the condition
   * @param exceptionTypeName the expected exception type
   */
  public ThrowsSpecification(String description, Guard guard, String exceptionTypeName) {
    super(description, guard);
    this.exceptionTypeName = exceptionTypeName;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ThrowsSpecification)) {
      return false;
    }
    ThrowsSpecification other = (ThrowsSpecification) object;
    return super.equals(other) && this.exceptionTypeName.equals(other.exceptionTypeName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), exceptionTypeName);
  }

  @Override
  public String toString() {
    return getGuard() + " => throws " + exceptionTypeName;
  }
}
