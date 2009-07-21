package randoop;

/**
 * Represents the outcome of executing one statement in a sequence, for example
 * the value created by the statement, or the exception thrown. See implementors
 * for the possible outcomes.
 */
public interface ExecutionOutcome {

  /** How long the associated statement took to execute. */
  public long getExecutionTime();
}