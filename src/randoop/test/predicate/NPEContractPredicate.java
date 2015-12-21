package randoop.test.predicate;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * Checks whether an {@code ExceptionalExecution} for a {@code ExecutableSequence}
 * is a NullPointerException in a sequence where a null value is given as input.
 */
public class NPEContractPredicate extends DefaultExceptionPredicate {

  /**
   * {@inheritDoc}
   * Tests whether NullPointerException occurred when null given as input.
   * 
   * @return true if NPE thrown when null given as input, false otherwise
   */
  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    Throwable exception = exec.getException();
    return (exception instanceof NullPointerException && s.hasNullInput());
  }

}
