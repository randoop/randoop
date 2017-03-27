package randoop.condition;

/** Created by bjkeller on 3/21/17. */
public class RandoopConditionError extends Error {
  private static final long serialVersionUID = 3517219213949862963L;

  RandoopConditionError(String s, Throwable cause) {
    super(s, cause);
  }

  @Override
  public String getMessage() {
    return super.getMessage() + ": " + getCause().getMessage();
  }
}
