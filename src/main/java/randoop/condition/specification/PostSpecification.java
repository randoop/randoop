package randoop.condition.specification;

import java.util.Objects;

/**
 * A {@link PostSpecification} is a specification of a contract on the return value (or outcome) of
 * the invocation of an operation. The specification consists of a {@link Guard} and a {@link
 * Property}. For an invocation of the operation, if the {@link Guard} evaluates to true, then the
 * {@link Property} must also be true.
 *
 * <p>*
 *
 * <p>The JSON serialization of this class is used to read the specifications for an operation given
 * using the {@code --specifications} command-line option. The JSON should include a JSON object
 * labeled by the name of each field of this class, as in
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
public class PostSpecification extends Specification {

  /** The post-condition */
  private final Property property;

  /** Default constructor for Gson serialization. */
  private PostSpecification() {
    super();
    this.property = null;
  }

  /**
   * Creates a {@link PostSpecification} with the given guard and property.
   *
   * @param description the description of the specification
   * @param guard the {@link Guard} for the constructed specification
   * @param property the {@link Property} for the constructed specification
   */
  public PostSpecification(String description, Guard guard, Property property) {
    super(description, guard);
    this.property = property;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof PostSpecification)) {
      return false;
    }
    PostSpecification other = (PostSpecification) object;
    return super.equals(other) && this.property.equals(other.property);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.property);
  }

  @Override
  public String toString() {
    return "{ guard: " + getGuard() + ", property: " + property + " }";
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
