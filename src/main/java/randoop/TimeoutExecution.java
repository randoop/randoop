package randoop;

import randoop.util.TimeoutExceededException;

/** Means that the statement that this outcome represents was not executed. */
public class TimeoutExecution extends ExecutionOutcome {

  private final Throwable exception;

  /** @param exception the TimeoutExceededException exception that was thrown */
  public TimeoutExecution(TimeoutExceededException exception) {
    super(-1);
    if (exception == null) {
      throw new IllegalArgumentException("exception must be non-null");
    }
    this.exception = exception;
  }

  public Throwable getException() {
    return this.exception;
  }

  @Override
  public String toString() {
    return "<timeout>";
  }

  @Override
  public long getExecutionTime() {
    throw new IllegalStateException("TimeoutExecution outcome has no execution time.");
  }
}
