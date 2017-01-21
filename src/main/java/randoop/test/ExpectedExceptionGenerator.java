package randoop.test;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NotExecuted;
import randoop.sequence.ExecutableSequence;
import randoop.types.ClassOrInterfaceType;

/**
 * A {@link TestCheckGenerator} that generates checks for exceptions that are expected at a
 * particular statement.
 * Creates a {@link ExpectedExceptionCheck} that is returned in a {@link RegressionChecks}
 * collection if the exception occurs, or a {@link ErrorRevealingChecks} collection if not.
 *
 * Note that this generator is distinct from other check generators that either return regression
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
   * {@inheritDoc} Adds checks for an expected exception at the final statement of the sequence.
   */
  @Override
  public TestChecks visit(ExecutableSequence s) {
    int finalIndex = s.sequence.size() - 1;
    ExecutionOutcome result = s.getResult(finalIndex);

    TestChecks checks;
    if (result instanceof NotExecuted) {
      throw new Error("Abnormal execution in sequence: " + s);
    } else if (result instanceof ExceptionalExecution) { // exception occurred
      checks = new RegressionChecks();
      ExceptionalExecution exec = (ExceptionalExecution) result;
      Throwable throwable = exec.getException();
      ClassOrInterfaceType throwableType = ClassOrInterfaceType.forClass(throwable.getClass());
      if (throwableType.isSubtypeOf(expected)) { // if exception is one expected
        Check check = new ExpectedExceptionCheck(throwable, finalIndex, expected.getName());
        checks.add(check);
        return checks;
      }
      // otherwise, exception should be handled normally
    } else { // if execution was normal, then expected exception is missing
      checks = new ErrorRevealingChecks();
      Check check = new MissingExceptionCheck(expected, conditionComment, finalIndex);
      checks.add(check);
    }

    return checks;
  }
}
