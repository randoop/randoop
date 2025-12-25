package randoop.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import org.checkerframework.checker.formatter.qual.FormatMethod;
import org.checkerframework.checker.mustcall.qual.NotOwning;
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
   * Returns the demand-driven log writer if logging is enabled, or null if logging is disabled.
   *
   * <p>This method provides access to {@code GenInputsAbstract.demand_driven_log} without taking
   * ownership of the resource. It exists because the Checker Framework does not permit referencing
   * {@code GenInputsAbstract.demand_driven_log} directly in the expression of an
   * {@code @EnsuresNonNullIf} annotation.
   *
   * @return the log writer if logging is enabled, null otherwise
   */
  @NotOwning
  private static @Nullable FileWriterWithName logWriter() {
    return GenInputsAbstract.demand_driven_log;
  }

  /** Do not instantiate. */
  private DemandDrivenLog() {
    throw new IllegalStateException("Do not instantiate.");
  }

  /**
   * Returns true iff logging is enabled.
   *
   * @return true iff logging is enabled
   */
  public static boolean isLoggingOn() {
    return logWriter() != null;
  }

  /**
   * Prints classes that are not part of the software under test (SUT) but are used by demand-driven
   * input creation to the console.
   *
   * @param nonSutClasses classes that are not part of the SUT but are used by demand-driven input
   *     creation
   */
  public static void printNonSutClasses(Set<Class<?>> nonSutClasses) {
    if (nonSutClasses.isEmpty()) {
      return;
    }

    System.out.println(generateNonSutClassesMessage(nonSutClasses));
    System.out.println(
        "\nNote: This list omits JDK classes. To see the full set of visited non-SUT classes, "
            + "use --demand-driven-log=<file>.");
  }

  /**
   * Logs classes that are not part of the software under test (SUT) but are used by demand-driven
   * input creation.
   *
   * @param nonSutClasses classes that are not part of the SUT but are used by demand-driven input
   *     creation
   */
  public static void logNonSutClasses(Set<Class<?>> nonSutClasses) {
    if (nonSutClasses.isEmpty()) {
      return;
    }

    logPrintln(generateNonSutClassesMessage(nonSutClasses));
  }

  /**
   * Generates a message listing classes that are not part of the software under test (SUT) but are
   * used by demand-driven input creation.
   *
   * @param nonSutClasses classes that are not part of the SUT but are used by demand-driven input
   *     creation
   * @return a formatted message listing the non-SUT classes
   */
  private static String generateNonSutClassesMessage(Set<Class<?>> nonSutClasses) {
    if (nonSutClasses.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    int numClasses = nonSutClasses.size();
    sb.append("NOTE: ")
        .append(numClasses == 1 ? "1 class was" : numClasses + " classes were")
        .append(
            " not explicitly included as test targets but are used by demand-driven to create"
                + " inputs:\n");
    sb.append("-----------------------------------------------------------------------------");
    for (Class<?> cls : nonSutClasses) {
      sb.append("\n- ").append(cls.getName());
    }
    sb.append("\n-----------------------------------------------------------------------------");
    sb.append(
        "\nTo avoid this warning, add these classes as classes-under-test via "
            + "--testclass, --classlist, or by including a jar with --testjar.");
    return sb.toString();
  }

  /**
   * Prints uninstantiable types to the console.
   *
   * @param uninstantiableTypes Set of types that could not be instantiated by demand-driven input
   *     creation.
   */
  public static void printUninstantiableTypes(Set<Type> uninstantiableTypes) {
    if (uninstantiableTypes.isEmpty()) {
      return;
    }

    System.out.println(generateUninstantiableTypesMessage(uninstantiableTypes));
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

    logPrintln(generateUninstantiableTypesMessage(uninstantiableTypes));
  }

  /**
   * Generates a message listing types that could not be instantiated by demand-driven input
   * creation.
   *
   * @param uninstantiableTypes Set of types that could not be instantiated by demand-driven input
   *     creation.
   * @return a formatted message listing the uninstantiable types
   */
  private static String generateUninstantiableTypesMessage(Set<Type> uninstantiableTypes) {
    if (uninstantiableTypes.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            "%nNOTE: %s could not be instantiated by Randoop demand-driven input creation:%n",
            StringsPlume.nPlural(uninstantiableTypes.size(), "type")));
    for (Type type : uninstantiableTypes) {
      sb.append("- ").append(type.getRuntimeClass().getName()).append("\n");
    }
    sb.append("As a result, certain sequences requiring these types may not be generated.\n");
    sb.append("Optional: To enable test generation for these types, you may:\n");
    sb.append(
        "  1. Define public static factory methods (in any class on the test classpath) that return"
            + " the target type, e.g.:\n");
    sb.append("       public static MyType createMyType() { /* build and return a MyType */ }\n");
    sb.append("  2. Include classes under test that produce these types,\n");
    sb.append(
        "       e.g., via Randoop's --classlist/--testclass args or by adding them to the"
            + " classpath\n");
    sb.append(
        "  3. Modify the source code of the SUT to make the necessary non-public constructors or"
            + " methods public for Randoop to instantiate the type directly.\n");
    return sb.toString();
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
      final FileWriterWithName w = logWriter();
      if (w == null) {
        return;
      }
      synchronized (w) {
        w.write(msg);
        w.flush();
      }
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
      final FileWriterWithName w = logWriter();
      if (w == null) {
        return;
      }
      synchronized (w) {
        w.write(msg);
        w.write(System.lineSeparator());
        w.flush();
      }
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
      final FileWriterWithName w = logWriter();
      if (w == null) {
        return;
      }
      synchronized (w) {
        PrintWriter pw = new PrintWriter(w);
        t.printStackTrace(pw);
        pw.flush();
        w.flush();
      }
    } catch (IOException e) {
      throw new RandoopBug("Exception while writing to log", e);
    }
  }
}
