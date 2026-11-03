package randoop.test;

/**
 * Represents the occurrence of an invalid value for a {@link
 * randoop.condition.ExecutableBooleanExpression} of the {@link randoop.operation.TypedOperation} in
 * the last {@link randoop.sequence.Statement} of a sequence.
 */
public class InvalidValueCheck implements Check {

  /** Create an InvalidValueCheck. */
  public InvalidValueCheck() {}

  @Override
  public String toCodeStringPreStatement() {
    return "";
  }

  @Override
  public String toCodeStringPostStatement() {
    return "";
  }
}
