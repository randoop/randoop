package randoop.main;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Indicates a user error in invoking Randoop.
 *
 * @see randoop.main.RandoopBug
 */
// TODO: Does getMessage() include the getMessage() of the cause?  If not, define it to do so.
public class RandoopUsageError extends RuntimeException {

  /** ID for serializing this class. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a {@link RandoopUsageError} with the given message.
   *
   * @param message the exception message
   */
  public RandoopUsageError(String message) {
    super(message);
  }

  /**
   * Creates a {@link RandoopUsageError} with the given message and causing exception.
   *
   * @param message the exception message, or null if none
   * @param cause the causing exception
   */
  public RandoopUsageError(@Nullable String message, Throwable cause) {
    super(message, cause);
  }
}
