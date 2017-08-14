package randoop.condition;

/**
 * Represents a guard and property expression pair for an operation.
 *
 * <p>Corresponds to a {@link randoop.condition.specification.PostSpecification}.
 */
class GuardPropertyPair {

  /** The expression that must hold before the operation is called. */
  final BooleanExpression guardExpression;

  /**
   * The expression that must be true after the operation is called when the {@link
   * #guardExpression} is true.
   */
  final PropertyExpression propertyExpression;

  /**
   * Creates a {@link GuardPropertyPair} object for the guard and property expressions.
   *
   * @param guardExpression the {@link BooleanExpression} to be evaluated before the operation is
   *     called
   * @param propertyExpression the {@link PropertyExpression} to be evaluated after the operation is
   *     called
   */
  GuardPropertyPair(BooleanExpression guardExpression, PropertyExpression propertyExpression) {
    this.guardExpression = guardExpression;
    this.propertyExpression = propertyExpression;
  }
}
