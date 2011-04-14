package randoop.util;

import java.lang.Thread.UncaughtExceptionHandler;

public class RandoopUncaughtRunnerThreadExceptionHandler implements UncaughtExceptionHandler {

  private static RandoopUncaughtRunnerThreadExceptionHandler singleInstance = new RandoopUncaughtRunnerThreadExceptionHandler();

  public static UncaughtExceptionHandler getHandler() {
    return singleInstance;
  }

  public void uncaughtException(Thread t, Throwable e) {
    // Do nothing.
  }

}
