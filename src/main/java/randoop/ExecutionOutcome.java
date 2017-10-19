package randoop;

/**
 * Represents the outcome of executing one statement in a sequence, including:
 *
 * <ul>
 *   <li>the value returned by the statement, or the exception thrown.
 *   <li>the execution time
 *   <li>the textual output
 * </ul>
 *
 * The subclasses are {@link NormalExecution}, {@link ExceptionalExecution}, and {@link
 * NotExecuted}.
 */
public abstract class ExecutionOutcome {

  private final long executionTime;

  String output = null;

  public ExecutionOutcome(long executionTime) {
    this.executionTime = executionTime;
  }

  /**
   * How long the associated statement took to execute.
   *
   * @return the execution time for the statement
   */
  public long getExecutionTime() {
    return executionTime;
  }

  /**
   * Set the output of the statement
   *
   * @param output the statement output
   */
  public void set_output(String output) {
    this.output = output;
  }

  /**
   * Retrieve the output of the statement
   *
   * @return the statement output
   */
  public String get_output() {
    return output;
  }
}
