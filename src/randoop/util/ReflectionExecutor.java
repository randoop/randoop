package randoop.util;

import java.io.PrintStream;

import plume.Option;


/**
 * Executes the code of a ReflectionCode object.
 *
 * This class maintains an "executor" thread. Code is executed
 * on that thread. If the code takes longer than the specified
 * timeout, the thread is killed and a ReflectionExecutor.TimeoutExceeded
 * exception is reported.
 *
 */
public final class ReflectionExecutor {

  // Milliseconds after which an executing thread will be forcefully stopped.
  // Default is arbitrary; can be changed via setter method.

  /* @Invisible*/
  @Option("Milliseconds after which a statement (e.g. method call) is stopped forcefully. Only meaningfull with --usethreads.")
  public static long timeout = 5000;

  /* @Invisible*/
  @Option("Executing tested code in a separate thread (lets Randoop detect and kill nonterminating or long-running tests")
  public static boolean usethreads = true;

  public static class TimeoutExceeded extends RuntimeException {
    private static final long serialVersionUID = -5314228165430676893L;
  }

  // Execution statistics.
  private static long normal_exec_accum  = 0;
  private static int normal_exec_count = 0;
  private static long excep_exec_accum  = 0;
  private static int excep_exec_count = 0;

  public static int normalExecs() {
    return normal_exec_count;
  }

  public static int excepExecs() {
    return excep_exec_count;
  }

  public static double normalExecAvgMillis() {
    return ((normal_exec_accum / (double) normal_exec_count)/Math.pow(10,6));
  }

  public static double excepExecAvgMillis() {
    return ((excep_exec_accum / (double) excep_exec_count)/Math.pow(10,6));
  }

  public static Throwable executeReflectionCode(ReflectionCode code, PrintStream out) {
    Throwable ret = null;

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
   * Executes code.runReflectionCode(). If no exception is thrown, returns null.
   * Otherwise, returns the exception thrown.
   * @param code
   * @param out stream to print message to or null if message is to be ignored.
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
        runnerThread.stop();// We use this deprecated method because it's the only way to
        // stop a thread no matter what it's doing.
        return new ReflectionExecutor.TimeoutExceeded();
      }

      return runnerThread.exceptionThrown;

    } catch (java.lang.InterruptedException e) {
      throw new IllegalStateException("A RunnerThread thread shouldn't be interrupted by anyone! "
          + "(this may be a bug in Randoop; please report it.)");
    }
  }

  /**
   * without threads.
   */
  private static Throwable executeReflectionCodeUnThreaded(ReflectionCode code, PrintStream out) {
    try {
      code.runReflectionCode();
      return null;
    } catch (ThreadDeath e) {//can't stop these guys
      throw e;
    } catch (ReflectionCode.NotCaughtIllegalStateException e) {//exception in randoop code
      throw e;
    } catch (Throwable e) {
      if (e instanceof java.lang.reflect.InvocationTargetException)
        e = e.getCause();

      if (out != null){
        out.println("Exception thrown:" + e.toString());
        out.println("Message: " + e.getMessage());
        out.println("Stack trace: ");
        e.printStackTrace(out);
      }
      return e;
    }
  }
}
