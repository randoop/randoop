package randoop;

/**
 * Thrown to indicate exceptional behavior that definitely indicates
 * a bug in Randoop.
 */
public class BugInRandoopException extends RuntimeException {

  private static final long serialVersionUID = -5508231959912731870L;

  public BugInRandoopException() {
    super();
  }

  public BugInRandoopException(String message) {
    super(message);
  }

  public BugInRandoopException(Exception e) {
    super(e);
  }

  public BugInRandoopException(Throwable exception) {
    super(exception);
  }
}
