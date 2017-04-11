package randoop.test;

import java.util.List;
import java.util.Set;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NotExecuted;
import randoop.condition.ExpectedException;
import randoop.sequence.ExecutableSequence;
import randoop.types.ClassOrInterfaceType;

/**
 * A {@link TestCheckGenerator} that generates checks for exceptions that are expected at a
 * particular statement. Creates a {@link ExpectedExceptionCheck} that is returned in a {@link
 * RegressionChecks} collection if the exception occurs, or a {@link ErrorRevealingChecks}
 * collection if not.
 *
 * <p>Note that this generator is distinct from other check generators that either return regression
 * checks or error-revealing checks.
 */
public class ExpectedExceptionGenerator implements TestCheckGenerator {
  private final List<Set<ExpectedException>> exceptionSets;

  public ExpectedExceptionGenerator(List<Set<ExpectedException>> exceptionSets) {
    this.exceptionSets = exceptionSets;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Adds checks for an expected exception at the final statement of the sequence.
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
      for (Set<ExpectedException> exceptionSet : exceptionSets) {
        for (ExpectedException exception : exceptionSet) {
          ClassOrInterfaceType expected = exception.getExceptionType();
          if (!throwableType.isSubtypeOf(expected)) { // if exception is not in set
            // XXX this doesn't carry information about exception that occurred
            return getMissingExceptionTestChecks(finalIndex);
          }
        }
      }
      Check check = new ExpectedExceptionCheck(throwable, finalIndex, throwableType.getName());
      checks.add(check);
      return checks;
    } else { // if execution was normal, then expected exception is missing
      checks = getMissingExceptionTestChecks(finalIndex);
    }

    return checks;
  }

  private TestChecks getMissingExceptionTestChecks(int finalIndex) {
    TestChecks checks = new ErrorRevealingChecks();
    checks.add(new MissingExceptionCheck(exceptionSets, finalIndex));
    return checks;
  }

  /**
   * Returns the type of the expected exception.
   *
   * @return the type of the expected exception
   */
  public List<Set<ExpectedException>> getExpected() {
    return exceptionSets;
  }
}
