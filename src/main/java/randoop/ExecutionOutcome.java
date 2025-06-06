package randoop;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents the outcome of executing one statement in a sequence, including:
 *
 * <ul>
 *   <li>the value returned by the statement, or the exception thrown.
 *   <li>the execution time
 *   <li>the textual output
 * </ul>
 */
public abstract class ExecutionOutcome {

  /** The execution time, in nanoseconds. */
  private final long executionTimeNanos;

  /**
   * The standard output and error output of executing the sequence. Only populated if {@link
   * randoop.main.GenInputsAbstract#capture_output} is true.
   */
  @Nullable String output = null;

  /**
   * Creates a new ExecutionOutcome.
   *
   * @param executionTimeNanos the execution time, in nanoseconds
   */
  protected ExecutionOutcome(long executionTimeNanos) {
    this.executionTimeNanos = executionTimeNanos;
  }

  /**
   * How long the associated statement took to execute, in nanoseconds.
   *
   * @return the execution time for the statement, in nanoseconds
   */
  public long getExecutionTimeNanos() {
    return executionTimeNanos;
  }

  /**
   * Set the output of the statement.
   *
   * @param output the statement output
   */
  public void set_output(String output) {
    this.output = output;
  }

  /**
   * Retrieve the output of the statement.
   *
   * @return the statement output
   */
  public String get_output() {
    return output;
  }
}
