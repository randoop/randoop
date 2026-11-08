package randoop.util;

import java.lang.Thread.UncaughtExceptionHandler;

/** An uncaught-exception handler for {@link RunnerThread}s that ignores all exceptions. */
public final class RandoopUncaughtRunnerThreadExceptionHandler implements UncaughtExceptionHandler {

  /** The singleton instance of this class. */
  private static RandoopUncaughtRunnerThreadExceptionHandler singleInstance =
      new RandoopUncaughtRunnerThreadExceptionHandler();

  /** Creates a RandoopUncaughtRunnerThreadExceptionHandler. */
  private RandoopUncaughtRunnerThreadExceptionHandler() {}

  /**
   * Returns the singleton instance of this handler.
   *
   * @return the singleton instance of this handler
   */
  public static UncaughtExceptionHandler getHandler() {
    return singleInstance;
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    // Do nothing.
  }
}
