package randoop.reflection;

/** Represents when a user provides a signature that is excluded by a predicate. */
public class FailedPredicateException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Create a new FailedPredicateException.
   *
   * @param message the detail message
   */
  public FailedPredicateException(String message) {
    super(message);
  }

  /**
   * Create a new FailedPredicateException.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public FailedPredicateException(String message, Throwable cause) {
    super(message, cause);
  }
}
