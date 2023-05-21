package randoop.main;

/**
 * Indicates a user error in invoking Randoop.
 *
 * @see randoop.main.RandoopBug
 */
// TODO: Does getMessage() include the getMessage() of the cause?  If not, define it to do so.
public class RandoopUsageError extends RuntimeException {

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
   * @param message the exception message
   * @param cause the causing exception
   */
  public RandoopUsageError(String message, Throwable cause) {
    super(message, cause);
  }
}
