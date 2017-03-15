package randoop.condition.specification;

/** Created by bjkeller on 3/14/17. */
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
