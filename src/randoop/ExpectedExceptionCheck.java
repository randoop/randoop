package randoop;

import randoop.sequence.Execution;
import randoop.util.Reflection;

/**
 * An {@code ExceptionCheck} that enforces the expectation of an exception being
 * thrown. In particular,
 * <ul>
 * <li> fails if exception is not thrown, and
 * <li> succeeds only when expected exception is thrown.
 * </ul>
 */
public class ExpectedExceptionCheck extends ExceptionCheck {

  private static final long serialVersionUID = -1172907532417774517L;

  public ExpectedExceptionCheck(Throwable exception, int statementIndex) {
    super(exception,statementIndex);
  }

  /**
   * {@inheritDoc}
   * Appends a fail assertion after statement in try block.
   */
  @Override
  protected void appendTryBehavior(StringBuilder b, String exceptionClassName) {
    String assertion = "org.junit.Assert.fail(\"Expected exception of type " 
                      + exceptionClassName + "\")";
    b.append("  " + assertion + ";" + Globals.lineSep);
  }
  
  /**
   * {@inheritDoc}
   * Appends assertion to confirm expected exception caught.
   */
  @Override
  protected void appendCatchBehavior(StringBuilder b, String exceptionClassName) {
    String condition = "! e.getClass().getCanonicalName().equals(\"" 
                      + exceptionClassName + "\")";
    String assertion = "org.junit.Assert.fail(\"Expected exception of type " 
                      + exceptionClassName 
                      + ", got \" + e.getClass().getCanonicalName())";
    b.append("  if (" + condition + ") {" + Globals.lineSep);
    b.append("    " + assertion + ";" + Globals.lineSep);
    b.append("  }" + Globals.lineSep);
  }

  /**
   * {@inheritDoc}
   * Checks that an exception of the expected type is thrown by the statement
   * in this object in the given {@code Execution}.
   * @return true if statement throws the expected exception, and false otherwise 
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
    ExceptionalExecution e = (ExceptionalExecution)outcomeAtIndex;
    return Reflection.canBeUsedAs(e.getException().getClass(), exception.getClass());
  }

}
