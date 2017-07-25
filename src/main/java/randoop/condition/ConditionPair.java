package randoop.condition;

/**
 * Represents a condition-clause pair.
 *
 * <p>Intended to be used to represent pairs for pre-specifications (a pair with pre-condition and
 * post-condition) and throws-specifications (a pair with a pre-condition and a throws-clause).
 *
 * @param <T> the post-clause type (Use {@link PostCondition} or {@link ThrowsClause}.)
 */
class ConditionPair<T> {

  /** The pre-condition for the pair. */
  final Condition preCondition;

  /**
   * The clause to be evaluated after the operation is called (intention that this be either {@link
   * PostCondition} or {@link ThrowsClause}).
   */
  final T postClause;

  /**
   * Creates a {@link ConditionPair} object for the pre-condition and post-clause.
   *
   * @param preCondition The condition to be evaluated before the operation is called
   * @param postClause The clause to be evaluated after the operation is called
   */
  ConditionPair(Condition preCondition, T postClause) {
    this.preCondition = preCondition;
    this.postClause = postClause;
  }
}
