package randoop.condition.specification;

import java.util.Objects;

/**
 * Abstract class for representations of conditions that can be attached to methods and
 * constructors.
 */
public abstract class Specification {

  /** The description of this {@link Specification} */
  private final String description;

  /** The {@link Guard} for this specification */
  private final Guard guard;

  /** Default constructor for Gson serialization. */
  protected Specification() {
    this.description = "";
    this.guard = null;
  }

  /**
   * Creates a {@link Specification} with the given guard.
   *
   * @param description the description of the created specification
   * @param guard the {@link Guard} for the created specification
   */
  public Specification(String description, Guard guard) {
    this.description = description;
    this.guard = guard;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Specification)) {
      return false;
    }
    Specification other = (Specification) object;
    return this.description.equals(other.description)
        && ((this.guard != null && this.guard.equals(other.guard))
            || (this.guard == null && other.guard == null));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(description);
  }

  /**
   * Returns the description of this {@link Specification}.
   *
   * @return the description of this specification
   */
  public String getDescription() {
    return description;
  }

  /**
   * Return the {@link Guard} of this {@link Specification}.
   *
   * @return the guard of this specification
   */
  public Guard getGuard() {
    return guard;
  }
}
