package randoop.test;

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
   * @param exception the expected exception
   * @param statementIndex the index of the statement in the sequence where exception is thrown
   * @param catchClassName the name of exception to be caught
   */
  ExpectedExceptionCheck(Throwable exception, int statementIndex, String catchClassName) {
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
    message = fixMessage(message);
    String assertion = "org.junit.Assert.fail(\"" + message + "\")";
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

  /**
   * Ensures that the fail message built from an exception message is formatted propertly for use in
   * an assertion by removing newlines.
   *
   * @param message the message to convert
   * @return the message with newlines removed
   */
  private static String fixMessage(String message) {
    return message.replaceAll("[\\r\\n]+", ".");
  }
}
