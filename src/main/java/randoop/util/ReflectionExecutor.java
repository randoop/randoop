package randoop.util;

import java.io.PrintStream;
import plume.Option;
import plume.OptionGroup;
import plume.UtilMDE;

/**
 * Executes the code of a ReflectionCode object.
 *
 * <p>This class maintains an "executor" thread. Code is executed on that thread. If the code takes
 * longer than the specified timeout, the thread is killed and a TimeoutExceededException exception
 * is reported.
 */
public final class ReflectionExecutor {

  private ReflectionExecutor() {
    throw new Error("Do not instantiate");
  }

  /**
   * If true, Randoop executes each test in a separate thread and kills tests that take too long to
   * finish, as determined by the --timeout command-line argument. Tests killed in this manner are
   * not reported to the user.
   *
   * <p>Use this option if Randoop does not terminate, which is usually due to execution of code
   * under test that results in an infinite loop or that waits for user input. The downside of this
   * option is a BIG (order-of-magnitude) decrease in generation speed. The tests are not run in
   * parallel, merely in isolation.
   */
  @OptionGroup("Threading and timeouts")
  @Option("Execute each test in a separate thread, with timeout")
  public static boolean usethreads = false;

  /**
   * After this many milliseconds, a non-returning method call, and its associated test, are stopped
   * forcefully. Only meaningful if --usethreads is also specified.
   */
  @Option("Maximum number of milliseconds a test may run. Only meaningful with --usethreads")
  public static int timeout = 5000;

  // Execution statistics.
  private static long normal_exec_accum = 0;
  private static int normal_exec_count = 0;
  private static long excep_exec_accum = 0;
  private static int excep_exec_count = 0;

  public static int normalExecs() {
    return normal_exec_count;
  }

  public static int excepExecs() {
    return excep_exec_count;
  }

  public static double normalExecAvgMillis() {
    return ((normal_exec_accum / (double) normal_exec_count) / Math.pow(10, 6));
  }

  public static double excepExecAvgMillis() {
    return ((excep_exec_accum / (double) excep_exec_count) / Math.pow(10, 6));
  }

  public static Throwable executeReflectionCode(ReflectionCode code, PrintStream out) {
    Throwable ret;

    long start = System.nanoTime();
    if (usethreads) {
      ret = executeReflectionCodeThreaded(code, out);
    } else {
      ret = executeReflectionCodeUnThreaded(code, out);
    }
    long duration = System.nanoTime() - start;

    if (ret == null) {
      // Add duration to running average for normal execution.
      normal_exec_accum += duration;
      assert normal_exec_accum > 0; // check no overflow.
      normal_exec_count++;
    } else {
      // Add duration to running average for exceptional execution.
      excep_exec_accum += duration;
      assert excep_exec_accum > 0; // check no overflow.
      excep_exec_count++;
    }

    return ret;
  }

  /**
   * Executes code.runReflectionCode(). If no exception is thrown, returns null. Otherwise, returns
   * the exception thrown.
   *
   * @param code the {@link ReflectionCode} to be executed
   * @param out stream to print message to or null if message is to be ignored
   * @return null or the exception thrown
   */
  @SuppressWarnings("deprecation")
  private static Throwable executeReflectionCodeThreaded(ReflectionCode code, PrintStream out) {

    RunnerThread runnerThread = new RunnerThread(null);
    runnerThread.setup(code);

    try {

      // Start the test.
      runnerThread.start();

      // If test doesn't finish in time, suspend it.
      runnerThread.join(timeout);

      if (!runnerThread.runFinished) {
        if (Log.isLoggingOn()) {
          Log.log("Exceeded max wait: aborting test input.");
        }

        // We use this deprecated method because it's the only way to
        // stop a thread no matter what it's doing.
        runnerThread.stop();

        return new TimeoutExceededException();
      }

      return runnerThread.exceptionThrown;

    } catch (java.lang.InterruptedException e) {
      throw new IllegalStateException(
          "A RunnerThread thread shouldn't be interrupted by anyone! "
              + "(this may be a bug in Randoop; please report it.)");
    }
  }

  /**
   * without threads.
   *
   * @param code the {@link ReflectionCode} to be executed
   * @param out the string to print messages; null if no output
   * @return null, or the exception thrown
   */
  private static Throwable executeReflectionCodeUnThreaded(ReflectionCode code, PrintStream out) {
    try {
      code.runReflectionCode();
      return null;
    } catch (ThreadDeath e) { // can't stop these guys
      throw e;
    } catch (ReflectionCode.NotCaughtIllegalStateException e) { // exception in
      // randoop code
      throw e;
    } catch (Throwable e) {
      Throwable orig_e = null;
      if (e instanceof java.lang.reflect.InvocationTargetException) {
        orig_e = e;
        e = e.getCause();
      }

      // Debugging -- prints unconditionally, to System.out.
      // printExceptionDetails(e, System.out);
      // if (orig_e != null) {
      // System.out.println("Original exception: " + orig_e);
      // }
      if (out != null) {
        printExceptionDetails(e, out);
        if (orig_e != null) {
          out.println("Original exception: " + orig_e);
        }
      }
      return e;
    }
  }

  private static void printExceptionDetails(Throwable e, PrintStream out) {
    out.println("Exception thrown: " + e.toString());
    out.println("Message: " + e.getMessage());
    out.println("Stack trace: ");
    try {
      e.printStackTrace(out);
    } catch (Throwable t) {
      try {
        // Workaround for http://bugs.sun.com/view_bug.do?bug_id=6973831
        // Note that field Throwable.suppressedExceptions only exists in JDK 7.
        Object eSuppressedExceptions = UtilMDE.getPrivateField(e, "suppressedExceptions");
        if (eSuppressedExceptions == null) {
          UtilMDE.setFinalField(e, "suppressedExceptions", new java.util.ArrayList<>());
        }
      } catch (NoSuchFieldException nsfe) {
        out.println("This can't happen on JDK7 (can on JDK6): NoSuchFieldException " + nsfe);
      }
    }
  }
}
