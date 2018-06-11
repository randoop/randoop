package randoop.test;

import org.plumelib.util.UtilPlume;
import randoop.Globals;

/**
 * An {@code ExceptionCheck} that enforces the expectation of an exception being thrown. In
 * particular,
 *
 * <ul>
 *   <li>fails if exception is not thrown, and
 *   <li>succeeds only when expected exception is thrown.
 * </ul>
 */
public class ExpectedExceptionCheck extends ExceptionCheck {

  /**
   * Creates check that enforces expectation that an exception is thrown by the statement at the
   * statement index.
   *
   * <p>These are created before the test is classified as normal, exceptional, or invalid behavior.
   * For example, this could be created with a TimeoutExceededException, but the sequence would
   * later be classified as invalid.
   *
   * @param exception the expected exception
   * @param statementIndex the index of the statement in the sequence where exception is thrown
   * @param catchClassName the name of exception to be caught
   */
  public ExpectedExceptionCheck(Throwable exception, int statementIndex, String catchClassName) {
    super(exception, statementIndex, catchClassName);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Appends a fail assertion after statement in try block.
   */
  @Override
  protected void appendTryBehavior(StringBuilder b) {
    String message;
    if (exception.getClass().isAnonymousClass()) {
      message = "Expected anonymous exception";
    } else {
      message =
          "Expected exception of type "
              + getExceptionName()
              + "; message: "
              + exception.getMessage();
    }
    String assertion = "org.junit.Assert.fail(\"" + UtilPlume.escapeNonJava(message) + "\")";
    b.append(Globals.lineSep).append("  ").append(assertion).append(";").append(Globals.lineSep);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Appends assertion to confirm expected exception caught.
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
}
