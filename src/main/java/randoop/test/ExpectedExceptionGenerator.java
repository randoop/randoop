package randoop.test;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NotExecuted;
import randoop.sequence.ExecutableSequence;
import randoop.types.ClassOrInterfaceType;

/**
 * A {@link TestCheckGenerator} that generates checks for exceptions that are expected at the final
 * statement of a sequence. Creates a {@link ExpectedExceptionCheck} that is returned in a {@link
 * RegressionChecks} collection if the exception occurs, or a {@link ErrorRevealingChecks}
 * collection if not.
 *
 * <p>Note that this generator is distinct from other check generators that either return regression
 * checks or error-revealing checks.
 */
public class ExpectedExceptionGenerator implements TestCheckGenerator {
  private final ClassOrInterfaceType expected;
  private final String conditionComment;

  public ExpectedExceptionGenerator(ClassOrInterfaceType expected, String conditionComment) {
    this.expected = expected;
    this.conditionComment = conditionComment;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Adds checks for an expected exception at the final statement of the sequence.
   */
  @Override
  public TestChecks<?> generateTestChecks(ExecutableSequence eseq) {
    int finalIndex = eseq.sequence.size() - 1;
    ExecutionOutcome result = eseq.getResult(finalIndex);

    if (result instanceof NotExecuted) {
      throw new Error("Abnormal execution in sequence: " + eseq);
    } else if (result instanceof ExceptionalExecution) { // exception occurred
      ExceptionalExecution exec = (ExceptionalExecution) result;
      Throwable throwable = exec.getException();
      ClassOrInterfaceType throwableType = ClassOrInterfaceType.forClass(throwable.getClass());
      if (throwableType.isSubtypeOf(expected)) { // if exception is one expected
        return new RegressionChecks(
            new ExpectedExceptionCheck(throwable, finalIndex, expected.getName()));
      } else {
        // otherwise, exception should be handled normally
        return RegressionChecks.EMPTY;
      }
    } else { // if execution was normal, then expected exception is missing
      return new ErrorRevealingChecks(
          new MissingExceptionCheck(expected, conditionComment, finalIndex));
    }
  }

  /**
   * Returns the type of the expected exception.
   *
   * @return the type of the expected exception
   */
  public ClassOrInterfaceType getExpected() {
    return expected;
  }
}
