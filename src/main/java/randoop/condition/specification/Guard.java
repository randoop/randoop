package randoop.condition.specification;

import java.util.Objects;

/**
 * The specification for a condition over the values of parameters and return value of an operation
 * (e.g., a method or constructor).
 *
 * @see OperationSpecification
 */
public class ConditionSpecification extends SimpleSpecification {

  /** The text of the Java code for this condition */
  private final String conditionText;

  /**
   * Creates a {@link ConditionSpecification} with the given description and condition code.
   *
   * @param description the description of this boolean condition
   * @param conditionText the text of the Java code for the created condition
   */
  public ConditionSpecification(String description, String conditionText) {
    super(description);
    this.conditionText = conditionText;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ConditionSpecification)) {
      return false;
    }
    ConditionSpecification other = (ConditionSpecification) object;
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

  /**
   * Return the condition text for this {@link ConditionSpecification}.
   *
   * @return the Java text for this condition
   */
  public String getConditionText() {
    return conditionText;
  }
}
