package randoop;

import randoop.sequence.Execution;

/**
 * An {@code ExceptionCheck} that doesn't enforce the expectation of an
 * exception at a particular statement. Allows for execution at the statement 
 * to be either normal or throw an exception.
 */
public class EmptyExceptionCheck extends ExceptionCheck {
  
  private static final long serialVersionUID = 8159839339961663076L;
  
  /**
   * Creates an exception check at the given statement.
   * 
   * @param exception  the exception thrown by statement
   * @param statementIndex  the position of statement in sequence
   */
  public EmptyExceptionCheck(Throwable exception, int statementIndex) {
    super(exception, statementIndex);
  }

  /**
   * {@inheritDoc}
   * Appends comment indicating that catch is being ignored.
   */
  @Override
  protected void appendCatchBehavior(StringBuilder b, String exceptionClassName) {
    b.append("  // ignoring fact that caught expected exception " + exceptionClassName);
  }

  /**
   * {@inheritDoc}
   * Appends comment indicating that failure to throw exception being ignored. 
   */
  @Override
  protected void appendTryBehavior(StringBuilder b, String exceptionClassName) {
    b.append("  // ignoring fact that expected exception " + exceptionClassName + " not thrown");
  }

  /**
   * {@inheritDoc}
   * This check passes if execution is either normal or an exception is thrown.
   * @return true if execution outcome is normal or and exception, false otherwise
   * @throws IllegalArgumentException if execution indicates statement not executed 
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
