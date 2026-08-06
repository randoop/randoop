package randoop.util;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeoutException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.main.OptionsCache;

/** Tests for {@link ReflectionExecutor}. */
public class ReflectionExecutorTest {

  /** The saved state of the command-line arguments. */
  private static OptionsCache optionsCache;

  @BeforeClass
  public static void setup() {
    optionsCache = new OptionsCache();
    optionsCache.saveState();
  }

  @AfterClass
  public static void restore() {
    optionsCache.restoreState();
  }

  /** Code that runs until it is interrupted. */
  private static class NonTerminatingCode extends ReflectionCode {

    /** Creates a NonTerminatingCode. */
    NonTerminatingCode() {}

    @Override
    protected void runReflectionCodeRaw() {
      try {
        while (true) {
          Thread.sleep(10);
        }
      } catch (InterruptedException e) {
        // Permit this thread to terminate.
      }
    }

    @Override
    public String toString() {
      return "NonTerminatingCode: " + status();
    }
  }

  /**
   * With {@code --usethreads}, a call that exceeds the timeout is reported as an {@link
   * ExceptionalExecution} that holds a {@code TimeoutException}, on every JDK version. (As of JDK
   * 20, {@code Thread.stop()} throws {@code UnsupportedOperationException} rather than stopping the
   * thread.)
   */
  @Test
  public void testTimeoutIsReportedAsTimeoutException() {
    ReflectionExecutor.usethreads = true;
    ReflectionExecutor.call_timeout_millis = 100;

    ExecutionOutcome outcome = ReflectionExecutor.executeReflectionCode(new NonTerminatingCode());

    assertTrue(
        "expected an ExceptionalExecution, got: " + outcome,
        outcome instanceof ExceptionalExecution);
    Throwable thrown = ((ExceptionalExecution) outcome).getException();
    assertTrue("expected a TimeoutException, got: " + thrown, thrown instanceof TimeoutException);
  }
}
