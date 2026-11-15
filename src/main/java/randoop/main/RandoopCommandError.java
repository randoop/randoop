package randoop.main;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Indicates a user error in a command supplied to Randoop. Randoop prints the message and also
 * prints how to get help.
 */
public class RandoopCommandError extends RandoopUsageError {

  /** ID for serializing this class. */
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
   * @param message the exception message, or null if none
   * @param cause the causing exception
   */
  RandoopCommandError(@Nullable String message, Throwable cause) {
    super(message, cause);
  }
}
