package randoop;

import randoop.sequence.Execution;

/**
 * An {@code ExceptionCheck} that doesn't enforce the expectation of an
 * exception by the statement at the statement index. Allows for execution of
 * the statement to be either normal or throw an exception.
 */
public class EmptyExceptionCheck extends ExceptionCheck {

  /**
   * Creates an exception check for the given statement index.
   *
   * @param exception
   *          the exception thrown by statement
   * @param statementIndex
   *          the position of statement in sequence
   * @param catchClassName
   *          the name of the exception class to be caught
   */
  public EmptyExceptionCheck(Throwable exception, int statementIndex, String catchClassName) {
    super(exception, statementIndex, catchClassName);
  }

  /**
   * {@inheritDoc} Appends comment indicating that catch is being ignored.
   */
  @Override
  protected void appendCatchBehavior(StringBuilder b, String exceptionClassName) {
    b.append("  // expected exception caught" + exceptionClassName + Globals.lineSep);
  }

  /**
   * {@inheritDoc} Appends comment indicating that failure to throw exception
   * being ignored.
   */
  @Override
  protected void appendTryBehavior(StringBuilder b, String exceptionClassName) {
    b.append("  // expected exception " + exceptionClassName + " not thrown" + Globals.lineSep);
  }

  /**
   * {@inheritDoc} This check passes if execution is either normal or an
   * exception is thrown.
   *
   * @return true if execution outcome is normal or an exception, false
   *         otherwise
   * @throws IllegalArgumentException
   *           if execution indicates statement not executed
   */
  @Override
  public boolean evaluate(Execution execution) {
    ExecutionOutcome outcomeAtIndex = execution.get(statementIndex);
    if (outcomeAtIndex instanceof NotExecuted) {
      throw new IllegalArgumentException("Statement not executed");
    }
    return true;
  }
}
