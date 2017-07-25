package randoop.condition;

/**
 * Exception to represent errors that occur during creation or evaluation of a {@link Condition}.
 */
public class RandoopConditionError extends Error {

  private static final long serialVersionUID = 3517219213949862963L;

  /**
   * Create a {@link RandoopConditionError} with the given message and cause.
   *
   * @param message the error message
   * @param cause the causing exception
   */
  RandoopConditionError(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public String getMessage() {
    return super.getMessage() + ": " + getCause().getMessage();
  }
}
