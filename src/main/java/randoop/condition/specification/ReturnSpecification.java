package randoop.condition.specification;

import java.util.Objects;

/**
 * A {@link ReturnSpecification} is a specification of a contract on the return value (or outcome)
 * of the invocation of an operation. The specification consists of a {@link Guard} and a {@link
 * Property}. For an invocation of the operation, if the {@link Guard} evaluates to true, then the
 * {@link Property} must also be true.
 */
public class ReturnSpecification extends Specification {

  /** the post-condition */
  private final Property property;

  /**
   * Creates a {@link ReturnSpecification} with the given guard and property.
   *
   * @param description the description of the specification
   * @param guard the {@link Guard} for the constructed specification
   * @param property the {@link Property} for the constructed specification
   */
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

  /**
   * Gets the {@link Property} (post-condition) for this specification.
   *
   * @return the property for this specification
   */
  public Property getProperty() {
    return property;
  }
}
