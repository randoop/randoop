package randoop.condition.specification;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

/** Abstract representation of boolean expressions that occur in {@link Specification} objects. */
public abstract class AbstractBooleanExpression {

  /** The text of the Java code for this expression */
  @SerializedName("condition")
  private final String conditionText;

  /** The description of the expression */
  private final String description;

  /** A default constructor is expected for Gson serialization. */
  private AbstractBooleanExpression() {
    this.conditionText = "";
    this.description = "";
  }

  /**
   * Creates a {@link AbstractBooleanExpression} with the given description and condition code.
   *
   * @param description the description of this boolean condition
   * @param conditionText the text of the Java code for the created condition
   */
  AbstractBooleanExpression(String description, String conditionText) {
    this.description = description;
    this.conditionText = conditionText;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof AbstractBooleanExpression)) {
      return false;
    }
    AbstractBooleanExpression other = (AbstractBooleanExpression) object;
    return this.description.equals(other.description)
        && this.conditionText.equals(other.conditionText);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.description, this.conditionText);
  }

  @Override
  public String toString() {
    return "{ description: " + description + ", conditionText: " + conditionText + " }";
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
   * @return the Java text for this condition
   */
  public String getConditionText() {
    return conditionText;
  }
}
