package randoop.condition;

/**
 * Represents a guard and property expression pair for an operation.
 *
 * <p>Corresponds to a {@link randoop.condition.specification.Postcondition}.
 */
public class GuardPropertyPair {

  /** The expression that must hold before the operation is called. */
  final ExecutableBooleanExpression guardExpression;

  /**
   * The expression that must be true after the operation is called when the {@link
   * #guardExpression} is true.
   */
  final ExecutableBooleanExpression booleanExpression;

  /**
   * Creates a {@link GuardPropertyPair} object for the guard and property expressions.
   *
   * @param guardExpression the {@link ExecutableBooleanExpression} to be evaluated before the
   *     operation is called
   * @param booleanExpression the {@link ExecutableBooleanExpression} to be evaluated after the
   *     operation is called
   */
  GuardPropertyPair(
      ExecutableBooleanExpression guardExpression, ExecutableBooleanExpression booleanExpression) {
    this.guardExpression = guardExpression;
    this.booleanExpression = booleanExpression;
  }
}
