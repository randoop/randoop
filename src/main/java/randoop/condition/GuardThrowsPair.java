package randoop.condition;

/**
 * Represents a guard expression and throws clause pair for an operation.
 *
 * <p>Corresponds to a {@link randoop.condition.specification.ThrowsCondition}.
 */
class GuardThrowsPair {

  /** The expression that should be true before the operation is called. */
  final ExecutableBooleanExpression guardExpression;

  /**
   * The {@link ThrowsClause} representing an exception that is expected to be thrown by the
   * operation if the {@link #guardExpression} is true.
   */
  final ThrowsClause throwsClause;

  /**
   * Creates a {@link GuardThrowsPair} object for the guard expression and throws-clause.
   *
   * @param guardExpression the {@link ExecutableBooleanExpression} to be evaluated before the
   *     operation is called
   * @param throwsClause the {@link ThrowsClause} to be evaluated after the operation is called
   */
  GuardThrowsPair(ExecutableBooleanExpression guardExpression, ThrowsClause throwsClause) {
    this.guardExpression = guardExpression;
    this.throwsClause = throwsClause;
  }
}
