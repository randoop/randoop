package randoop;

/** Thrown to indicate exceptional behavior that definitely indicates a bug in Randoop. */
public class BugInRandoopException extends RuntimeException {

  private static final long serialVersionUID = -5508231959912731870L;

  /** Create a BugInRandoopException with no additional details. */
  public BugInRandoopException() {
    super();
  }

  /**
   * Create a BugInRandoopException with the given message.
   *
   * @param message exception message
   */
  public BugInRandoopException(String message) {
    super(message);
  }

  /**
   * Create a BugInRandoopException with the given message and cause.
   *
   * @param message exception message
   * @param cause exception cause
   */
  public BugInRandoopException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a BugInRandoopException with the given cause.
   *
   * @param cause exception cause
   */
  public BugInRandoopException(Throwable cause) {
    super(cause);
  }
}
