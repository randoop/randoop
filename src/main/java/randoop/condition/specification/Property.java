package randoop.condition.specification;

/**
 * The representation of a boolean expression over the pre- and post-state of the parameters,
 * receiver and return value of an operation.
 */
public class Property extends AbstractBooleanExpression {
  /**
   * Creates a {@link AbstractBooleanExpression} with the given description and condition code.
   *
   * @param description the description of this boolean condition
   * @param conditionText the text of the Java code for the created condition
   */
  public Property(String description, String conditionText) {
    super(description, conditionText);
  }
}
