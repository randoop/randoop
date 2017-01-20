package randoop.condition;

/**
 * Represents a condition on an operation.
 * Kind of like a predicate, but with names changed to protect the innocent.
 */
public interface Condition {

  /**
   * Indicate whether this condition is satisfied by the given values.
   *
   * @param values  the values to check the condition against
   * @return true if this condition is satisfied by the values, false otherwise
   */
  boolean check(Object[] values);

  /**
   * Return text that describes this condition.
   *
   * @return text that describes this condition
   */
  String getComment();
}
