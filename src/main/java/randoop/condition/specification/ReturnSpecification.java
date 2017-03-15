package randoop.condition.specification;

import java.util.Objects;

/**
 * A {@link ReturnSpecification} describes a pre-post-condition pair where the post-condition is
 * expected to hold if the pre-condition holds. The post-condition is a {@link Property}.
 */
public class ReturnSpecification extends Specification {

  /** the post-condition */
  private final Property property;

  public ReturnSpecification(String description, Guard guard, Property property) {
    super(description, guard);
    this.property = property;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ReturnSpecification)) {
      return false;
    }
    ReturnSpecification other = (ReturnSpecification) object;
    return super.equals(other) && this.property.equals(other.property);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.property);
  }

  @Override
  public String toString() {
    return getGuard() + " => " + property;
  }
}
