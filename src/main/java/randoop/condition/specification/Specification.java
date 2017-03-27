package randoop.condition.specification;

import java.util.Objects;

/**
 * Conditions that can be attached to methods and constructors are given in the form of {@link
 * Specification} objects.
 */
public abstract class Specification {

  /** The description of this {@link Specification} */
  private final String description;

  /** The {@link Guard} for this specification */
  private final Guard guard;

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
    return this.description.equals(other.description) && this.guard.equals(other.guard);
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
