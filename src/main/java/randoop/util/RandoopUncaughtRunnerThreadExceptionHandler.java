package randoop.util;

import java.lang.Thread.UncaughtExceptionHandler;

public final class RandoopUncaughtRunnerThreadExceptionHandler implements UncaughtExceptionHandler {

  /** The singleton instance of this class. */
  private static RandoopUncaughtRunnerThreadExceptionHandler singleInstance =
      new RandoopUncaughtRunnerThreadExceptionHandler();

  /** Creates a RandoopUncaughtRunnerThreadExceptionHandler. */
  private RandoopUncaughtRunnerThreadExceptionHandler() {}

  public static UncaughtExceptionHandler getHandler() {
    return singleInstance;
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    // Do nothing.
  }
}
