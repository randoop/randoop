package randoop.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import org.checkerframework.checker.formatter.qual.FormatMethod;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.plumelib.util.UtilPlume;
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

  ///////////////////////////////////////////////////////////////////////////
  /// Debugging toString
  ///

  /// TODO: Move these methods to UtilPlume.  (Synchronize the two versions before deleting this
  // one.  Also write tests, probably.)

  /**
   * Gives a string representation of the value and its class. Intended for debugging.
   *
   * @param v a value; may be null
   * @return the value's toString and its class
   */
  @SideEffectFree
  public static String toStringAndClass(Object v) {
    return toStringAndClass(v, false);
  }

  /**
   * Gives a string representation of the value and its class. Intended for debugging.
   *
   * @param v a value; may be null
   * @param shallow if true, do not show elements of arrays and lists
   * @return the value's toString and its class
   */
  @SideEffectFree
  private static String toStringAndClass(Object v, boolean shallow) {
    if (v == null) {
      return "null";
    } else if (v.getClass() == Object.class) {
      return "a value of class " + v.getClass();
    } else if (v.getClass().isArray()) {
      return arrayToStringAndClass(v);
    } else if (v instanceof List) {
      return listToStringAndClass((List<?>) v, shallow);
    } else {
      try {
        String formatted = UtilPlume.escapeJava(v.toString());
        return String.format("%s [%s]", formatted, v.getClass());
      } catch (Exception e) {
        return String.format("exception_when_calling_toString [%s]", v.getClass());
      }
    }
  }

  /**
   * Gives a string representation of the value and its class. Intended for debugging.
   *
   * @param lst a value; may be null
   * @param shallow if false, show the value and class of list elements; if true, do not recurse
   *     into elements of arrays and lists;
   * @return the value's toString and its class
   */
  @SideEffectFree
  public static String listToStringAndClass(List<?> lst, boolean shallow) {
    if (lst == null) {
      return "null";
    } else {
      return listToString(lst, false) + " [" + lst.getClass() + "]";
    }
  }

  /**
   * For use by toStringAndClass. Calls toStringAndClass on each element, but does not add the class
   * of the list itself.
   *
   * @param lst the list to print
   * @param shallow if true, just use {@code toString} on the whole list
   * @return a string representation of each element and its class
   */
  @SideEffectFree
  public static String listToString(List<?> lst, boolean shallow) {
    if (lst == null) {
      return "null";
    } else if (shallow) {
      return lst.toString();
    }
    StringJoiner sj = new StringJoiner(", ", "[", "]");
    for (Object o : lst) {
      sj.add(toStringAndClass(o, true));
    }
    return sj.toString();
  }

  /**
   * Returns a string representation of the contents of the specified array. The argument must be an
   * array or null. This just dispatches one of the 9 overloaded versions of {@code
   * java.util.Arrays.toString()}.
   *
   * @param a an array
   * @return a string representation of the array
   * @throws IllegalArgumentException if a is not an array
   */
  @SuppressWarnings("all:purity") // defensive coding: throw exception when argument is invalid
  @SideEffectFree
  public static String arrayToStringAndClass(Object a) {

    if (a == null) {
      return "null";
    }
    String theClass = " [" + a.getClass() + "]";

    if (a instanceof boolean[]) {
      return Arrays.toString((boolean[]) a) + theClass;
    } else if (a instanceof byte[]) {
      return Arrays.toString((byte[]) a) + theClass;
    } else if (a instanceof char[]) {
      return Arrays.toString((char[]) a) + theClass;
    } else if (a instanceof double[]) {
      return Arrays.toString((double[]) a) + theClass;
    } else if (a instanceof float[]) {
      return Arrays.toString((float[]) a) + theClass;
    } else if (a instanceof int[]) {
      return Arrays.toString((int[]) a) + theClass;
    } else if (a instanceof long[]) {
      return Arrays.toString((long[]) a) + theClass;
    } else if (a instanceof short[]) {
      return Arrays.toString((short[]) a) + theClass;
    }

    if (a instanceof Object[]) {
      try {
        return listToString(Arrays.asList((Object[]) a), false) + theClass;
      } catch (Exception e) {
        return String.format("exception_when_printing_array" + theClass);
      }
    }

    throw new IllegalArgumentException(
        "Argument is not an array; its class is " + a.getClass().getName());
  }
}
