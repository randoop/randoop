package randoop.condition.specification;

/**
 * The representation of a boolean expression over the values of parameters and return value of an
 * operation (e.g., a method or constructor).
 *
 * @see Specification
 */
public class Guard extends AbstractBooleanExpression {

  /**
   * Creates a {@link AbstractBooleanExpression} with the given description and condition code.
   *
   * @param description the description of this boolean condition
   * @param conditionText the text of the Java code for the created condition
   */
  public Guard(String description, String conditionText) {
    super(description, conditionText);
  }
}
