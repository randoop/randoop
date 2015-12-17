package randoop.test;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * An error exception predicate is used to determine whether an exception in
 * a given sequence is to be considered an error (e.g., a failure).
 */
public interface FailureExceptionPredicate {
  
  /**
   * Test predicate on the {@code ExceptionalExecution} for the given 
   * {@code ExecutableSequence}.
   * 
   * @param exec  the exceptional execution
   * @param s  the sequence in which exception occurred
   * @return true if exception is considered an error, and false otherwise
   */
  boolean test(ExceptionalExecution exec, ExecutableSequence s);
}
