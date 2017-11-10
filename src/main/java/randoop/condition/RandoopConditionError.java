package randoop.condition;

/**
 * Indicates a problem creating {@link ExecutableBooleanExpression} (usually a syntax error in the
 * condition text) or an exception thrown when evaluating it.
 */
public class RandoopConditionError extends Error {

  private static final long serialVersionUID = 3517219213949862963L;

  /**
   * Create a {@link RandoopConditionError} with the given message.
   *
   * @param message the error message
   */
  RandoopConditionError(String message) {
    super(message);
  }

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
