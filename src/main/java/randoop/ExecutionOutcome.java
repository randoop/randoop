package randoop;

/**
 * Represents the outcome of executing one statement in a sequence, including
 * the value returned by the statement, or the exception thrown.
 */
public abstract class ExecutionOutcome {

  String output = null;

  public ExecutionOutcome() {
  }

  /**
   * How long the associated statement took to execute.
   *
   * @return the execution time for the statement
   */
  public abstract long getExecutionTime();

  /**
   * Set the output of the statement
   *
   * @param output  the statement output
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
