package randoop.test;

import randoop.ExceptionCheck;
import randoop.ExceptionalExecution;
import randoop.ExpectedExceptionCheck;

/**
 * Generates {@code ExpectedExceptionCheck} objects for all exceptions. These
 * objects enforce expectation that the exception will be thrown by the
 * statement, and that it is a failure if it does not.
 */
public class ExpectAllExceptions implements RegressionExceptionCheckGenerator {
  
  /**
   * {@inheritDoc}
   * @return an {@code ExpectedExceptionCheck} for the exception at the statement
   */
  @Override
  public ExceptionCheck getExceptionCheck(ExceptionalExecution e, int statementIndex) {
    Throwable exception = e.getException();
    return new ExpectedExceptionCheck(exception,statementIndex);
  }

}
