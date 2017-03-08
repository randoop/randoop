package randoop.condition;

/**
 * Represents a condition on an operation. Kind of like a predicate, but with names changed to
 * protect the innocent.
 */
public interface Condition {

  /**
   * Indicate whether this condition is satisfied by the given values.
   *
   * @param values the values to check the condition against
   * @return true if this condition is satisfied by the values, false otherwise
   */
  boolean check(Object[] values);

  /**
   * Return text that describes this condition.
   *
   * @return text that describes this condition
   */
  String getComment();

  /**
   * Return this condition as a string. Arguments to the condition {@link
   * randoop.contract.ObjectContract} convention where variables are represented by {@code x0}, ...,
   * {@code xn} for some number {@code n}. If the operation takes a receiver it will be {@code x0},
   * and if the operation has a return value it will be {@code xn} (the last variable).
   *
   * @return the Java representation of the condition as a {@code String}
   */
  String getConditionString();
}
