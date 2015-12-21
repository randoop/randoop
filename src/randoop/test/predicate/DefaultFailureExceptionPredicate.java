package randoop.test;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * Implements a test for exceptions that are considered to be errors by default.
 * This include {@code AssertionError} and {@code StackOverflowError}.
 */
public class DefaultFailureExceptionPredicate extends DefaultExceptionPredicate {

  /**
   * {@inheritDoc}
   * @return true if exception is either {@code AssertionError} or {@code StackOverflowError},
   * and false otherwise
   */
  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    Throwable exception = exec.getException();
    
    return (exception instanceof AssertionError) 
        || (exception instanceof StackOverflowError);
  }

}
