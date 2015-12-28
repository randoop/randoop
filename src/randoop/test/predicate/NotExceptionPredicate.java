package randoop.test.predicate;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * An {@code ExceptionPredicate} that negates the value returned by another
 * predicate.
 */
public class NotExceptionPredicate extends DefaultExceptionPredicate {

  private ExceptionPredicate p;

  /**
   * Creates a predicate that is the negation of the given predicate.
   * 
   * @param p  the negated predicate
   */
  public NotExceptionPredicate(ExceptionPredicate p) {
    this.p = p;
  }

  /**
   * {@inheritDoc}
   * @return true if p returns false, and false otherwise
   */
  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    // TODO Auto-generated method stub
    return ! p.test(exec, s);
  }

}
