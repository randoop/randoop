package randoop.main;

/** Exception for classname errors. */
public class RandoopClassNameError extends Error {
  private static final long serialVersionUID = -3625971508842588810L;

  public String className;

  public RandoopClassNameError(String className, String message) {
    super(message);
    this.className = className;
  }

  public RandoopClassNameError(String className, String message, Throwable cause) {
    super(message, cause);
    this.className = className;
  }
}
