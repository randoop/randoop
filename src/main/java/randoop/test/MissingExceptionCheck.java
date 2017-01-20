package randoop.test;

import java.util.Objects;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NotExecuted;
import randoop.sequence.Execution;
import randoop.types.ClassOrInterfaceType;

/**
 * Represents the fact that a statement should throw an exception, but did not.
 * It is used in an error-revealing test to indicate that normal execution of the statement
 * violates the stated throws-condition of the method/constructor.
 */
public class MissingExceptionCheck implements Check {

  /** The type of the expected exception */
  private final ClassOrInterfaceType expected;

  /** The comment describing the condition when the exception should be thrown */
  private final String conditionComment;

  /** The index of the statement where the exception should be thrown */
  private final int index;

  /**
   * Creates a {@link MissingExceptionCheck} object for the expected exception type
   * at the given statement.
   *
   * @param expected  the type of the expected exception
   * @param conditionComment  the comment text describing when exception should be thrown
   * @param index  the statement index
   */
  MissingExceptionCheck(ClassOrInterfaceType expected, String conditionComment, int index) {
    this.expected = expected;
    this.conditionComment = conditionComment;
    this.index = index;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MissingExceptionCheck)) {
      return false;
    }
    MissingExceptionCheck other = (MissingExceptionCheck) obj;
    return this.expected.equals(other.expected)
        && this.conditionComment.equals(other.conditionComment)
        && this.index == other.index;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.expected, this.conditionComment, this.index);
  }

  @Override
  public String toCodeStringPreStatement() {
    return String.format(
        "// this statement should throw %s %s%n", expected.getName(), conditionComment);
  }

  @Override
  public String toCodeStringPostStatement() {
    return String.format(
        "org.junit.Assert.fail(\"exception %s is expected\");", expected.getName());
  }

  @Override
  public String getValue() {
    return "missing_exception";
  }

  @Override
  public String getID() {
    return "MissingExceptionCheck @" + index;
  }

  /**
   * {@inheritDoc} Checks that an exception of the expected type is thrown by
   * the statement in this object in the given {@link Execution}.
   *
   * @return true if the statement throws the expected exception, false otherwise
   */
  @Override
  public boolean evaluate(Execution execution) {
    ExecutionOutcome outcomeAtIndex = execution.get(index);
    if (outcomeAtIndex instanceof NotExecuted) {
      throw new IllegalArgumentException("Statement not executed");
    }
    if (!(outcomeAtIndex instanceof ExceptionalExecution)) {
      return false;
    }
    ExceptionalExecution exec = (ExceptionalExecution) outcomeAtIndex;
    Throwable t = exec.getException();
    ClassOrInterfaceType thrownType = ClassOrInterfaceType.forClass(t.getClass());
    return thrownType.isSubtypeOf(expected);
  }
}
