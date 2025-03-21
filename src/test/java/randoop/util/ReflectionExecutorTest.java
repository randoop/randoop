package randoop.util;

import static org.junit.Assert.*;

import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;

/**
 * Tests for ReflectionExecutor to verify normal execution, timeout behavior, and exception
 * handling.
 */
public class ReflectionExecutorTest {

  @Before
  public void setUp() {
    ReflectionExecutor.resetStatistics();
    ReflectionExecutor.usethreads = false; // default: unthreaded mode
    ReflectionExecutor.call_timeout_millis = ReflectionExecutor.CALL_TIMEOUT_MILLIS_DEFAULT;
  }

  /** Test normal execution in unthreaded mode. */
  @Test
  public void testNormalExecutionUnthreaded() {
    ReflectionCode code =
        new ReflectionCode() {
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
    assertEquals("Expected return value 42", 42, normal.getRuntimeValue());
    assertTrue("Execution time should be non-negative", normal.getExecutionTimeNanos() >= 0);
    assertEquals("Normal execution count should be 1", 1, ReflectionExecutor.normalExecs());
  }

  /** Test normal execution in threaded mode. */
  @Test
  public void testNormalExecutionThreaded() {
    ReflectionExecutor.usethreads = true;
    ReflectionCode code =
        new ReflectionCode() {
          private Object retval;
          private Throwable exception;

          @Override
          public void runReflectionCodeRaw() {
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
    assertEquals("Expected return value 'success'", "success", normal.getRuntimeValue());
    assertTrue("Execution time should be non-negative", normal.getExecutionTimeNanos() >= 0);
    assertEquals("Normal execution count should be 1", 1, ReflectionExecutor.normalExecs());
  }

  /** Test that a long-running ReflectionCode times out when using threads. */
  @Test
  public void testTimeoutExecution() {
    ReflectionExecutor.usethreads = true;
    ReflectionExecutor.call_timeout_millis = 100;
    ReflectionCode code =
        new ReflectionCode() {
          private Object retval;
          private Throwable exception;

          @Override
          public void runReflectionCodeRaw() {
            try {
              Thread.sleep(200);
            } catch (InterruptedException e) {

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
    assertTrue(
        "Outcome should be an ExceptionalExecution", outcome instanceof ExceptionalExecution);
    ExceptionalExecution exceptional = (ExceptionalExecution) outcome;
    assertTrue(
        "Expected a TimeoutException", exceptional.getException() instanceof TimeoutException);
    long execTime = exceptional.getExecutionTimeNanos();
    assertTrue(
        "Execution time should be at least the timeout duration",
        execTime >= ReflectionExecutor.call_timeout_millis * 1_000_000L);
    assertEquals("Exceptional execution count should be 1", 1, ReflectionExecutor.excepExecs());
  }

  /** Test that a ReflectionCode that throws an exception is handled correctly. */
  @Test
  public void testExceptionExecution() {
    ReflectionExecutor.usethreads = false;
    ReflectionCode code =
        new ReflectionCode() {
          private Object retval;
          private Throwable exception;

          @Override
          public void runReflectionCodeRaw() {
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
      assertTrue(
          "Exception message should contain 'Test Exception'",
          e.getMessage().contains("Test Exception"));
    }
  }
}
