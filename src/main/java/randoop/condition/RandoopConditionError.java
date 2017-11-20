package randoop.condition;

import java.io.File;

/**
 * Indicates a problem creating {@link ExecutableBooleanExpression} (usually a syntax error in the
 * condition text) or an exception thrown when evaluating it.
 */
public class RandoopConditionError extends Error {

  private static final long serialVersionUID = 3517219213949862963L;

  File file = null;

  String thisMessage = null;

  /**
   * Create a {@link RandoopConditionError} with the given message.
   *
   * @param message the error message
   */
  RandoopConditionError(String message) {
    super(message);
  }

  /**
   * Create a {@link RandoopConditionError} with the given message and cause.
   *
   * @param message the error message
   * @param cause the causing exception
   */
  RandoopConditionError(String message, Throwable cause) {
    super(message, cause);
  }

  /** Indicate which file was being read when the error occurred. */
  public void setFile(File file) {
    this.file = file;
  }

  /** Get the local message (ignoring the message of the cause). */
  public void setThisMessage(String message) {
    thisMessage = message;
  }

  /** Set the local message (ignoring the message of the cause). */
  public String getThisMessage() {
    return thisMessage;
  }

  @Override
  public String getMessage() {
    String thisLocalMessage = (thisMessage != null ? thisMessage : super.getMessage());
    String fileMessage = (file != null ? (" while reading file " + file) : "");
    String causeMessage = (getCause() != null ? (": " + getCause().getMessage()) : "");
    return thisLocalMessage + fileMessage + causeMessage;
  }
}
