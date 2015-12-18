package randoop.test;

import randoop.EmptyExceptionCheck;
import randoop.ExceptionCheck;
import randoop.ExceptionalExecution;
import randoop.ExpectedExceptionCheck;
import randoop.reflection.VisibilityPredicate;

/**
 * Generates {@code ExpectedExceptionCheck} objects when given a checked
 * exception, and an {@code EmptyExceptionCheck} otherwise.  Resulting tests only
 * enforce expected checked exceptions.
 */
public class ExpectCheckedExceptions extends DefaultRegressionExceptionCheckGenerator {

  /**
   * Create an object that generates expected exception checks for checked
   * exceptions and empty exception checks for other exceptions.
   * 
   * @param visibility  a predicate to determine visibility of exception classes
   */
  public ExpectCheckedExceptions(VisibilityPredicate visibility) {
    super(visibility);
  }
  
  /**
   * {@inheritDoc}
   * @return an {@code ExpectedExceptionCheck} object for exception and statement
   * if exception is checked, and an {@code EmptyExceptionCheck} otherwise
   */
  @Override
  public ExceptionCheck getExceptionCheck(ExceptionalExecution exec, int statementIndex) {
    Throwable e = exec.getException();
    String catchClassName = getCatchClassName(e.getClass());
    if (! (e instanceof Error || e instanceof RuntimeException)) {
      return new ExpectedExceptionCheck(e, statementIndex, catchClassName);
    } else {
      return new EmptyExceptionCheck(e, statementIndex, catchClassName);
    }
  }

}
