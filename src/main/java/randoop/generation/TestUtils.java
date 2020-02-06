package randoop.generation;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.FileWriterWithName;
import randoop.main.GenInputsAbstract;

/**
 * Utility methods for setting up selection and operation-history logging in integration tests that
 * use a generator.
 */
public class TestUtils {

  // Setting to true causes system test runNoOutputTest to fail.
  static boolean debug = false;

  /**
   * Use system properties to set command-line arguments for logging.
   *
   * <p>This has no effect unless tests are run with gradle command-line options such as
   *
   * <pre>
   * -Drandoop.log=randoop-log.txt
   * -Drandoop.selection.log=selection-log.txt
   * -Drandoop.operation.history.log=operation-log.txt
   * </pre>
   *
   * @param generator the AbstractGenerator in which to set the operation log
   */
  public static void setAllLogs(AbstractGenerator generator) {
    setRandoopLog();
    setSelectionLog();
    setOperationLog(generator);
  }

  /** Uses the system property {@code randoop.log} to set {@link GenInputsAbstract#log}. */
  public static void setRandoopLog() {
    String randoopLog = System.getProperty("randoop.log");
    setRandoopLog(randoopLog);
  }

  /**
   * Uses the argument to set {@link GenInputsAbstract#log}.
   *
   * @param filename the file to write the log to; does nothing if filename is null
   */
  @SuppressWarnings(
      "DefaultCharset") // TODO: make GenInputsAbstract.log a Writer; change command-line arguments.
  public static void setRandoopLog(@Nullable String filename) {
    if (debug) {
      System.out.println("setRandoopLog(" + filename + ")");
    }
    if (filename == null) {
      return;
    }
    if (filename.isEmpty()) {
      throw new IllegalArgumentException();
    }
    try {
      GenInputsAbstract.log = new FileWriterWithName(filename);
    } catch (IOException ioe) {
      // TODO: clarify that this is a user error
      throw new Error("Cannot write file " + filename, ioe);
    }
  }

  /**
   * Uses the system property {@code randoop.selection.log} to set {@link
   * GenInputsAbstract#selection_log}.
   */
  public static void setSelectionLog() {
    String selectionLog = System.getProperty("randoop.selection.log");
    setSelectionLog(selectionLog);
  }

  /**
   * Uses the argument to set {@link GenInputsAbstract#selection_log}.
   *
   * @param filename the file to write the log to; does nothing if filename is null
   */
  @SuppressWarnings("DefaultCharset") // TODO: should specify a charset
  public static void setSelectionLog(@Nullable String filename) {
    if (debug) {
      System.out.println("setSelectionLog(" + filename + ")");
    }
    if (filename == null) {
      return;
    }
    if (filename.isEmpty()) {
      throw new IllegalArgumentException();
    }
    try {
      setSelectionLog(new FileWriterWithName(filename));
    } catch (IOException e) {
      throw new Error("problem creating FileWriter for " + filename, e);
    }
  }

  /**
   * Uses the argument to set {@link GenInputsAbstract#selection_log}.
   *
   * @param fw the FileWriter to write the log to; does nothing if fw is null
   */
  public static void setSelectionLog(FileWriterWithName fw) {
    if (debug) {
      System.out.println("setSelectionLog(" + fw + ")");
    }
    GenInputsAbstract.selection_log = fw;
  }

  /**
   * If the system property {@code randoop.operation.history.log} is set, sets the operation history
   * logger for the generator using the destination given by the property.
   *
   * @param generator the generator for which logger is to be set
   */
  public static void setOperationLog(AbstractGenerator generator) {
    setOperationLog(System.getProperty("randoop.operation.history.log"), generator);
  }

  /**
   * If the filename is non-null, sets the operation history logger for the generator using it.
   *
   * @param filename the file to write the log to; does nothing if filename is null
   * @param generator the generator for which logger is to be set
   */
  public static void setOperationLog(@Nullable String filename, AbstractGenerator generator) {
    // This println statement causes system test runNoOutputTest to fail.
    if (debug) {
      System.out.println("setOperationLog(" + filename + ")");
    }
    if (filename == null) {
      return;
    }
    if (filename.isEmpty()) {
      throw new IllegalArgumentException();
    }
    try {
      setOperationLog(new PrintWriter(new File(filename), UTF_8.name()), generator);
    } catch (IOException e) {
      throw new Error("problem creating FileWriter for " + filename, e);
    }
  }

  /**
   * If the PrintWriter is non-null, sets the operation history logger for the generator using it.
   *
   * @param pw the PrintWriter to write the log to; does nothing if pw is null
   * @param generator the generator for which logger is to be set
   */
  public static void setOperationLog(PrintWriter pw, AbstractGenerator generator) {
    // This println statement causes system test runNoOutputTest to fail.
    if (debug) {
      System.out.println("setOperationLog(" + pw + ")");
    }
    if (pw != null) {
      OperationHistoryLogger historyLogger = new OperationHistoryLogger(pw);
      generator.setOperationHistoryLogger(historyLogger);
    }
  }
}
