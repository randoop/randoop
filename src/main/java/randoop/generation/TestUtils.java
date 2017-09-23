package randoop.generation;

import plume.SimpleLog;
import randoop.util.Randomness;

/**
 * Utility methods for setting up selection and operation-history logging in integration tests that
 * use a generator.
 */
public class TestUtils {

  /**
   * Uses the system property {@code randoop.selection.log} to set {@link Randomness#selectionLog}.
   */
  public static void setSelectionLog() {
    String selectionLog = System.getProperty("randoop.selection.log");
    if (selectionLog != null && !selectionLog.isEmpty()) {
      Randomness.selectionLog = new SimpleLog(selectionLog);
    }
  }

  /**
   * Uses the argument to set {@link Randomness#selectionLog}.
   *
   * @param file the file to write the log to
   */
  public static void setSelectionLog(String file) {
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
    // System.out.println("in setOperationLog, file = " + file);
    if (file != null && !file.isEmpty()) {
      SimpleLog logger = new SimpleLog(file);
      OperationHistoryLogger historyLogger = new OperationHistoryLogger(logger);
      generator.setOperationHistoryLogger(historyLogger);
    }
  }
}
