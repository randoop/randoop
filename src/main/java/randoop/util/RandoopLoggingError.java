package randoop.util;

/** Exception for tracking errors in logging that should result in Randoop termination. */
public class RandoopLoggingError extends RuntimeException {
  /** ID for serializing this class. */
  private static final long serialVersionUID = -3641426773814539646L;

  /**
   * Creates a {@code RandoopLoggingError} with the given message.
   *
   * @param message the detail message
   */
  public RandoopLoggingError(String message) {
    super(message);
  }
}
