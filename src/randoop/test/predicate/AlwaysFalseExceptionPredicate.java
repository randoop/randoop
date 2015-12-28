package randoop.test.predicate;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * An exception predicate that always returns false.
 */
public class AlwaysFalseExceptionPredicate implements ExceptionPredicate {

  /**
   * {@inheritDoc}
   * @return false, always
   */
  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    return false;
  }

  /**
   * {@inheritDoc}
   * @return the second predicate
   */
  @Override
  public ExceptionPredicate or(ExceptionPredicate p) {
    return p;
  }

  /**
   * {@inheritDoc}
   * @return this predicate since the and-also always returns false
   */
  @Override
  public ExceptionPredicate and(ExceptionPredicate p) {
    return this;
  }

  /**
   * {@inheritDoc}
   * @return a predicate that always returns true
   */
  @Override
  public ExceptionPredicate not() {
    return new AlwaysTrueExceptionPredicate();
  }

}
