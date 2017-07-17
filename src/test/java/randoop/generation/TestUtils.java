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
      Randomness.selectionLog = getSimpleLog(selectionLog);
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
      historyLogger = new OperationHistoryLogger(getSimpleLog(operationLog));
      generator.setOperationHistoryLogger(historyLogger);
    }
  }

  /**
   * Creates a {@code plume.SimpleLog} that writes to standard output if {@code filename} is "-" (a
   * hyphen), or the file with name {@code filename}.
   *
   * <p>Method should be replaced by call to {@code SimpleLog(String)} constructor once plume is
   * updated.
   *
   * @param filename the name of a file or a hyphen to indicate standard output, must not be null
   * @return a {@code SimpleLog} object that writes to the location indicated by {@code filename}
   */
  private static SimpleLog getSimpleLog(String filename) {
    SimpleLog logger = null;
    if (filename.equals("-")) {
      System.out.println("stdout logger");
      logger = new SimpleLog(true);
    } else {
      logger = new SimpleLog(filename, true);
    }
    return logger;
  }
}
