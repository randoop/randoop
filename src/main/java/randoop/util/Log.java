package randoop.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import org.checkerframework.checker.formatter.qual.FormatMethod;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;

/** Static methods that log to GenInputsAbstract.log, if that is non-null. */
public final class Log {

  private Log() {
    throw new IllegalStateException("no instance");
  }

  /**
   * Returns true if logging is enabled.
   *
   * @return true if logging is enabled
   */
  @SuppressWarnings("nullness:flowexpr.parse.error") // TEMPORARY, to mask a bug
  @EnsuresNonNullIf(expression = "GenInputsAbstract.log", result = true)
  public static boolean isLoggingOn() {
    return GenInputsAbstract.log != null;
  }

  /**
   * Log using {@code String.format} to GenInputsAbstract.log, if that is non-null.
   *
   * @param fmt the format string
   * @param args arguments to the format string
   */
  @FormatMethod
  public static void logPrintf(String fmt, @Nullable Object... args) {
    if (!isLoggingOn()) {
      return;
    }

    String msg;
    try {
      msg = String.format(fmt, args);
    } catch (Throwable t) {
      logPrintf("A user-defined toString() method failed.%n");
      @Nullable Class<?>[] argTypes = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) {
        argTypes[i] = args[i] == null ? null : args[i].getClass();
      }
      logPrintf("  fmt = %s%n", fmt);
      logPrintf("  arg types = %s%n", Arrays.toString(argTypes));
      logStackTrace(t);
      return;
    }

    try {
      GenInputsAbstract.log.write(msg);
      GenInputsAbstract.log.flush();
    } catch (IOException e) {
      throw new RandoopBug("Exception while writing to log", e);
    }
  }

  /**
   * Log a literal string to GenInputsAbstract.log, if that is non-null.
   *
   * @param msg a string to log
   */
  public static void logPrintln(String msg) {
    if (!isLoggingOn()) {
      return;
    }

    try {
      GenInputsAbstract.log.write(msg);
      GenInputsAbstract.log.write(System.lineSeparator());
      GenInputsAbstract.log.flush();
    } catch (IOException e) {
      throw new RandoopBug("Exception while writing to log", e);
    }
  }

  /** Log a blank line to GenInputsAbstract.log, if that is non-null. */
  public static void logPrintln() {
    if (!isLoggingOn()) {
      return;
    }

    try {
      GenInputsAbstract.log.write(System.lineSeparator());
      GenInputsAbstract.log.flush();
    } catch (IOException e) {
      throw new RandoopBug("Exception while writing to log", e);
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
      throw new RandoopBug("Exception while writing to log", e);
    }
  }
}
