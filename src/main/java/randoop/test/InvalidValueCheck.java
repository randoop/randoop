package randoop.test;

import randoop.sequence.ExecutableSequence;

/**
 * Represents the occurrence of an invalid value for a {@link
 * randoop.condition.ExecutableBooleanExpression} of the {@link randoop.operation.TypedOperation} in
 * a {@link randoop.sequence.Statement}.
 */
public class InvalidValueCheck implements Check {

  ExecutableSequence sequence;
  int index;

  public InvalidValueCheck(ExecutableSequence sequence, int index) {
    this.sequence = sequence;
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
