package randoop.test;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.SequenceExceptionError;
import randoop.test.predicate.ExceptionPredicate;

/**
 * A {@code ValidityCheckingVisitor} checks for occurrences of exceptions that have been tagged as
 * invalid behaviors as represented by a {@code ExceptionPredicate}. Also, either ignores or reports
 * flaky test sequences --- an input sequence that throws an exception in a longer test sequence,
 * despite having run normally by itself. Ignores flaky sequences are classified as invalid. Flaky
 * occurrences of {@code OutOfMemoryError} are always treated as invalid.
 */
public class ValidityCheckingVisitor implements TestCheckGenerator {

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
  public ValidityCheckingVisitor(ExceptionPredicate isInvalid, boolean throwExceptionOnFlakyTest) {
    this.isInvalid = isInvalid;
    this.throwExceptionOnFlakyTest = throwExceptionOnFlakyTest;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Checks validity of a test sequence and creates a {@code InvalidChecks} object containing
   * checks for any invalid exceptions encountered. Exceptions are classified by the {@code
   * ExceptionPredicate}, but a sequence where an {@code OutOfMemoryError} is seen before the last
   * statement is classified as invalid regardless of how {@code OutOfMemoryError} is classified by
   * the predicate.
   *
   * @return a possibly empty {@link InvalidChecks} object for sequence
   * @throws Error if any exception encountered before last statement of sequence
   */
  @Override
  public TestChecks generateTestChecks(ExecutableSequence eseq) {
    InvalidChecks checks = new InvalidChecks();
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
        }

        if (isInvalid.test(exec, eseq)) {
          checks.add(new InvalidExceptionCheck(e, i, e.getClass().getName()));
        }
      }
    }

    return checks;
  }
}
