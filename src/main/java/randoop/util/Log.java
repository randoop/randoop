package randoop.util;

import java.io.IOException;
import java.io.PrintWriter;
import randoop.Globals;
import randoop.main.GenInputsAbstract;

/** Methods that log to GenInputsAbstract.log, if that is non-null. */
public final class Log {

  private Log() {
    throw new IllegalStateException("no instance");
  }

  /**
   * Log to GenInputsAbstract.log, if that is non-null.
   *
   * @param s the string to output
   */
  public static void log(String s) {
    if (!isLoggingOn()) {
      return;
    }

    try {
      GenInputsAbstract.log.write(s);
      GenInputsAbstract.log.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Log to GenInputsAbstract.log, if that is non-null.
   *
   * @param s the string to output (followed by a newline)
   */
  public static void logLine(String s) {
    if (!isLoggingOn()) {
      return;
    }

    try {
      GenInputsAbstract.log.write(s);
      GenInputsAbstract.log.write(Globals.lineSep);
      GenInputsAbstract.log.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Log to GenInputsAbstract.log, if that is non-null.
   *
   * @param t the Throwable whose stack trace to log
   */
  public static void logStackTrace(Throwable t) {
    if (!isLoggingOn()) {
      return;
    }

    try {
      // Gross, GenInputsAbstract.log should be a writer instead of a FileWriter
      PrintWriter pw = new PrintWriter(GenInputsAbstract.log);
      t.printStackTrace(pw);
      pw.flush();
      GenInputsAbstract.log.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static boolean isLoggingOn() {
    return GenInputsAbstract.log != null;
  }
}
