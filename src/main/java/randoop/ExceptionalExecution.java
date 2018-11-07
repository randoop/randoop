package randoop;

/** Means that the execution of a statement threw an exception. */
public class ExceptionalExecution extends ExecutionOutcome {

  private final Throwable exception;

  /**
   * @param exception the exception that was thrown
   * @param executionTime the execution time, in nanoseconds
   */
  public ExceptionalExecution(Throwable exception, long executionTime) {
    super(executionTime);
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
    return "// <ExceptionalExecution, exception type=" + exception.getClass().getName() + ">;";
  }
}
