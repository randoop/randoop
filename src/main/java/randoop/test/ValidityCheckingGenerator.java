package randoop.test;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.SequenceExceptionError;
import randoop.test.predicate.ExceptionPredicate;

/**
 * A {@code ValidityCheckingGenerator} checks for occurrences of exceptions that have been tagged as
 * invalid behaviors as represented by a {@code ExceptionPredicate}. Also, either ignores or reports
 * flaky test sequences --- an input sequence that throws an exception in a longer test sequence,
 * despite having run normally by itself. Ignores flaky sequences are classified as invalid. Flaky
 * occurrences of {@code OutOfMemoryError} are always treated as invalid.
 */
public class ValidityCheckingGenerator implements TestCheckGenerator {

  /** The predicate to determine whether a test sequence is valid */
  private ExceptionPredicate isInvalid;

  /** The flag to determine whether to report flaky tests by throwing an exception */
  private boolean throwExceptionOnFlakyTest;

  /**
   * Creates an object that looks for invalid exceptions.
   *
   * @param isInvalid the predicate to test for invalid exceptions
   * @param throwExceptionOnFlakyTest a flag indicating whether to report flaky tests by throwing an
   *     exception
   */
  public ValidityCheckingGenerator(
      ExceptionPredicate isInvalid, boolean throwExceptionOnFlakyTest) {
    this.isInvalid = isInvalid;
    this.throwExceptionOnFlakyTest = throwExceptionOnFlakyTest;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Checks validity of a test sequence and creates a {@code InvalidCheck} object containing
   * checks for any invalid exceptions encountered. An exception is invalid if:
   *
   * <ul>
   *   <li>an {@code OutOfMemoryError} or {@code StackOverflowError} exception is seen before the
   *       last statement, or
   *   <li>the exception is classified as invalid by this visitor's {@code ExceptionPredicate}
   * </ul>
   *
   * @return a possibly empty {@link InvalidCheck} object for sequence
   * @throws Error if throwExceptionOnFlakyTest==true and any exception encountered before last
   *     statement of sequence
   */
  @Override
  public InvalidCheck generateTestChecks(ExecutableSequence eseq) {
    InvalidCheck checks = new InvalidCheck();
    int finalIndex = eseq.sequence.size() - 1;
    for (int i = 0; i < eseq.sequence.size(); i++) {
      ExecutionOutcome result = eseq.getResult(i);
      if (result instanceof ExceptionalExecution) {
        ExceptionalExecution exec = (ExceptionalExecution) result;
        Throwable e = exec.getException();

        if (i != finalIndex) {
          if (throwExceptionOnFlakyTest
              && !((e instanceof OutOfMemoryError) || (e instanceof StackOverflowError))) {
            throw new SequenceExceptionError(eseq, i, e);
          }
          checks.add(new InvalidExceptionCheck(e, i, e.getClass().getName()));
          return checks;
        } else if (isInvalid.test(exec, eseq)) {
          checks.add(new InvalidExceptionCheck(e, i, e.getClass().getName()));
          return checks;
        }
      }
    }

    return checks;
  }
}
