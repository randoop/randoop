package randoop.test;

import randoop.EmptyExceptionCheck;
import randoop.ExceptionCheck;
import randoop.ExceptionalExecution;

/**
 * Generates {@code EmptyExceptionCheck} objects for the given exception thrown
 * by a statement. These allow tests to passively catch and ignore exceptions
 * thrown in a regression test.
 */
public class NoExceptionExpectation implements RegressionExceptionCheckGenerator {

  /**
   * {@inheritDoc}
   * @return an {@code EmptyExceptionCheck} to be attached to the statement
   */
  @Override
  public ExceptionCheck getExceptionCheck(ExceptionalExecution e, int statementIndex) {
    return new EmptyExceptionCheck(e.getException(), statementIndex);
  }

}
