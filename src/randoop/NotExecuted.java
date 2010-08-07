package randoop;

/**
 * Means that the statement that this outcome represents was not executed.
 */
public class NotExecuted extends ExecutionOutcome {

  private static NotExecuted notExecutedSingleton = new NotExecuted();

  private NotExecuted() {
    // Empty body.
  }

  public static NotExecuted create() {
    return notExecutedSingleton;
  }

  @Override
  public String toString() {
    return "<not_executed>";
  }

  public long getExecutionTime() {
    throw new IllegalStateException("NotExecuted outcome has no execution time.");
  }
}
