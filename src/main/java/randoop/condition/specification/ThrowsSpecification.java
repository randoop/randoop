package randoop.condition.specification;

import java.util.Objects;

/**
 * The specification for a {@link ThrowsSpecification} that specifies that an exception should be
 * thrown. For use as a post-condition in a {@link ReturnSpecification}.
 */
public class ThrowsSpecification extends Specification {

  /** The fully-qualified name of the type of the expected exception */
  private final String exceptionType;

  /**
   * Creates a {@link ThrowsSpecification} representing an excepted exception.
   *
   * @param description the description of the condition
   * @param exceptionType the expected exception type
   */
  public ThrowsSpecification(String description, Guard guard, String exceptionType) {
    super(description, guard);
    this.exceptionType = exceptionType;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ThrowsSpecification)) {
      return false;
    }
    ThrowsSpecification other = (ThrowsSpecification) object;
    return super.equals(other) && this.exceptionType.equals(other.exceptionType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), exceptionType);
  }

  @Override
  public String toString() {
    return getGuard() + " => throws " + exceptionType;
  }
}
