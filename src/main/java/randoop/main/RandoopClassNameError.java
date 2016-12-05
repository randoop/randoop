package randoop.main;

/**
 * Exception for classname errors
 */
public class RandoopClassNameError extends Error {
  private static final long serialVersionUID = -3625971508842588810L;

  public RandoopClassNameError(String message) {
    super(message);
  }

  public RandoopClassNameError(String message, Throwable cause) {
    super(message, cause);
  }
}
