package randoop.generation;

import randoop.operation.TypedOperation;

/**
 * Interface for classes that log the usage of operations in the generated sequences. Represents a
 * table of counts indexed by operations and the outcome (as a {@link OperationOutcome}.
 */
public interface OperationHistoryLogInterface {
  /**
   * Increments the count for {@code operation} and {@code outcome}.
   *
   * @param operation the {@link TypedOperation}
   * @param outcome the generation outcome for the operation
   */
  void add(TypedOperation operation, OperationOutcome outcome);

  /**
   * Prints a table showing the counts for each operation-outcome pair in the operation history log.
   */
  void outputTable();
}
