package randoop.test.predicate;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * A predicate that determines if an exception is unchecked.
 */
public class UncheckedExceptionPredicate extends DefaultExceptionPredicate {

  /**
   * {@inheritDoc}
   * Tests whether the exception is unchecked.
   * 
   * @return true if exception is unchecked, false otherwise
   */
  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    Throwable e = exec.getException();
    return (e instanceof Error || e instanceof RuntimeException);
  }

}
