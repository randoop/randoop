package randoop;

/** Means that the execution of a statement threw an exception. */
public class ExceptionalExecution extends ExecutionOutcome {

  /** The execution that caused the this ExceptionalExecution. */
  private final Throwable exception;

  /**
   * Creates an ExceptionalExecution.
   *
   * @param exception the exception that was thrown
   * @param executionTimeNanos the execution time, in nanoseconds
   */
  public ExceptionalExecution(Throwable exception, long executionTimeNanos) {
    super(executionTimeNanos);
    if (exception == null) {
      throw new IllegalArgumentException("exception must be non-null");
    }
    this.exception = exception;
  }

  /**
   * Return the exception.
   *
   * @return the exception
   */
  public Throwable getException() {
    return this.exception;
  }

  /**
   * Warning: this method calls toString() of code under test, which may have arbitrary behavior. We
   * use this method in randoop.test.SequenceTests.
   */
  @Override
  public String toString() {
    return String.format(
        "[ExceptionalExecution, %s, at %s]",
        exception.getClass().getName(), exception.getStackTrace()[0]);
  }
}
