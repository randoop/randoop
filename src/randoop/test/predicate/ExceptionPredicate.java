package randoop.test.predicate;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * An exception predicate is used to determine whether an exception in
 * a given sequence meets an implementing criteria.
 */
public interface ExceptionPredicate {
  
  /**
   * Test predicate on the {@code ExceptionalExecution} for the given 
   * {@code ExecutableSequence}.
   * 
   * @param exec  the exceptional execution
   * @param s  the sequence in which exception occurred
   * @return true if exception satisfies the predicate, and false otherwise
   */
  boolean test(ExceptionalExecution exec, ExecutableSequence s);

  /**
   * Creates a new predicate that performs an or-else operator on this and the
   * given predicate.
   * 
   * @param p  the predicate to check if this predicate returns false
   * @return a predicate that returns true if this or the second predicate 
   * returns true, and is false if neither return true
   */
  ExceptionPredicate or(ExceptionPredicate p);
  
  /**
   * Creates a new predicate that performs an and-also operator on this and the
   * give predicate.
   * 
   * @param p  the predicate to check if this predicate returns true
   * @return  a predicate that returns true if this and the second predicate
   * return true, and is false otherwise
   */
  ExceptionPredicate and(ExceptionPredicate p);

  /**
   * Creates a new predicate that returns the negation of the given predicate.
   * 
   * @param p  the predicate to negate
   * @return a predicate that returns the negation of {@code p}
   */
  ExceptionPredicate not();
}
