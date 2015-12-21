package randoop.test;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * Predicate to determine whether an exception is checked.
 */
public class CheckedExceptionPredicate extends DefaultExceptionPredicate {

  /**
   * {@inheritDoc}
   * Test whether the exception is checked.
   * 
   * @return true if exception is not unchecked, and false otherwise
   */
  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    Throwable e = exec.getException();
    return ! (e instanceof Error || e instanceof RuntimeException);
  }

}
