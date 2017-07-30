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
   * If the system property {@code randoop.operation.history.log} is set, sets the operation history
   * logger for the generator using the destination given by the property.
   *
   * @param generator the generator for which logger is to be set
   */
  public static void setOperationLog(ForwardGenerator generator) {
    String operationLog = System.getProperty("randoop.operation.history.log");
    if (operationLog != null && !operationLog.isEmpty()) {
      OperationHistoryLogger historyLogger;
      historyLogger = new OperationHistoryLogger(new SimpleLog(operationLog));
      generator.setOperationHistoryLogger(historyLogger);
    }
  }
}
