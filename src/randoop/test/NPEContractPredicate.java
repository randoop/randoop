package randoop.test;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * Checks whether an {@code ExceptionalExecution} for a {@code ExecutableSequence}
 * satisfies the criteria for the No-NullPointerException contract, which states
 * that it is an error if a NullPointerException is thrown when no null input is given.
 * Used to determine whether an NPE should be treated as an error.
 * <p>
 * Must be chained with another predicate.
 */
public class NPEContractPredicate implements FailureExceptionPredicate {

  private FailureExceptionPredicate predicate;

  /**
   * Creates a predicate object that first checks for NPE contract and then 
   * calls another predicate.
   * 
   * @param predicate  the predicate to call.
   */
  public NPEContractPredicate(FailureExceptionPredicate predicate) {
    if (predicate == null) {
      throw new IllegalArgumentException("Predicate must be non-null");
    }
    this.predicate = predicate;
  }

  /**
   * {@inheritDoc}
   * Tests whether no-NullPointerException contract is violated, and if not
   * returns result of chained predicate.
   * 
   * @return true if either no-NPE contract is violated or other predicate 
   * returns true, false otherwise
   */
  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    Throwable exception = exec.getException();
    
    return (exception instanceof NullPointerException && !(s.hasNullInput()))
        || predicate.test(exec, s);
  }

}
