package randoop.test;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.Globals;
import randoop.NotExecuted;
import randoop.sequence.Execution;

/**
 * An {@code ExceptionCheck} that enforces the expectation of an exception being
 * thrown. In particular,
 * <ul>
 * <li>fails if exception is not thrown, and
 * <li>succeeds only when expected exception is thrown.
 * </ul>
 */
public class ExpectedExceptionCheck extends ExceptionCheck {

  /**
   * Creates check that enforces expectation that an exception is thrown by the
   * statement at the statement index.
   *
   * @param exception
   *          the expected exception
   * @param statementIndex
   *          the index of the statement in the sequence where exception is
   *          thrown
   * @param catchClassName
   *          the name of exception to be caught
   */
  ExpectedExceptionCheck(Throwable exception, int statementIndex, String catchClassName) {
    super(exception, statementIndex, catchClassName);
  }

  /**
   * {@inheritDoc} Appends a fail assertion after statement in try block.
   */
  @Override
  protected void appendTryBehavior(StringBuilder b) {
    String message;
    if (exception.getClass().isAnonymousClass()) {
      message = "Expected anonymous exception";
    } else {
      message = "Expected exception of type " + getExceptionName();
    }
    String assertion = "org.junit.Assert.fail(\"" + message + "\")";
    b.append("  ").append(assertion).append(";").append(Globals.lineSep);
  }

  /**
   * {@inheritDoc} Appends assertion to confirm expected exception caught.
   */
  @Override
  protected void appendCatchBehavior(StringBuilder b) {
    String condition;
    String message;
    if (exception.getClass().isAnonymousClass()) {
      condition = "e.getClass().isAnonymousClass()";
      message = "Expected anonymous exception, got \" + e.getClass().getCanonicalName()";
      String assertion = "org.junit.Assert.fail(\"" + message + ")";
      b.append("  if (! ").append(condition).append(") {").append(Globals.lineSep);
      b.append("    ").append(assertion).append(";").append(Globals.lineSep);
      b.append("  }").append(Globals.lineSep);
    }
  }

  /**
   * {@inheritDoc} Checks that an exception of the expected type is thrown by
   * the statement in this object in the given {@link Execution}.
   *
   * @return true if statement throws the expected exception, and false
   *         otherwise
   */
  @Override
  public boolean evaluate(Execution execution) {
    ExecutionOutcome outcomeAtIndex = execution.get(statementIndex);
    if (outcomeAtIndex instanceof NotExecuted) {
      throw new IllegalArgumentException("Statement not executed");
    }
    if (!(outcomeAtIndex instanceof ExceptionalExecution)) {
      return false;
    }
    ExceptionalExecution e = (ExceptionalExecution) outcomeAtIndex;
    // TODO verify that this substitution still works!!!
    return exception.getClass().isAssignableFrom(e.getException().getClass());
  }
}
