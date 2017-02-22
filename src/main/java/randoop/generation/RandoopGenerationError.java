package randoop.generation;

import randoop.operation.TypedOperation;

/**
 * Created by bjkeller on 2/22/17.
 */
public class RandoopGenerationError extends Error {

  private static final long serialVersionUID = -2655768762421700468L;

  private final String operationName;
  private final TypedOperation operation;
  private final Throwable exception;

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
