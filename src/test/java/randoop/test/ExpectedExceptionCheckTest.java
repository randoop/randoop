package randoop.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests the failure message that {@link ExpectedExceptionCheck} puts in generated tests. */
public class ExpectedExceptionCheckTest {

  /**
   * Returns the code that a generated test contains after a statement that throws the given
   * exception.
   *
   * @param exception the exception that the statement throws
   * @return the code after the statement
   */
  private static String postStatementCode(Throwable exception) {
    return new ExpectedExceptionCheck(exception, 0, exception.getClass().getCanonicalName())
        .toCodeStringPostStatement();
  }

  /** The message of an exception that the JVM may throw implicitly is omitted. */
  @Test
  public void testImplicitExceptionMessageOmitted() {
    String code = postStatementCode(new NullPointerException("detailed message"));
    assertTrue(
        code,
        code.contains(
            "org.junit.Assert.fail(\"Expected exception of type java.lang.NullPointerException\");"));
    assertFalse(code, code.contains("detailed message"));
  }

  /** The message of any other exception is included. */
  @Test
  public void testOtherExceptionMessageIncluded() {
    String code = postStatementCode(new IllegalArgumentException("detailed message"));
    assertTrue(
        code,
        code.contains(
            "org.junit.Assert.fail(\"Expected exception of type"
                + " java.lang.IllegalArgumentException; message: detailed message\");"));
  }
}
