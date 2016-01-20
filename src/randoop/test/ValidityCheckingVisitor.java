package randoop.test;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.SequenceExceptionError;
import randoop.test.predicate.ExceptionPredicate;

/**
 * A {@code ValidityCheckingVisitor} checks for occurrences of exceptions that
 * have been tagged as invalid behaviors as represented by a 
 * {@code ExceptionPredicate}.
 * Also, either ignores or reports flaky test sequences --- an input 
 * sequence that throws an exception in a longer test sequence, despite having
 * run normally by itself.
 * Ignored flaky sequences are classified as invalid.
 * Flaky occurrences of {@code OutOfMemoryError} are always treated as invalid.
 */
public class ValidityCheckingVisitor implements TestCheckGenerator {

  private ExceptionPredicate isInvalid;
  private boolean reportFlaky;

  /**
   * Creates an object that looks for invalid exceptions.
   * 
   * @param isInvalid  the predicate to test for invalid exceptions
   * @param reportFlaky  a flag indicating whether to report flaky tests by
   *                     throwing an exception
   */
  public ValidityCheckingVisitor(ExceptionPredicate isInvalid, boolean reportFlaky) {
    this.isInvalid = isInvalid;
    this.reportFlaky = reportFlaky;
  }

  /**
   * {@inheritDoc}
   * Checks validity of a test sequence and creates a {@code InvalidChecks}
   * object containing checks for any invalid exceptions encountered.
   * Exceptions are classified by the {@code ExceptionPredicate}, but a 
   * sequence where an {@code OutOfMemoryError} is seen before the last statement
   * is classified as invalid regardless of how {@code OutOfMemoryError} is
   * classified by the predicate.
   * 
   * @return a possibly empty {@link InvalidChecks} object for sequence
   * @throws Error if any exception encountered before last statement of sequence
   */
  @Override
  public TestChecks visit(ExecutableSequence s) {
    InvalidChecks checks = new InvalidChecks();
    int finalIndex = s.sequence.size() - 1;
    for (int i = 0; i < s.sequence.size(); i++) {
      ExecutionOutcome result = s.getResult(i);
      if (result instanceof ExceptionalExecution) {
        ExceptionalExecution exec = (ExceptionalExecution)result;
        Throwable e = exec.getException();
        
        if (i != finalIndex) {
          if (reportFlaky && ! (e instanceof OutOfMemoryError)) {
            throw new SequenceExceptionError(s, i, e);            
          }
          checks.add(new InvalidExceptionCheck(e, i, e.getClass().getName()));
        }
        
        if (isInvalid.test(exec, s)) {
          checks.add(new InvalidExceptionCheck(e, i, e.getClass().getName()));
        }
      }
    }
    
    return checks;
  }

}
