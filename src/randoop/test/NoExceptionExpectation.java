package randoop.test;

import randoop.EmptyExceptionCheck;
import randoop.ExceptionCheck;
import randoop.ExceptionalExecution;
import randoop.reflection.VisibilityPredicate;

/**
 * Generates {@code EmptyExceptionCheck} objects for the given exception thrown
 * by a statement. These allow tests to passively catch and ignore exceptions
 * thrown in a regression test.
 */
public class NoExceptionExpectation extends DefaultRegressionExceptionCheckGenerator {

  /**
   * Create an object that generates empty exception checks for all exceptions.
   * 
   * @param visibility  the predicate to determine visibility of exception classes
   */
  public NoExceptionExpectation(VisibilityPredicate visibility) {
    super(visibility);
  }

  /**
   * {@inheritDoc}
   * @return an {@code EmptyExceptionCheck} to be attached to the statement
   */
  @Override
  public ExceptionCheck getExceptionCheck(ExceptionalExecution e, int statementIndex) {
    Throwable t = e.getException();
    return new EmptyExceptionCheck(e.getException(), 
                                   statementIndex, 
                                   getCatchClassName(t.getClass()));
  }

}
