package randoop.test;

import randoop.Globals;

/**
 * An {@code ExceptionCheck} that doesn't enforce the expectation of an exception by the statement
 * at the statement index. Allows for execution of the statement to be either normal or throw an
 * exception.
 */
public class EmptyExceptionCheck extends ExceptionCheck {

  /**
   * Creates an exception check for the given statement index.
   *
   * @param exception the exception thrown by statement
   * @param statementIndex the position of statement in sequence
   * @param catchClassName the name of the exception class to be caught
   */
  EmptyExceptionCheck(Throwable exception, int statementIndex, String catchClassName) {
    super(exception, statementIndex, catchClassName);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Appends comment indicating that catch is being ignored.
   */
  @Override
  protected void appendCatchBehavior(StringBuilder b) {
    String message = "expected exception caught " + getExceptionName();
    if (exception.getClass().isAnonymousClass()) {
      message = "anonymous exception caught";
    }
    b.append("  // ").append(message).append(Globals.lineSep);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Appends comment indicating that failure to throw exception being ignored.
   */
  @Override
  protected void appendTryBehavior(StringBuilder b) {
    String message = "expected exception " + getExceptionName();
    if (exception.getClass().isAnonymousClass()) {
      message = "expected anonymous exception";
    }
    b.append("  // ").append(message).append(" not thrown").append(Globals.lineSep);
  }
}
