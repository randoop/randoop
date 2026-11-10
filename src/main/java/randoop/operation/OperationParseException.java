package randoop.operation;

/**
 * Thrown when a string cannot be parsed as an operation, or as a component of an operation such as
 * a type, method, constructor, or field.
 */
// Not meant for serialization.
public class OperationParseException extends Exception {

  /** ID for serializing this class. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an {@code OperationParseException} with the given message.
   *
   * @param string the detail message
   */
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
