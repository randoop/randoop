package randoop.util;

import java.io.IOException;
import java.io.PrintWriter;
import randoop.BugInRandoopException;
import randoop.Globals;
import randoop.main.GenInputsAbstract;

/** Static methods that log to GenInputsAbstract.log, if that is non-null. */
public final class Log {

  private Log() {
    throw new IllegalStateException("no instance");
  }

  public static boolean isLoggingOn() {
    return GenInputsAbstract.log != null;
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
      throw new BugInRandoopException("Exception while writing to log", e);
    }
  }

  /**
   * Log to GenInputsAbstract.log, if that is non-null.
   *
   * <p>It is better to use {@link #logPrintf} than to call {@code logLine} with a concatenation. In
   * other words:
   *
   * <pre>{@code
   * logLine("arg1=" + arg1);      // BAD
   * logPrintf("arg1=%s%n", arg1); // GOOD
   * }</pre>
   *
   * The reason is that if {@code arg1.toString()} fails, {@code #logPrintf} can catch that
   * exception, but the exception would occur before {@code logLine} is entered.
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
      throw new BugInRandoopException("Exception while writing to log", e);
    }
  }

  /**
   * Log using {@code String.format} to GenInputsAbstract.log, if that is non-null.
   *
   * @param fmt the format string
   * @param args arguments to the format string
   */
  public static void logPrintf(String fmt, Object... args) {
    if (!isLoggingOn()) {
      return;
    }

    String msg;
    try {
      msg = String.format(fmt, args);
    } catch (Throwable t) {
      logLine("A user-defined toString() method failed.");
      logStackTrace(t);
      return;
    }

    try {
      GenInputsAbstract.log.write(msg);
      GenInputsAbstract.log.flush();
    } catch (IOException e) {
      throw new BugInRandoopException("Exception while writing to log", e);
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
      throw new BugInRandoopException("Exception while writing to log", e);
    }
  }
}
