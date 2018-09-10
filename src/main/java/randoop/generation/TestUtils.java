package randoop.generation;

import java.io.IOException;
import java.io.PrintWriter;
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
   * Use system properties to set all logs.
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
   * @param file the file to write the log to; does nothing if file is null
   */
  @SuppressWarnings(
      "DefaultCharset") // TODO: make GenInputsAbstract.log a Writer; change command-line arguments.
  public static void setRandoopLog(String file) {
    if (debug) {
      System.out.println("setRandoopLog(" + file + ")");
    }
    if (file != null && !file.isEmpty()) {
      try {
        GenInputsAbstract.log = new FileWriterWithName(file);
        // GenInputsAbstract.log = Files.newBufferedWriter(Paths.get(file), UTF_8);
      } catch (IOException ioe) {
        // TODO: clarify that this is a user error
        throw new Error("Cannot write file " + file, ioe);
      }
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
   * @param file the file to write the log to; does nothing if file is null
   */
  @SuppressWarnings("DefaultCharset") // TODO: should specify a charset
  public static void setSelectionLog(String file) {
    if (debug) {
      System.out.println("setSelectionLog(" + file + ")");
    }
    if (file != null && !file.isEmpty()) {
      try {
        setSelectionLog(new FileWriterWithName(file));
      } catch (IOException e) {
        throw new Error("problem creating FileWriter for " + file, e);
      }
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
   * If the file is non-null, sets the operation history logger for the generator using the file.
   *
   * @param file the file to write the log to; does nothing if file is null
   * @param generator the generator for which logger is to be set
   */
  public static void setOperationLog(String file, AbstractGenerator generator) {
    // This println statement causes system test runNoOutputTest to fail.
    if (debug) {
      System.out.println("setOperationLog(" + file + ")");
    }
    if (file != null && !file.isEmpty()) {
      setOperationLog(file, generator);
    }
  }

  /**
   * If the PrintWriter is non-null, sets the operation history logger for the generator using the
   * file.
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
