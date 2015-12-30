package randoop.test;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.sequence.ExecutableSequence;
import randoop.test.predicate.ExceptionPredicate;

/**
 * A {@code ValidityCheckingVisitor} checks for occurrences of exceptions that
 * have been tagged as invalid behaviors as represented by a 
 * {@code ExceptionPredicate}.
 */
public class ValidityCheckingVisitor implements TestCheckGenerator {

  private ExceptionPredicate isInvalid;

  /**
   * Creates an object that looks for invalid exceptions.
   * 
   * @param isInvalid  the predicate to test for invalid exceptions
   */
  public ValidityCheckingVisitor(ExceptionPredicate isInvalid) {
    this.isInvalid = isInvalid;
  }

  /**
   * {@inheritDoc}
   * Checks validity of a test sequence and creates a {@code InvalidChecks}
   * object containing checks for any invalid exceptions encountered.
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
        if (i != finalIndex) {
          throw new Error("Exception thrown before end of sequence");
        }
        
        ExceptionalExecution exec = (ExceptionalExecution)result;
        if (isInvalid.test(exec, s)) {
          Throwable e = exec.getException();
          checks.add(new InvalidExceptionCheck(e, i, e.getClass().getName()));
        }
      }
    }
    
    return checks;
  }

}
