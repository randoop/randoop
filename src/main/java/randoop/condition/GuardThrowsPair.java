package randoop.condition;

/**
 * Represents a pair of a an executable guard expression and an executable throws clause.
 *
 * <p>Corresponds to a {@link randoop.condition.specification.ThrowsCondition}.
 */
class GuardThrowsPair {

  /** The expression that should be true before the operation is called. */
  final ExecutableBooleanExpression guard;

  /**
   * The {@link ThrowsClause} representing an exception that is expected to be thrown by the
   * operation if the {@link #guard} is true.
   */
  final ThrowsClause throwsClause;

  /**
   * Creates a {@link GuardThrowsPair} object for the guard expression and throws-clause.
   *
   * @param guard the {@link ExecutableBooleanExpression} to be evaluated before the operation is
   *     called
   * @param throwsClause the {@link ThrowsClause} to be evaluated after the operation is called
   */
  GuardThrowsPair(ExecutableBooleanExpression guard, ThrowsClause throwsClause) {
    this.guard = guard;
    this.throwsClause = throwsClause;
  }
}
