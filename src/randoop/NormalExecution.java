package randoop;

/**
 * Means that the statement that this result represents completed normally.
 *
 * Method r.getRuntimeVariable() returns the runtime value that the statement
 * returns (null for void method calls).
 *
 * IMPORTANT NOTE: The object returned by getRuntimeVariable() is the actual
 * runtime object created during execution of the sequence (call it s). This
 * means that if you invoke s.execute(v) and then you invoke
 * s.getResult(i).getRuntimeVariable(), the state of the object you get is the
 * FINAL state of the object after s finished executing, NOT the state of the
 * object after the i-th statement was executed. Similarly, if you invoke
 * getRuntimeVariable() sometime in the middle of the execution of s (e.g. you're
 * an ExecutionVisitor and you invoke getRuntimeVariable()), you'll get the state
 * in whatever state it is at that point in the execution.
 */
public class NormalExecution extends ExecutionOutcome {

  private final Object result;
  private final long executionTime;

  public NormalExecution(Object result, long executionTime) {
    this.result = result;
    this.executionTime = executionTime;
  }

  public Object getRuntimeValue() {
    return this.result;
  }

  /**
   * Warning: this method calls toString() of code under test, which may have
   * arbitrary behavior. We use this method in randoop.test.SequenceTests.
   */
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("// <NormalExecution object=");
    if (result == null)
      b.append("null");
    else
      b.append("object-of-type-" + result.getClass().getName());
    b.append(">;");
    return b.toString();
  }

  public long getExecutionTime() {
    return executionTime;
  }
}
