package randoop.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import org.checkerframework.checker.formatter.qual.FormatMethod;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.FileWriterWithName;
import org.plumelib.util.StringsPlume;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.types.Type;

/**
 * This class contains static methods for logging messages related to demand-driven input creation.
 */
public final class DemandDrivenLog {

  /**
   * The file writer for the demand-driven log, if logging is enabled. If this is null, then logging
   * is disabled.
   *
   * <p>This field was introduced even though {@code GenInputsAbstract.demand_driven_log} already
   * exists, because the Checker Framework does not permit referencing {@code
   * GenInputsAbstract.demand_driven_log} in the expression of an {@code @EnsuresNonNullIf}
   * annotation.
   */
  private static final @Nullable FileWriterWithName DEMAND_DRIVEN_LOG_FLAG =
      GenInputsAbstract.demand_driven_log;

  /** Do not instantiate. */
  private DemandDrivenLog() {
    throw new IllegalStateException("Do not instantiate.");
  }

  /**
   * Returns true iff logging is enabled.
   *
   * @return true iff logging is enabled
   */
  @EnsuresNonNullIf(expression = "DEMAND_DRIVEN_LOG_FLAG", result = true)
  public static boolean isLoggingOn() {
    return DEMAND_DRIVEN_LOG_FLAG != null;
  }

  /**
   * Logs classes that are not part of the software under test (SUT) but are used by demand-driven
   * input creation to the demand-driven log file.
   *
   * @param nonSutClasses Set of classes that are not part of the SUT but are used by demand-driven
   *     input creation
   */
  public static void logNonSutClasses(Set<Class<?>> nonSutClasses) {
    if (nonSutClasses.isEmpty()) {
      return;
    }

    int numClasses = nonSutClasses.size();
    logPrintln(
        "NOTE: "
            + (numClasses == 1 ? "1 class was" : numClasses + " classes were")
            + " not specified but are "
            + "used by demand-driven to create inputs:");
    logPrintln("-----------------------------------------------------------------------------");
    for (Class<?> cls : nonSutClasses) {
      logPrintln("- " + cls.getName());
    }
    logPrintln("-----------------------------------------------------------------------------");
    logPrintln("To avoid this warning, explicitly specify these classes to Randoop.");
  }

  /**
   * Logs uninstantiable types to the demand-driven log file.
   *
   * @param uninstantiableTypes Set of types that could not be instantiated by demand-driven input
   *     creation.
   */
  public static void logUninstantiableTypes(Set<Type> uninstantiableTypes) {
    if (uninstantiableTypes.isEmpty()) {
      return;
    }

    logPrintf(
        "%nNOTE: %s could not be instantiated by Randoop demand-driven input creation:%n",
        StringsPlume.nplural(uninstantiableTypes.size(), "type"));
    for (Type type : uninstantiableTypes) {
      logPrintln("- " + type.getRuntimeClass().getName());
    }
    logPrintln("As a result, certain sequences requiring these types may not be generated.");
    logPrintln("Optional: To enable test generation for these types, you may:");
    logPrintln(
        "  1. Define public static factory methods (in any class on the test classpath) that return"
            + " the target type, e.g.:");
    logPrintln("       public static MyType createMyType() { /* build and return a MyType */ }");
    logPrintln("  2. Include classes under test that produce these types,");
    logPrintln(
        "       e.refg., via Randoop's --classlist/--testclass args or by adding them to the"
            + " classpath");
    logPrintln(
        "  3. Allow reflective access to non-public constructors by making the needed"
            + " constructor/method public");
    logPrintln("");
  }

  /**
   * Log using {@code String.format} to GenInputsAbstract.demand_driven_log, if that is non-null.
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
      logPrintln("A user-defined toString() method failed.");
      Class<?>[] argTypes = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) {
        argTypes[i] = args[i].getClass();
      }
      logPrintln("  fmt = " + fmt);
      logPrintln("  arg types = " + Arrays.toString(argTypes));
      logStackTrace(t);
      return;
    }

    try {
      DEMAND_DRIVEN_LOG_FLAG.write(msg);
      DEMAND_DRIVEN_LOG_FLAG.flush();
    } catch (IOException e) {
      throw new RandoopBug("Exception while writing to log", e);
    }
  }

  /**
   * Logs a message to the demand-driven log file, if logging is enabled.
   *
   * @param msg the message to log
   */
  private static void logPrintln(String msg) {
    if (!isLoggingOn()) {
      return;
    }

    try {
      DEMAND_DRIVEN_LOG_FLAG.write(msg);
      DEMAND_DRIVEN_LOG_FLAG.write(System.lineSeparator());
      DEMAND_DRIVEN_LOG_FLAG.flush();
    } catch (IOException e) {
      throw new RandoopBug("Exception while writing to demand-driven log file.", e);
    }
  }

  /**
   * Log to GenInputsAbstract.demand_driven_log, if logging is enabled.
   *
   * @param t the Throwable whose stack trace to log
   */
  public static void logStackTrace(Throwable t) {
    if (!isLoggingOn()) {
      return;
    }

    try {
      PrintWriter pw = new PrintWriter(DEMAND_DRIVEN_LOG_FLAG);
      t.printStackTrace(pw);
      pw.flush();
      DEMAND_DRIVEN_LOG_FLAG.flush();
    } catch (IOException e) {
      throw new RandoopBug("Exception while writing to log", e);
    }
  }
}
