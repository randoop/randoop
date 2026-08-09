package randoop.operation;

// Not meant for serialization.
public class OperationParseException extends Exception {

  /** ID for serializing this class. */
  private static final long serialVersionUID = 1L;

  public OperationParseException(String string) {
    super(string);
  }

  /**
   * Creates an {@code OperationParseException} with the given message and cause.
   *
   * @param string the detail message
   * @param cause the cause of this exception
   */
  public OperationParseException(String string, Throwable cause) {
    super(string, cause);
  }
}
