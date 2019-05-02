package randoop.test;

import static randoop.main.GenInputsAbstract.BehaviorType.INVALID;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.main.ExceptionBehaviorClassifier;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.SequenceExceptionError;
import randoop.util.TimeoutExceededException;

/**
 * A {@code ValidityCheckingGenerator} checks for occurrences of exceptions that have been tagged as
 * invalid behaviors. Also, either ignores or reports flaky test sequences --- an input sequence
 * that throws an exception in a longer test sequence, despite having run normally by itself.
 * Ignored flaky sequences are classified as invalid. Flaky occurrences of {@code OutOfMemoryError}
 * or {@code StackOverflowError} are always treated as invalid.
 */
public class ValidityCheckingGenerator extends TestCheckGenerator {

  /** If true, report flaky tests by throwing an exception. */
  private boolean throwExceptionOnFlakyTest;

  /**
   * Creates an object that looks for invalid exceptions.
   *
   * @param throwExceptionOnFlakyTest a flag indicating whether to report flaky tests by throwing an
   *     exception
   */
  public ValidityCheckingGenerator(boolean throwExceptionOnFlakyTest) {
    this.throwExceptionOnFlakyTest = throwExceptionOnFlakyTest;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Checks validity of a test sequence and creates a {@code InvalidChecks} object containing a
   * {@link InvalidChecks} for the first invalid exception encountered, if any. There are three
   * possible outcomes:
   *
   * <ul>
   *   <li>An exception is seen before the last statement:
   *       <ul>
   *         <li>If throwExceptionOnFlakyTest is true and the exception is not {@code
   *             OutOfMemoryError} or {@code StackOverflowError}, throw an exception.
   *         <li>Otherwise, the sequence is invalid.
   *       </ul>
   *   <li>An exception is seen on the last statement:
   *       <ul>
   *         <li>if the exception is classified as invalid, the sequence is invalid.
   *         <li>otherwise, the returned InvalidChecks is empty (the sequence is valid).
   *       </ul>
   *   <li>Otherwise, the returned InvalidChecks is empty (the sequence is valid).
   * </ul>
   *
   * @return a possibly-empty {@link InvalidChecks} object for sequence
   * @throws Error if throwExceptionOnFlakyTest==true and exception encountered before last
   *     statement of sequence
   */
  @Override
  public InvalidChecks generateTestChecks(ExecutableSequence eseq) {
    int finalIndex = eseq.sequence.size() - 1;
    for (int i = 0; i < eseq.sequence.size(); i++) {
      ExecutionOutcome result = eseq.getResult(i);
      if (result instanceof ExceptionalExecution) {
        ExceptionalExecution exec = (ExceptionalExecution) result;
        Throwable e = exec.getException();

        if (i != finalIndex) {
          if (throwExceptionOnFlakyTest
              && !((e instanceof OutOfMemoryError)
                  || (e instanceof StackOverflowError)
                  || (e instanceof TimeoutExceededException))) {
            throw new SequenceExceptionError(eseq, i, e);
          }
          return new InvalidChecks(new InvalidExceptionCheck(e, i, e.getClass().getName()));
        } else if (ExceptionBehaviorClassifier.classify(exec, eseq) == INVALID) {
          return new InvalidChecks(new InvalidExceptionCheck(e, i, e.getClass().getName()));
        }
      }
    }

    return InvalidChecks.EMPTY;
  }
}
