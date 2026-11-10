package randoop.generation;

import randoop.operation.TypedOperation;

/**
 * The default implementation of the {@link OperationHistoryLogInterface}. Each method does nothing.
 */
public class DefaultOperationHistoryLogger implements OperationHistoryLogInterface {

  /** Creates a DefaultOperationHistoryLogger. */
  public DefaultOperationHistoryLogger() {}

  @Override
  public void add(TypedOperation operation, OperationOutcome outcome) {
    // these methods don't do anything
  }

  @Override
  public void outputTable() {
    // these methods don't do anything
  }
}
