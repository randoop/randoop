package randoop.test.predicate;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * Implements an and-also operator on {@code ExceptionPredicate} objects.
 */
public class AndExceptionPredicate extends DefaultExceptionPredicate {

  private ExceptionPredicate first;
  private ExceptionPredicate second;

  /**
   * Create a predicate that performs an and-also operator on the given predicates.
   * Tests the first predicate, and if true, returns the result of the second.
   * 
   * @param first  the first predicate to test
   * @param second  the second predicate to test
   */
  public AndExceptionPredicate(ExceptionPredicate first, ExceptionPredicate second) {
    this.first = first;
    this.second = second;
  }

  /**
   * {@inheritDoc}
   * @return true if first and second predicate both return true, false otherwise
   */
  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    return first.test(exec, s) && second.test(exec, s);
  }

}
