package randoop.main;

/** Thrown to indicate exceptional behavior that definitely indicates a bug in Randoop. */
public class RandoopBug extends RuntimeException {

  private static final long serialVersionUID = -5508231959912731870L;

  /** Create a RandoopBug with no additional details. */
  public RandoopBug() {
    super();
  }

  /**
   * Create a RandoopBug with the given message.
   *
   * @param message exception message
   */
  public RandoopBug(String message) {
    super(message);
  }

  /**
   * Create a RandoopBug with the given message and cause.
   *
   * @param message exception message
   * @param cause exception cause
   */
  public RandoopBug(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a RandoopBug with the given cause.
   *
   * @param cause exception cause
   */
  public RandoopBug(Throwable cause) {
    super(cause);
  }
}
