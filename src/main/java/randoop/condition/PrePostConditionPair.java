package randoop.condition;

/** Represents a pre-condition and post-condition pair for an operation. */
class PrePostConditionPair {

  /** The pre-condition that must hold before the operation is called. */
  final Condition preCondition;

  /**
   * The post-condition that must be true after the operation is called when the pre-condition is
   * true.
   */
  final PostCondition postCondition;

  /**
   * Creates a {@link PrePostConditionPair} object for the pre-condition and post-clause.
   *
   * @param preCondition the {@link Condition} to be evaluated before the operation is called
   * @param postCondition the {@link PostCondition} to be evaluated after the operation is called
   */
  PrePostConditionPair(Condition preCondition, PostCondition postCondition) {
    this.preCondition = preCondition;
    this.postCondition = postCondition;
  }
}
