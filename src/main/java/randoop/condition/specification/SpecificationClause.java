package randoop.condition.specification;

import java.util.Objects;

/**
 * Abstract class for representations of conditions that can be attached to methods and
 * constructors.
 *
 * <p>Every specification clause has at least a guard (which this class provides). Some clauses have
 * more parts, so subclasses of this class can add fields.
 */
public abstract class SpecificationClause {

  // NOTE: changing field names or @SerializedName annotations could affect integration with other
  // tools

  /** The description of this {@link SpecificationClause}. */
  private final String description;

  /** The {@link Guard} for this specification. */
  private final Guard guard;

  /** Gson serialization requires a default constructor. */
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

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
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
}
