package randoop.test;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * A predicate to determine whether an exception is an {@code OutOfMemoryError}.
 */
public class OOMExceptionPredicate extends DefaultExceptionPredicate {

  /**
   * {@inheritDoc}
   * Tests whether the exception is a {@code OutOfMemoryError}.
   * 
   * @return true if the exception is an {@code OutOfMemoryError}, false otherwise
   */
  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    Throwable e = exec.getException();
    return (e instanceof OutOfMemoryError);
  }

}
