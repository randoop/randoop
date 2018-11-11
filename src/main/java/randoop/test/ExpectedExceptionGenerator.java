package randoop.test;

import java.util.List;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NotExecuted;
import randoop.condition.ThrowsClause;
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
public class ExpectedExceptionGenerator extends TestCheckGenerator {
  /**
   * The list of lists of throws clauses for which the guard expression was satisfied. Each list of
   * throwsclauses represents one specification, and each such list must be satisfied.
   */
  private final List<List<ThrowsClause>> exceptionSets;

  /**
   * Creates an {@link ExpectedExceptionGenerator} for the list of sets of expected exceptions.
   *
   * @param exceptionSets a list of lists of expected exceptions to be searched when testing an
   *     exception thrown by the operation in the final statement of the sequence. Each list of
   *     expected exceptions must be satisfied.
   */
  public ExpectedExceptionGenerator(List<List<ThrowsClause>> exceptionSets) {
    this.exceptionSets = exceptionSets;
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
      // TODO: Should this be RandoopBug?
      throw new Error("Abnormal execution in sequence: " + eseq);
    } else if (result instanceof ExceptionalExecution) { // exception occurred
      ExceptionalExecution exec = (ExceptionalExecution) result;
      Throwable throwable = exec.getException();
      ClassOrInterfaceType throwableType = ClassOrInterfaceType.forClass(throwable.getClass());
      for (List<ThrowsClause> exceptionSet : exceptionSets) {
        ClassOrInterfaceType matchingType = findMatchingExpectedType(throwableType, exceptionSet);
        if (matchingType == null) {
          // XXX this doesn't carry information about exception that occurred
          return getMissingExceptionTestChecks(finalIndex);
        }
      }
      return new RegressionChecks(
          new ExpectedExceptionCheck(throwable, finalIndex, throwableType.getName()));
    } else { // if execution was normal, then expected exception is missing
      return getMissingExceptionTestChecks(finalIndex);
    }
  }

  /**
   * Find the first ThrowsClause whose exception is a supertype of (or equal to) {@code
   * throwableType}, and return that exception type.
   *
   * @param throwableType the type to search for
   * @param throwsClauses the {@link ThrowsClause}s to search within
   * @return the type in the first matching {@link ThrowsClause}, or null if none match
   */
  private static ClassOrInterfaceType findMatchingExpectedType(
      ClassOrInterfaceType throwableType, List<ThrowsClause> throwsClauses) {
    for (ThrowsClause exception : throwsClauses) {
      ClassOrInterfaceType expected = exception.getExceptionType();
      if (throwableType.isSubtypeOf(expected)) { // if exception is in set
        return expected;
      }
    }
    return null;
  }

  /**
   * Return an ErrorRevealingChecks that checks for a missing exception at the given index.
   *
   * @param finalIndex the index
   * @return the ErrorRevealingChecks object
   */
  private ErrorRevealingChecks getMissingExceptionTestChecks(int finalIndex) {
    ErrorRevealingChecks checks = new ErrorRevealingChecks();
    checks.add(new MissingExceptionCheck(exceptionSets, finalIndex));
    return checks;
  }

  /**
   * Returns the types of the expected exceptions. Each list of throwsclauses represents one
   * specification, and each such list must be satisfied.
   *
   * @return the types of the expected exceptions
   */
  public List<List<ThrowsClause>> getExpected() {
    return exceptionSets;
  }
}
