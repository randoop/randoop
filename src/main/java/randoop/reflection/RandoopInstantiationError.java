package randoop.reflection;

/** Error class to signal an error while instantiating a generic operation. */
public class RandoopInstantiationError extends RuntimeException {

  /** ID for serializing this class. */
  private static final long serialVersionUID = 3611612630372756721L;

  /** The name of the operation that could not be instantiated. */
  private final String opName;

  /** The exception that was thrown while instantiating the operation. */
  private final Throwable exception;

  /**
   * Creates a {@link RandoopInstantiationError} for the given operation and exception.
   *
   * @param opName the name of the operation that could not be instantiated
   * @param exception the exception thrown while instantiating the operation
   */
  public RandoopInstantiationError(String opName, Throwable exception) {
    this.opName = opName;
    this.exception = exception;
  }

  /**
   * Returns the name of the operation that could not be instantiated.
   *
   * @return the name of the operation that could not be instantiated
   */
  public String getOpName() {
    return opName;
  }

  /**
   * Returns the exception that was thrown while instantiating the operation.
   *
   * @return the exception that was thrown while instantiating the operation
   */
  public Throwable getException() {
    return exception;
  }
}
