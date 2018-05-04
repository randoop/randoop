package randoop.main;

/**
 * Indicates a user error in a command supplied to Randoop. Randoop prints the message and also
 * prints how to get help.
 */
public class RandoopCommandError extends RandoopUsageError {

  private static final long serialVersionUID = 1L;

  /**
   * Creates a {@link RandoopCommandError} with the given message.
   *
   * @param message the exception message
   */
  public RandoopCommandError(String message) {
    super(message);
  }

  /**
   * Creates a {@link RandoopCommandError} with the given message and causing exception.
   *
   * @param message the exception message
   * @param cause the causing exception
   */
  RandoopCommandError(String message, Throwable cause) {
    super(message, cause);
  }
}
