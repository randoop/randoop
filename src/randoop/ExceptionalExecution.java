package randoop;

/**
 * Means that the execution of a statement threw an exception.
 */
public class ExceptionalExecution extends ExecutionOutcome {

  private final Throwable exception;
  private final long executionTime;

  public ExceptionalExecution(Throwable exception, long executionTime) {
    if (exception == null) throw new IllegalArgumentException();
    this.exception = exception;
    this.executionTime = executionTime;
  }

  public Throwable getException() {
    return this.exception;
  }

  /** Warning: this method calls toString() of code under test, which may have
   * arbitrary behavior. We use this method in randoop.test.SequenceTests.
   */
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("// <ExceptionalExecution, exception type="
             + exception.getClass().getName());
    b.append(">;");
    return b.toString();
  }

  public long getExecutionTime() {
    return executionTime;
  }
}
