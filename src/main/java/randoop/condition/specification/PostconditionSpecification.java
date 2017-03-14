package randoop.condition.specification;

import java.util.Objects;

/**
 * A {@link PostconditionSpecification} describes a pre-post-condition pair where the post-condition
 * is expected to hold if the pre-condition holds. The post-condition is a {@link
 * SimpleSpecification}.
 */
public class PostconditionSpecification extends Specification {

  /** the precondition */
  private final ConditionSpecification precondition;

  /** the post-condition */
  private final SimpleSpecification postcondition;

  public PostconditionSpecification(
      String description, ConditionSpecification precondition, SimpleSpecification postcondition) {
    super(description);
    this.precondition = precondition;
    this.postcondition = postcondition;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof PostconditionSpecification)) {
      return false;
    }
    PostconditionSpecification other = (PostconditionSpecification) object;
    return super.equals(other)
        && this.precondition.equals(other.precondition)
        && this.postcondition.equals(other.postcondition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.precondition, this.postcondition);
  }

  @Override
  public String toString() {
    return "precondition: " + precondition + ", poscondition: " + postcondition;
  }
}
