package randoop.generation;

import randoop.operation.TypedOperation;

/**
 * Error class to signal generation errors that should stop Randoop execution.
 */
public class RandoopGenerationError extends Error {

  private static final long serialVersionUID = -2655768762421700468L;

  private final String operationName;
  private final TypedOperation operation;
  private final Throwable exception;

  /**
   * Create a {@link RandoopGenerationError}
   *
   * @param operationName
   * @param operation
   * @param exception
   */
  public RandoopGenerationError(
      String operationName, TypedOperation operation, Throwable exception) {
    this.operationName = operationName;
    this.operation = operation;
    this.exception = exception;
  }

  public String getOperationName() {
    return operationName;
  }

  public String getInstantiatedOperation() {
    return operation.toString();
  }

  public Throwable getException() {
    return exception;
  }
}
