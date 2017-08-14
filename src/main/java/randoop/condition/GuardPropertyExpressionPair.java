package randoop.condition;

/** Represents a pre-condition and post-condition pair for an operation. */
class GuardPropertyExpressionPair {

  /** The expression that must hold before the operation is called. */
  final BooleanExpression guardExpression;

  /**
   * The expression that must be true after the operation is called when the {@link
   * #guardExpression} is true.
   */
  final PropertyExpression propertyExpression;

  /**
   * Creates a {@link GuardPropertyExpressionPair} object for the guard and property expressions.
   *
   * @param guardExpression the {@link BooleanExpression} to be evaluated before the operation is
   *     called
   * @param propertyExpression the {@link PropertyExpression} to be evaluated after the operation is
   *     called
   */
  GuardPropertyExpressionPair(
      BooleanExpression guardExpression, PropertyExpression propertyExpression) {
    this.guardExpression = guardExpression;
    this.propertyExpression = propertyExpression;
  }
}
