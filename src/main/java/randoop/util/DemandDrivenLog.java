package randoop.util;

import java.io.IOException;
import java.util.Set;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.types.Type;

/** Utility class for logging messages related to demand-driven input creation. */
public final class DemandDrivenLog {

  /** Prevents instantiation. */
  private DemandDrivenLog() {
    throw new IllegalStateException("Utility class; should not be instantiated.");
  }

  /**
   * Checks if logging is enabled.
   *
   * @return true if logging is enabled, false otherwise
   */
  private static boolean isLoggingOn() {
    return GenInputsAbstract.demand_driven_log != null;
  }

  /**
   * Logs unspecified classes to the demand-driven log file.
   *
   * @param nonJdkUnspecifiedClasses Set of classes not specified by the user but used by
   *     demand-driven input creation.
   */
  public static void logUnspecifiedClasses(Set<Class<?>> nonJdkUnspecifiedClasses) {
    if (nonJdkUnspecifiedClasses.isEmpty()) {
      return;
    }

    String header =
        String.format(
            "%nNOTE: %d class(es) were not specified but are used by demand-driven input creation:%n",
            nonJdkUnspecifiedClasses.size());
    String separator =
        "-----------------------------------------------------------------------------";

    // Log header and separator
    logPrintln(header.trim());
    logPrintln(separator);

    // Log each unspecified class
    for (Class<?> cls : nonJdkUnspecifiedClasses) {
      logPrintln("- " + cls.getName());
    }

    // Log separator and suggestion
    logPrintln(separator);
    logPrintln("To avoid this warning, explicitly specify these classes to Randoop.");
    logBlankLine();
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

    String header =
        String.format(
            "%nNOTE: %d type(s) could not be instantiated by Randoop demand-driven input creation:%n",
            uninstantiableTypes.size());
    String separator =
        "-----------------------------------------------------------------------------";

    // Log header and separator
    logPrintln(header.trim());
    logPrintln(separator);

    // Log each uninstantiable type
    for (Type type : uninstantiableTypes) {
      logPrintln("- " + type.getRuntimeClass().getName());
    }

    // Log separator and suggestions
    logPrintln(separator);
    logPrintln("As a result, certain sequences requiring these types may not be generated.");
    logPrintln("Optional: To enable test generation for these types, you may:");
    logPrintln("  1. Provide custom generators or factory methods.");
    logPrintln("  2. Specify additional classes that can produce instances of these types.");
    logBlankLine();
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
      GenInputsAbstract.demand_driven_log.write(msg);
      GenInputsAbstract.demand_driven_log.write(System.lineSeparator());
      GenInputsAbstract.demand_driven_log.flush();
    } catch (IOException e) {
      throw new RandoopBug("Exception while writing to demand-driven log file.", e);
    }
  }

  /** Logs a blank line to the demand-driven log file, if logging is enabled. */
  private static void logBlankLine() {
    if (!isLoggingOn()) {
      return;
    }

    try {
      GenInputsAbstract.demand_driven_log.write(System.lineSeparator());
      GenInputsAbstract.demand_driven_log.flush();
    } catch (IOException e) {
      throw new RandoopBug("Exception while writing to demand-driven log file.", e);
    }
  }
}
