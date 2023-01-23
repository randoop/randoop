package randoop.condition.specification;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

/**
 * Abstract class representing boolean expressions that occur in {@link SpecificationClause}
 * objects.
 *
 * @see Guard
 * @see Property
 */
public abstract class AbstractBooleanExpression {

  // NOTE: changing field names or @SerializedName annotations could affect integration with other
  // tools

  /** The text of the Java code for this expression. */
  @SerializedName("condition")
  private final String conditionSource;

  /** The description of the expression. Used as the descriptive text in test assertions. */
  private final String description;

  /** Gson serialization requires a default constructor. */
  @SuppressWarnings("unused")
  private AbstractBooleanExpression() {
    this.conditionSource = "";
    this.description = "";
  }

  /**
   * Creates a {@link AbstractBooleanExpression} with the given description and condition source
   * code.
   *
   * @param description the description of this boolean condition
   * @param conditionSource the text of the Java code for the created condition
   */
  AbstractBooleanExpression(String description, String conditionSource) {
    this.description = description;
    this.conditionSource = conditionSource;
  }

  /**
   * Return the description of this {@link AbstractBooleanExpression}.
   *
   * @return the description of this condition
   */
  public String getDescription() {
    return description;
  }

  /**
   * Return the condition text for this {@link AbstractBooleanExpression}.
   *
   * @return the Java source code for this condition
   */
  public String getConditionSource() {
    return conditionSource;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof AbstractBooleanExpression)) {
      return false;
    }
    AbstractBooleanExpression other = (AbstractBooleanExpression) object;
    return this.description.equals(other.description)
        && this.conditionSource.equals(other.conditionSource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.description, this.conditionSource);
  }

  @Override
  public String toString() {
    return "{ \"description\": \""
        + description
        + "\", \"conditionSource\": \""
        + conditionSource
        + "\" }";
  }
}
