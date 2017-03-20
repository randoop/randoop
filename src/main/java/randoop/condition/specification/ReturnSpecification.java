package randoop.condition.specification;

import java.util.Objects;

/**
 * A {@link ReturnSpecification} is a specification of a contract on the return value (or outcome)
 * of the invocation of an operation. The specification consists of a {@link Guard} and a {@link
 * Property}. For an invocation of the operation, if the {@link Guard} evaluates to true, then the
 * {@link Property} must also be true.
 *
 * <p>*
 *
 * <p>The JSON serialization of this class is used to read the specifications for an operation given
 * using the {@link randoop.main.GenInputsAbstract#specifications} command-line option. The JSON
 * should include a JSON object labeled by the name of each field of this class, as in
 *
 * <pre>
 *   {
 *     "property": {
 *        "conditionText": {@code "result >= 0"},
 *        "description": "received value is non-negative"
 *      },
 *     "description": "returns non-negative received value",
 *     "guard": {
 *        "conditionText": "true",
 *        "description": ""
 *      }
 *   }
 * </pre>
 *
 * See the classes {@link Guard} and {@link Property} for details on specifying those objects.
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
