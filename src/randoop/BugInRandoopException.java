package randoop;

/**
 * Thrown to indicate that the exceptional behavior shouldn't have happened and
 * is a bug in randoop.
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

}
