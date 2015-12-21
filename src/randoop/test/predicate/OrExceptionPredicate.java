package randoop.test.predicate;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * An {@code ExceptionPredicate} that implements a short-circuited or operator
 * on two predicates. 
 * This operator only tests the second predicate if the first returns false.
 */
public class OrExceptionPredicate extends DefaultExceptionPredicate {

  private ExceptionPredicate first;
  private ExceptionPredicate second;

  /**
   * Constructs a predicate that will test the first predicate and then the 
   * second if the first returns false.
   * 
   * @param first  the predicate to test first
   * @param second  the predicate to test second
   */
  public OrExceptionPredicate(ExceptionPredicate first, ExceptionPredicate second) {
    if (first == null || second == null) {
      throw new IllegalArgumentException("Predicate arguments must be non-null");
    }
    this.first = first;
    this.second = second;
  }

  /**
   * {@inheritDoc}
   * Return the short-circuited or of the two predicates for the exceptional 
   * execution of the given sequence.
   * 
   * @return true if either the first or second predicate is true, and false otherwise
   */
  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    return first.test(exec, s) || second.test(exec, s);
  }

}
