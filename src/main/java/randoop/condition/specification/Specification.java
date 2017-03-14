package randoop.condition.specification;

import java.util.Objects;

/**
 * Conditions that can be attached to methods and constructors are given in the form of {@link
 * Specification} objects.
 */
public abstract class Specification {

  private final String description;

  public Specification(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Specification)) {
      return false;
    }
    Specification other = (Specification) object;
    return this.description.equals(other.description);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(description);
  }

  @Override
  public String toString() {
    return description;
  }

  public String getDescription() {
    return description;
  }
}
