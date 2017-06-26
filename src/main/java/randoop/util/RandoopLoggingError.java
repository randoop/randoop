package randoop.util;

/** Exception for tracking errors in logging that should result in Randoop termination. */
public class RandoopLoggingError extends Error {
  private static final long serialVersionUID = -3641426773814539646L;

  public RandoopLoggingError(String message) {
    super(message);
  }
}
