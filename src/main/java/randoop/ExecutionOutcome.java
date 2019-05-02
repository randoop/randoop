package randoop;

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
  private final long executionTime;

  /**
   * The standard output and error output of executing the sequence. Only populated if {@link
   * randoop.main.GenInputsAbstract#capture_output} is true.
   */
  String output = null;

  /** @param executionTime the execution time, in nanoseconds */
  public ExecutionOutcome(long executionTime) {
    this.executionTime = executionTime;
  }

  /**
   * How long the associated statement took to execute, in nanoseconds.
   *
   * @return the execution time for the statement, in nanoseconds
   */
  public long getExecutionTime() {
    return executionTime;
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
