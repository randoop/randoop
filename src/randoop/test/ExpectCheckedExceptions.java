package randoop.test;

import randoop.EmptyExceptionCheck;
import randoop.ExceptionCheck;
import randoop.ExceptionalExecution;
import randoop.ExpectedExceptionCheck;

/**
 * Generates {@code ExpectedExceptionCheck} objects when given a {@code Checked}
 * exception, and an {@code EmptyExceptionCheck} otherwise.  Resulting tests only
 * enforce expected checked exceptions.
 */
public class ExpectCheckedExceptions implements RegressionExceptionCheckGenerator {

  /**
   * {@inheritDoc}
   * @return an {@code ExpectedExceptionCheck} object for exception and statement
   * if exception is checked, and an {@code EmptyExceptionCheck} otherwise
   */
  @Override
  public ExceptionCheck getExceptionCheck(ExceptionalExecution exec, int statementIndex) {
    Throwable e = exec.getException();
    if (! (e instanceof Error || e instanceof RuntimeException)) {
      return new ExpectedExceptionCheck(e, statementIndex);
    } else {
      return new EmptyExceptionCheck(e, statementIndex);
    }
  }

}
