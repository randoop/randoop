package randoop.condition;

import java.nio.file.Path;

/**
 * Indicates a problem creating {@link ExecutableBooleanExpression} (usually a syntax error in the
 * condition text) or an exception thrown when evaluating it. This is a user error, not a bug in
 * Randoop.
 */
public class RandoopSpecificationError extends Error {

  private static final long serialVersionUID = 3517219213949862963L;

  Path file = null;

  String thisMessage = null;

  /**
   * Create a {@link RandoopSpecificationError} with the given message.
   *
   * @param message the error message
   */
  RandoopSpecificationError(String message) {
    super(message);
  }

  /**
   * Create a {@link RandoopSpecificationError} with the given message and cause.
   *
   * @param message the error message
   * @param cause the causing exception
   */
  RandoopSpecificationError(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Indicate which file was being read when the error occurred.
   *
   * @param file the file
   */
  public void setFile(Path file) {
    this.file = file;
  }

  /**
   * Set the local message (ignoring the message of the cause).
   *
   * @param message the string to use as the local message for this Error
   */
  public void setThisMessage(String message) {
    thisMessage = message;
  }

  /**
   * Set the local message (ignoring the message of the cause).
   *
   * @return the local message (ignoring the message of the cause)
   */
  public String getThisMessage() {
    return thisMessage;
  }

  @Override
  public String getMessage() {
    String thisLocalMessage = (thisMessage != null ? thisMessage : super.getMessage());
    String fileMessage = (file != null ? (" while reading file " + file) : "");
    String causeMessage =
        (getCause() != null && getCause().getMessage() != null
            ? (": " + getCause().getMessage())
            : "");
    return thisLocalMessage + fileMessage + causeMessage;
  }
}
