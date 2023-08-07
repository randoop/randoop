package randoop.util;

import java.util.concurrent.TimeoutException;
import org.plumelib.options.Option;
import org.plumelib.options.OptionGroup;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.main.RandoopBug;

/**
 * Static methods that executes the code of a ReflectionCode object.
 *
 * <p>This class maintains an "executor" thread. Code is executed on that thread. If the code takes
 * longer than the specified timeout, the thread is killed and a TimeoutException exception is
 * reported.
 */
public final class ReflectionExecutor {

  private ReflectionExecutor() {
    throw new Error("Do not instantiate");
  }

  /**
   * If true, Randoop executes each test in a separate thread and kills tests that take too long to
   * finish, as determined by the --call-timeout command-line argument. Tests killed in this manner
   * are not reported to the user, but are recorded in Randoop's log. Use the {@code --log}
   * command-line option to make Randoop produce the log.
   *
   * <p>Use this option if Randoop does not terminate, which is usually due to execution of code
   * under test that results in an infinite loop or that waits for user input. The downside of this
   * option is a BIG (order-of-magnitude) decrease in generation speed. The tests are not run in
   * parallel, merely in isolation.
   */
  @OptionGroup("Threading")
  @Option("Execute each test in a separate thread, with timeout")
  public static boolean usethreads = false;

  /**
   * Default for call_timeout, in milliseconds. Should only be accessed by {@code
   * checkOptionsValid()}.
   */
  public static int CALL_TIMEOUT_MILLIS_DEFAULT = 5000;

  /**
   * After this many milliseconds, a non-returning method call, and its associated test, are stopped
   * forcefully. Only meaningful if {@code --usethreads} is also specified.
   */
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
    return ((normal_exec_duration_nanos / (double) normal_exec_count) / Math.pow(10, 6));
  }

  /** The average exceptional execution time, in milliseconds. */
  public static double excepExecAvgMillis() {
    return ((excep_exec_duration_nanos / (double) excep_exec_count) / Math.pow(10, 6));
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
    if (usethreads) {
      try {
        executeReflectionCodeThreaded(code);
      } catch (TimeoutException e) {
        // Don't factor timeouts into the average execution times.  (Is that the right thing to do?)
        return new ExceptionalExecution(
            e, call_timeout * 1000000L); // convert milliseconds to nanoseconds
      }
    } else {
      executeReflectionCodeUnThreaded(code);
    }
    long durationNanos = System.nanoTime() - startTimeNanos;

    if (code.getExceptionThrown() != null) {
      // Add durationNanos to running sum for exceptional execution.
      excep_exec_duration_nanos += durationNanos;
      assert excep_exec_duration_nanos > 0; // check no overflow.
      excep_exec_count++;
      // System.out.println("exceptional execution: " + code);
      return new ExceptionalExecution(code.getExceptionThrown(), durationNanos);
    } else {
      // Add durationNanos to running sum for normal execution.
      normal_exec_duration_nanos += durationNanos;
      assert normal_exec_duration_nanos > 0; // check no overflow.
      normal_exec_count++;
      // System.out.println("normal execution: " + code);
      return new NormalExecution(code.getReturnValue(), durationNanos);
    }
  }

  /**
   * Executes code.runReflectionCode() in its own thread.
   *
   * @param code the {@link ReflectionCode} to be executed
   * @throws TimeoutException if execution times out
   */
  @SuppressWarnings({"deprecation", "removal", "DeprecatedThreadMethods"})
  private static void executeReflectionCodeThreaded(ReflectionCode code) throws TimeoutException {

    RunnerThread runnerThread = new RunnerThread(null);
    runnerThread.setup(code);

    try {

      // Start the test.
      runnerThread.start();

      // If test doesn't finish in time, suspend it.
      runnerThread.join(call_timeout);

      if (!runnerThread.runFinished) {
        Log.logPrintf("Exceeded timeout: aborting execution of call: %s%n", runnerThread.getCode());
        // TODO: is it possible to log the test being executed?
        // (Maybe not here, but it has been previously logged.)

        // We use this deprecated method because it's the only way to
        // stop a thread no matter what it's doing.
        runnerThread.stop();

        throw new TimeoutException();
      }

    } catch (java.lang.InterruptedException e) {
      throw new IllegalStateException(
          "A RunnerThread thread shouldn't be interrupted by anyone! (This may be a bug in"
              + " Randoop; please report it at https://github.com/randoop/randoop/issues ,"
              + " providing the information requested at"
              + " https://randoop.github.io/randoop/manual/index.html#bug-reporting .)");
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
      return;
    } catch (
        @SuppressWarnings("removal")
        ThreadDeath e) { // can't stop these guys
      throw e;
    } catch (ReflectionCode.ReflectionCodeException e) { // bug in Randoop
      throw new RandoopBug("code=" + code, e);
    } catch (Throwable e) {
      if (e instanceof java.lang.reflect.InvocationTargetException) {
        throw new RandoopBug("Unexpected InvocationTargetException", e);
      }

      // Debugging -- prints unconditionally, to System.out.
      // printExceptionDetails(e, System.out);

      throw e;
    }
  }
}
