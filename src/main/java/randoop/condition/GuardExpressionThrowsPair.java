package randoop.condition;

/** Represents a guard expression and throws clause pair for an operation. */
class GuardExpressionThrowsPair {

  /** The expression that should be true before the operation is called. */
  final BooleanExpression guardExpression;

  /**
   * The {@link ThrowsClause} representing an exception that is expected to be thrown by the
   * operation if the {@link #guardExpression} is true.
   */
  final ThrowsClause throwsClause;

  /**
   * Creates a {@link GuardExpressionThrowsPair} object for the guard expression and throws-clause.
   *
   * @param guardExpression the {@link BooleanExpression} to be evaluated before the operation is
   *     called
   * @param throwsClause the {@link ThrowsClause} to be evaluated after the operation is called
   */
  GuardExpressionThrowsPair(BooleanExpression guardExpression, ThrowsClause throwsClause) {
    this.guardExpression = guardExpression;
    this.throwsClause = throwsClause;
  }
}
