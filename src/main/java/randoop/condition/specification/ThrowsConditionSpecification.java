package randoop.condition.specification;

import java.util.Objects;

/**
 * The specification for a {@link ThrowsConditionSpecification} that specifies that an exception
 * should be thrown. For use as a post-condition in a {@link PostconditionSpecification}.
 */
public class ThrowsConditionSpecification extends SimpleConditionSpecification {

  /** The type of the expected exception */
  private final Class<? extends Throwable> exceptionType;

  /**
   * Creates a {@link ThrowsConditionSpecification} representing an excepted exception.
   *
   * @param description the description of the condition
   * @param exceptionType the expected exception type
   */
  public ThrowsConditionSpecification(
      String description, Class<? extends Throwable> exceptionType) {
    super(description);
    this.exceptionType = exceptionType;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ThrowsConditionSpecification)) {
      return false;
    }
    ThrowsConditionSpecification other = (ThrowsConditionSpecification) object;
    return super.equals(other) && this.exceptionType.equals(other.exceptionType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), exceptionType);
  }

  @Override
  public String toString() {
    return "throws " + exceptionType.getName();
  }

  /**
   * Returns the expected exception type for this condition.
   *
   * @return the expected exception type
   */
  public Class<? extends Throwable> getExceptionType() {
    return exceptionType;
  }
}
