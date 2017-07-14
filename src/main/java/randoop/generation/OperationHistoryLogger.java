package randoop.generation;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import plume.SimpleLog;
import randoop.operation.TypedOperation;

/**
 * The working implementation of a {@link OperationHistoryLogInterface} that will collect and print
 * the operation history log to the {@code PrintWriter} given when constructed.
 */
public class OperationHistoryLogger implements OperationHistoryLogInterface {

  /** The {@code PrintWriter} for outputting the operation history as a table */
  private final SimpleLog logger;

  /** A sparse representation for the operation-outcome table */
  private final Map<TypedOperation, Map<OperationOutcome, Integer>> operationMap;

  /**
   * Creates an {@link OperationHistoryLogger} that will write to the given {@code PrintWriter}.
   *
   * @param logger the {@code PrintWriter} for writing the table from the created operation history
   */
  public OperationHistoryLogger(SimpleLog logger) {
    this.logger = logger;
    this.operationMap = new LinkedHashMap<>();
    this.logger.line_oriented = false; // don't want the logger to manage newlines
  }

  @Override
  public void add(TypedOperation operation, OperationOutcome outcome) {
    Map<OperationOutcome, Integer> outcomeMap = operationMap.get(operation);
    int count = 0;
    if (outcomeMap == null) {
      outcomeMap = new EnumMap<>(OperationOutcome.class);
    } else {
      Integer countInteger = outcomeMap.get(outcome);
      if (countInteger != null) {
        count = countInteger;
      }
    }
    count += 1;
    outcomeMap.put(outcome, count);
    operationMap.put(operation, outcomeMap);
  }

  @Override
  public void outputTable() {
    int maxNameLength = 0;
    for (TypedOperation operation : operationMap.keySet()) {
      int nameLength = operation.getSignatureString().length();
      maxNameLength = Math.max(nameLength, maxNameLength);
    }
    Map<OperationOutcome, String> formatMap = printHeader(maxNameLength);
    for (Map.Entry<TypedOperation, Map<OperationOutcome, Integer>> entry :
        operationMap.entrySet()) {
      printRow(maxNameLength, formatMap, entry.getKey(), entry.getValue());
    }
  }

  /**
   * Writes the header for the operation history table, and constructs a map of format strings for
   * the columns of the table matching the length of the outcome names.
   *
   * @param firstColumnLength the width to use for the first column
   * @return a map from {@link OperationOutcome} value to numeric column format for subsequent rows
   */
  private Map<OperationOutcome, String> printHeader(int firstColumnLength) {
    Map<OperationOutcome, String> formatMap = new EnumMap<>(OperationOutcome.class);
    logger.log("%-" + firstColumnLength + "s", "Operation");
    for (OperationOutcome outcome : OperationOutcome.values()) {
      logger.log("\t%" + outcome.name().length() + "s", outcome);
      formatMap.put(outcome, "\t%" + outcome.name().length() + "d");
    }
    logger.log("%n");
    return formatMap;
  }

  /**
   * Writes a row for a particular operation consisting of the operation signature and the counts of
   * sequences for each outcome.
   *
   * @param firstColumnLength the width to use for the first column
   * @param formatMap the map of format strings for the counts for each outcome
   * @param operation the operation for the row
   * @param countMap the map of counts for the operation and each outcome
   */
  private void printRow(
      int firstColumnLength,
      Map<OperationOutcome, String> formatMap,
      TypedOperation operation,
      Map<OperationOutcome, Integer> countMap) {
    logger.log("%-" + firstColumnLength + "s", operation.getSignatureString());
    for (OperationOutcome outcome : OperationOutcome.values()) {
      Integer count = countMap.get(outcome);
      if (count == null) {
        count = 0;
      }
      logger.log(formatMap.get(outcome), count);
    }
    logger.log("%n");
  }
}
