package randoop;

/** Means that the statement that this outcome represents was not executed. */
public class TimeoutExecution extends ExecutionOutcome {

  public static TimeoutExecution timeoutExecutionSingleton = new TimeoutExecution();

  private TimeoutExecution() {
    super(-1);
  }

  public static TimeoutExecution create() {
    return timeoutExecutionSingleton;
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
