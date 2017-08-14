package randoop.condition.specification;

import java.util.Objects;

/**
 * Abstract class for representations of conditions that can be attached to methods and
 * constructors.
 */
public abstract class SpecificationClause {

  /** The description of this {@link SpecificationClause}. */
  private final String description;

  /** The {@link Guard} for this specification */
  private final Guard guard;

  /** Default constructor for Gson serialization. */
  protected SpecificationClause() {
    this.description = "";
    this.guard = null;
  }

  /**
   * Creates a {@link SpecificationClause} with the given guard.
   *
   * @param description the description of the created specification
   * @param guard the {@link Guard} for the created specification
   */
  public SpecificationClause(String description, Guard guard) {
    this.description = description;
    this.guard = guard;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof SpecificationClause)) {
      return false;
    }
    SpecificationClause other = (SpecificationClause) object;
    return this.description.equals(other.description)
        && ((this.guard != null && this.guard.equals(other.guard))
            || (this.guard == null && other.guard == null));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(description);
  }

  /**
   * Returns the description of this {@link SpecificationClause}.
   *
   * @return the description of this specification
   */
  public String getDescription() {
    return description;
  }

  /**
   * Return the {@link Guard} of this {@link SpecificationClause}.
   *
   * @return the guard of this specification
   */
  public Guard getGuard() {
    return guard;
  }
}
