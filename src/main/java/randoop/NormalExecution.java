package randoop;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.StringsPlume;

/**
 * Means that the statement that this result represents completed normally.
 *
 * <p>Method r.getRuntimeVariable() returns the runtime value that the statement returns (null for
 * void method calls).
 *
 * <p>IMPORTANT NOTE: The object returned by getRuntimeVariable() is the actual runtime object
 * created during execution of the sequence (call it s). This means that if you invoke s.execute(v)
 * and then you invoke s.getResult(i).getRuntimeVariable(), the state of the object you get is the
 * FINAL state of the object after s finished executing, NOT the state of the object after the i-th
 * statement was executed. Similarly, if you invoke getRuntimeVariable() sometime in the middle of
 * the execution of s (e.g. you're an ExecutionVisitor and you invoke getRuntimeVariable()), you'll
 * get the state in whatever state it is at that point in the execution.
 */
public class NormalExecution extends ExecutionOutcome {

  /** The value created by executing the statement. */
  private final @Nullable Object result;

  /**
   * Creates a new NormalExecution.
   *
   * @param result the return value
   * @param executionTimeNanos the execution time, in nanoseconds
   */
  public NormalExecution(@Nullable Object result, long executionTimeNanos) {
    super(executionTimeNanos);
    this.result = result;
  }

  public @Nullable Object getRuntimeValue() {
    return this.result;
  }

  /**
   * randoop.test.SequenceTests uses this method.
   *
   * <p>Note that toString() of code under test may have arbitrary behavior.
   */
  @Override
  public String toString() {
    return String.format("[NormalExecution %s]", StringsPlume.toStringAndClass(result));
  }
}
