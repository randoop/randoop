package randoop.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import org.checkerframework.checker.formatter.qual.FormatMethod;
import org.plumelib.util.ArraysPlume;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;

/** Static methods that log to GenInputsAbstract.log, if that is non-null. */
public final class Log {

  private Log() {
    throw new IllegalStateException("no instance");
  }

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
  public static void logPrintf(String fmt, Object... args) {
    if (!isLoggingOn()) {
      return;
    }

    String msg;
    try {
      msg = String.format(fmt, args);
    } catch (Throwable t) {
      logPrintf("A user-defined toString() method failed.%n");
      Class<?>[] argTypes = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) {
        argTypes[i] = args[i].getClass();
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

  /**
   * Gives a string representation of the value and its class. Intended for debugging.
   *
   * @param v a value; may be null
   * @return the value's toString and its class
   */
  public static String toStringAndClass(Object v) {
    if (v == null) {
      return "null";
    } else if (v.getClass() == Object.class) {
      return "a value of class " + v.getClass();
    } else if (v.getClass().isArray()) {
      try {
        return String.format("%s [%s]", ArraysPlume.toString(v), v.getClass());
      } catch (Exception e) {
        return String.format("exception_when_printing_array [%s]", v.getClass());
      }
    } else {
      try {
        return String.format("%s [%s]", v, v.getClass());
      } catch (Exception e) {
        return String.format("exception_when_calling_toString [%s]", v.getClass());
      }
    }
  }
}
