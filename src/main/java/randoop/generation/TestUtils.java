package randoop.generation;

import java.io.FileWriter;
import java.io.IOException;
import plume.SimpleLog;
import randoop.main.GenInputsAbstract;
import randoop.util.Randomness;

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
   * @param file the file to write the log to
   */
  @SuppressWarnings(
      "DefaultCharset") // TODO: make GenInputsAbstract.log a Writer; change command-line arguments.
  public static void setRandoopLog(String file) {
    if (debug) {
      System.out.println("setRandoopLog(" + file + ")");
    }
    if (file != null && !file.isEmpty()) {
      try {
        GenInputsAbstract.log = new FileWriter(file);
        // GenInputsAbstract.log = Files.newBufferedWriter(Paths.get(file), UTF_8);
      } catch (IOException ioe) {
        // TODO: clarify that this is a user error
        throw new Error("Cannot write file " + file, ioe);
      }
    }
  }

  /**
   * Uses the system property {@code randoop.selection.log} to set {@link Randomness#selectionLog}.
   */
  public static void setSelectionLog() {
    String selectionLog = System.getProperty("randoop.selection.log");
    setSelectionLog(selectionLog);
  }

  /**
   * Uses the argument to set {@link Randomness#selectionLog}.
   *
   * @param file the file to write the log to
   */
  public static void setSelectionLog(String file) {
    if (debug) {
      System.out.println("setSelectionLog(" + file + ")");
    }
    if (file != null && !file.isEmpty()) {
      Randomness.selectionLog = new SimpleLog(file);
    }
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
   * @param file the file to write the log to
   * @param generator the generator for which logger is to be set
   */
  public static void setOperationLog(String file, AbstractGenerator generator) {
    // This println statement causes system test runNoOutputTest to fail.
    if (debug) {
      System.out.println("setOperationLog(" + file + ")");
    }
    if (file != null && !file.isEmpty()) {
      SimpleLog logger = new SimpleLog(file);
      OperationHistoryLogger historyLogger = new OperationHistoryLogger(logger);
      generator.setOperationHistoryLogger(historyLogger);
    }
  }
}
