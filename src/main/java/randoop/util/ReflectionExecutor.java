package randoop.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.plumelib.options.Option;
import org.plumelib.options.OptionGroup;
import org.plumelib.util.FileWriterWithName;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.main.RandoopBug;

/**
 * Static methods that execute the code of a ReflectionCode object.
 *
 * <p>This class uses an ExecutorService to run tests on multiple threads. When --usethreads is
 * true, each test is run on a separate thread with a timeout. If a test exceeds the timeout, it is
 * canceled and reported as a timeout.
 */
public final class ReflectionExecutor {

  private ReflectionExecutor() {
    throw new Error("Do not instantiate");
  }

  /**
   * If true, Randoop executes each test in a separate thread and kills tests that take too long to
   * finish, as determined by the --call-timeout command-line argument. Tests killed in this manner
   * are not reported to the user, but are recorded in Randoop's log.
   */
  @OptionGroup("Threading")
  @Option("Execute each test in a separate thread, with timeout")
  public static boolean usethreads = false;

  /**
   * If specified, Randoop logs timed-out tests to the specified file. Has no effect unless the
   * {@code --usethreads} command-line option is given.
   */
  @Option("<filename> logs timed-out tests to the specified file")
  public static FileWriterWithName timed_out_tests = null;

  /** Default for call_timeout, in milliseconds. */
  public static int CALL_TIMEOUT_MILLIS_DEFAULT = 5000;

  /** Maximum number of milliseconds a test may run. Only meaningful with --usethreads. */
  @Option("Maximum number of milliseconds a test may run. Only meaningful with --usethreads")
  public static int call_timeout = CALL_TIMEOUT_MILLIS_DEFAULT;

  // Execution statistics.
  /** The sum of durations for normal executions, in nanoseconds. */
  private static long normal_exec_duration_nanos = 0;

  /** The number of normal executions. */
  private static int normal_exec_count = 0;

  /** The sum of durations for exceptional executions, in nanoseconds. */
  private static long excep_exec_duration_nanos = 0;

  /** The number of exceptional executions. */
  private static int excep_exec_count = 0;

  /** Set statistics about normal and exceptional executions to zero. */
  public static void resetStatistics() {
    normal_exec_duration_nanos = 0;
    normal_exec_count = 0;
    excep_exec_duration_nanos = 0;
    excep_exec_count = 0;
  }

  public static int normalExecs() {
    return normal_exec_count;
  }

  public static int excepExecs() {
    return excep_exec_count;
  }

  /** The average normal execution time, in milliseconds. */
  public static double normalExecAvgMillis() {
    return ((normal_exec_duration_nanos / (double) normal_exec_count) / 1_000_000);
  }

  /** The average exceptional execution time, in milliseconds. */
  public static double excepExecAvgMillis() {
    return ((excep_exec_duration_nanos / (double) excep_exec_count) / 1_000_000);
  }

  // ExecutorService to run tests in parallel.
  private static ExecutorService executor = null;

  /**
   * Initialize the ExecutorService with the specified number of threads.
   *
   * @param numberOfThreads number of threads in the pool
   */
  public static void initializeExecutor(int numberOfThreads) {
    if (executor == null) {
      executor = Executors.newFixedThreadPool(numberOfThreads);
    }
  }

  /** Shutdown the ExecutorService. */
  public static void shutdownExecutor() {
    if (executor != null) {
      executor.shutdownNow();
      executor = null;
    }
  }

  /**
   * Executes {@code code.runReflectionCode()}, which sets {@code code}'s {@link
   * ReflectionCode#retval} or {@link ReflectionCode#exceptionThrown} field.
   *
   * @param code the {@link ReflectionCode} to be executed
   * @return the execution result
   */
  public static ExecutionOutcome executeReflectionCode(ReflectionCode code) {
    long startTimeNanos = System.nanoTime();
    ExecutionOutcome outcome;
    if (usethreads) {
      try {
        outcome = executeReflectionCodeThreaded(code);
      } catch (TimeoutException e) {
        // Timeouts are recorded with the timeout duration (converted from ms to ns)
        outcome = new ExceptionalExecution(e, call_timeout * 1_000_000L);
      }
    } else {
      executeReflectionCodeUnThreaded(code);
      long durationNanos = System.nanoTime() - startTimeNanos;
      outcome =
          (code.getExceptionThrown() == null)
              ? new NormalExecution(code.getReturnValue(), durationNanos)
              : new ExceptionalExecution(code.getExceptionThrown(), durationNanos);
    }
    updateStats(outcome, startTimeNanos);
    return outcome;
  }

  /**
   * Updates execution statistics based on the outcome.
   *
   * @param outcome the result of test execution
   * @param startTimeNanos start time in nanoseconds
   */
  private static void updateStats(ExecutionOutcome outcome, long startTimeNanos) {
    long durationNanos = System.nanoTime() - startTimeNanos;
    if (outcome instanceof NormalExecution) {
      normal_exec_duration_nanos += durationNanos;
      normal_exec_count++;
    } else if (outcome instanceof ExceptionalExecution) {
      excep_exec_duration_nanos += durationNanos;
      excep_exec_count++;
    }
  }

  /**
   * Executes code.runReflectionCode() in its own thread using an ExecutorService.
   *
   * @param code the {@link ReflectionCode} to be executed
   * @return the execution outcome
   * @throws TimeoutException if execution times out
   */
  private static ExecutionOutcome executeReflectionCodeThreaded(ReflectionCode code)
      throws TimeoutException {
    if (executor == null) {
      // Default initialization using available processors if not already initialized.
      executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
    Future<ExecutionOutcome> future = executor.submit(new ReflectionCodeCallable(code));
    try {
      return future.get(call_timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      future.cancel(true);
      throw e;
    } catch (InterruptedException e) {
      throw new IllegalStateException("Unexpected interrupt during test execution.", e);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RandoopBug) {
        throw (RandoopBug) cause;
      }
      throw new IllegalStateException("Error in ReflectionCodeCallable", cause);
    }
  }

  /**
   * Executes code.runReflectionCode() in the current thread.
   *
   * @param code the {@link ReflectionCode} to be executed
   */
  private static void executeReflectionCodeUnThreaded(ReflectionCode code) {
    try {
      code.runReflectionCode();
    } catch (ThreadDeath e) {
      throw e;
    } catch (ReflectionCode.ReflectionCodeException e) {
      throw new RandoopBug("code=" + code, e);
    } catch (Throwable e) {
      if (e instanceof java.lang.reflect.InvocationTargetException) {
        throw new RandoopBug("Unexpected InvocationTargetException", e);
      }
      throw e;
    }
  }

  /** A Callable that wraps the execution of ReflectionCode. */
  private static class ReflectionCodeCallable implements Callable<ExecutionOutcome> {
    private final ReflectionCode code;

    ReflectionCodeCallable(ReflectionCode code) {
      this.code = code;
    }

    @Override
    public ExecutionOutcome call() throws Exception {
      long startTimeNanos = System.nanoTime();
      try {
        code.runReflectionCode();
        long durationNanos = System.nanoTime() - startTimeNanos;
        if (code.getExceptionThrown() != null) {
          return new ExceptionalExecution(code.getExceptionThrown(), durationNanos);
        } else {
          return new NormalExecution(code.getReturnValue(), durationNanos);
        }
      } catch (ThreadDeath td) {
        throw td;
      } catch (ReflectionCode.ReflectionCodeException e) {
        throw new RandoopBug("Error in ReflectionCode: " + code, e);
      } catch (Throwable t) {
        long durationNanos = System.nanoTime() - startTimeNanos;
        return new ExceptionalExecution(t, durationNanos);
      }
    }
  }
}
