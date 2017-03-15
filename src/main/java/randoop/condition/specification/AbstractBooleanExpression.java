package randoop.condition.specification;

import java.util.Objects;

/** Created by bjkeller on 3/14/17. */
public abstract class AbstractBooleanExpression {
  /** The text of the Java code for this condition */
  private final String conditionText;

  private final String description;

  /**
   * Creates a {@link AbstractBooleanExpression} with the given description and condition code.
   *
   * @param description the description of this boolean condition
   * @param conditionText the text of the Java code for the created condition
   */
  public AbstractBooleanExpression(String description, String conditionText) {
    this.description = description;
    this.conditionText = conditionText;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof AbstractBooleanExpression)) {
      return false;
    }
    AbstractBooleanExpression other = (AbstractBooleanExpression) object;
    return super.equals(object) && this.conditionText.equals(other.conditionText);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.conditionText);
  }

  @Override
  public String toString() {
    return "not implemented";
  }

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
