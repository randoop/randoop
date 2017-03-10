package randoop.test.predicate;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * An {@code ExceptionPredicate} is a test on an exception primarily used to check whether the
 * exception is classified as expected. Used in {@link randoop.test.TestCheckGenerator}
 * implementations to decide whether an exception matches criteria of that check generator.
 */
public interface ExceptionPredicate {

  /**
   * Check whether the {@code ExceptionalExecution} of the {@code ExecutableSequence} satisfies the
   * criterion of the predicate.
   *
   * @param exec the exceptional execution wrapping an exception
   * @param s the sequence where exception was thrown
   * @return true if exception satisfies the predicate, and false otherwise
   */
  boolean test(ExceptionalExecution exec, ExecutableSequence s);
}
