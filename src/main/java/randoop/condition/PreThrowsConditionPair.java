package randoop.condition;

/** Represents a pre-condition and post-condition pair for an operation. */
class PreThrowsConditionPair {

  /** The pre-condition that should be true before the operation is called. */
  final Condition preCondition;

  /**
   * The {@link ThrowsClause} representing an exception that is expected to be thrown by the
   * operation if the pre-condition is true.
   */
  final ThrowsClause throwsClause;

  /**
   * Creates a {@link PreThrowsConditionPair} object for the pre-condition and throws-clause.
   *
   * @param preCondition the {@link Condition} to be evaluated before the operation is called
   * @param throwsClause the {@link ThrowsClause} to be evaluated after the operation is called
   */
  PreThrowsConditionPair(Condition preCondition, ThrowsClause throwsClause) {
    this.preCondition = preCondition;
    this.throwsClause = throwsClause;
  }
}
