package randoop.test;

import randoop.sequence.ExecutableSequence;

/**
 * Represents the occurrence of an invalid value for a {@link
 * randoop.condition.ExecutableBooleanExpression} of the {@link randoop.operation.TypedOperation} in
 * a {@link randoop.sequence.Statement}.
 */
public class InvalidValueCheck implements Check {

  /** The sequence that creates the value to be checked. */
  ExecutableSequence eseq;
  /** The index in the sequence of the statement that creates the value. */
  int index;

  /**
   * Create an InvalidValueCheck.
   *
   * @param eseq the sequence that creates the value to be checked
   * @param index the index in the sequence of the statement that creates the value to be checked
   */
  public InvalidValueCheck(ExecutableSequence eseq, int index) {
    this.eseq = eseq;
    this.index = index;
  }

  @Override
  public String toCodeStringPreStatement() {
    return "";
  }

  @Override
  public String toCodeStringPostStatement() {
    return "";
  }
}
