package randoop.util;

import static org.junit.Assert.*;

import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.ExceptionalExecution;
import randoop.main.RandoopBug;

/**
 * Tests for ReflectionExecutor to verify normal execution, timeout behavior,
 * and exception handling.
 *
 * Note: ReflectionCode has a final runReflectionCode() method that delegates to
 * the abstract runReflectionCodeRaw(), so we only override runReflectionCodeRaw().
 */
public class ReflectionExecutorTest {

  @Before
  public void setUp() {
    ReflectionExecutor.resetStatistics();
    ReflectionExecutor.usethreads = false; // default: unthreaded mode
    ReflectionExecutor.call_timeout = ReflectionExecutor.CALL_TIMEOUT_MILLIS_DEFAULT;
  }

  /**
   * Test normal execution in unthreaded mode.
   */
  @Test
  public void testNormalExecutionUnthreaded() {
    ReflectionCode code = new ReflectionCode() {
      private Object retval;
      private Throwable exception;

      @Override
      public void runReflectionCodeRaw() {
        // Simulate a successful execution by setting the return value.
        retval = 42;
      }

      @Override
      public Object getReturnValue() {
        return retval;
      }

      @Override
      public Throwable getExceptionThrown() {
        return exception;
      }
    };

    ExecutionOutcome outcome = ReflectionExecutor.executeReflectionCode(code);
    assertTrue("Outcome should be a NormalExecution", outcome instanceof NormalExecution);
    NormalExecution normal = (NormalExecution) outcome;
    // Use getRuntimeValue() instead of getValue()
    assertEquals("Expected return value 42", 42, normal.getRuntimeValue());
    // Verify execution time (in nanoseconds)
    assertTrue("Execution time should be non-negative", normal.getExecutionTimeNanos() >= 0);
    assertEquals("Normal execution count should be 1", 1, ReflectionExecutor.normalExecs());
  }

  /**
   * Test normal execution in threaded mode.
   */
  @Test
  public void testNormalExecutionThreaded() {
    ReflectionExecutor.usethreads = true;
    ReflectionCode code = new ReflectionCode() {
      private Object retval;
      private Throwable exception;

      @Override
      public void runReflectionCodeRaw() {
        // Simulate a successful execution by setting the return value.
        retval = "success";
      }

      @Override
      public Object getReturnValue() {
        return retval;
      }

      @Override
      public Throwable getExceptionThrown() {
        return exception;
      }
    };

    ExecutionOutcome outcome = ReflectionExecutor.executeReflectionCode(code);
    assertTrue("Outcome should be a NormalExecution", outcome instanceof NormalExecution);
    NormalExecution normal = (NormalExecution) outcome;
    // Use getRuntimeValue() instead of getValue()
    assertEquals("Expected return value 'success'", "success", normal.getRuntimeValue());
    assertTrue("Execution time should be non-negative", normal.getExecutionTimeNanos() >= 0);
    assertEquals("Normal execution count should be 1", 1, ReflectionExecutor.normalExecs());
  }

  /**
   * Test that a long-running ReflectionCode times out when using threads.
   */
  @Test
  public void testTimeoutExecution() {
    ReflectionExecutor.usethreads = true;
    // Set a short timeout (e.g., 100 milliseconds) for testing.
    ReflectionExecutor.call_timeout = 100;
    ReflectionCode code = new ReflectionCode() {
      private Object retval;
      private Throwable exception;

      @Override
      public void runReflectionCodeRaw() {
        try {
          // Sleep longer than the timeout to force a timeout.
          Thread.sleep(200);
        } catch (InterruptedException e) {
          // Expected interruption when timeout occurs.
        }
      }

      @Override
      public Object getReturnValue() {
        return retval;
      }

      @Override
      public Throwable getExceptionThrown() {
        return exception;
      }
    };

    ExecutionOutcome outcome = ReflectionExecutor.executeReflectionCode(code);
    assertTrue("Outcome should be an ExceptionalExecution", outcome instanceof ExceptionalExecution);
    ExceptionalExecution exceptional = (ExceptionalExecution) outcome;
    assertTrue("Expected a TimeoutException", 
               exceptional.getException() instanceof TimeoutException);
    // Check that the reported execution time is at least the timeout duration (in nanoseconds).
    long execTime = exceptional.getExecutionTimeNanos();
    assertTrue("Execution time should be at least the timeout duration",
               execTime >= ReflectionExecutor.call_timeout * 1_000_000L);
    assertEquals("Exceptional execution count should be 1", 1, ReflectionExecutor.excepExecs());
  }

  /**
   * Test that a ReflectionCode that throws an exception is handled correctly.
   */
  @Test
  public void testExceptionExecution() {
    ReflectionExecutor.usethreads = false;
    ReflectionCode code = new ReflectionCode() {
      private Object retval;
      private Throwable exception;

      @Override
      public void runReflectionCodeRaw() {
        // Simulate an error by throwing an exception.
        throw new RuntimeException("Test Exception");
      }

      @Override
      public Object getReturnValue() {
        return retval;
      }

      @Override
      public Throwable getExceptionThrown() {
        return exception;
      }
    };

    try {
      ReflectionExecutor.executeReflectionCode(code);
      fail("Expected RuntimeException to be thrown");
    } catch (RuntimeException e) {
      assertTrue("Exception message should contain 'Test Exception'", 
                 e.getMessage().contains("Test Exception"));
    }
  }
}
