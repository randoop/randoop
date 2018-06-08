package randoop.generation;

import java.util.List;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.util.Randomness;

/** This class selects a method with uniform probability. */
public class UniformRandomMethodSelection implements TypedOperationSelector {
  private final List<TypedOperation> operations;

  /**
   * Maintains a reference to the list of the methods under test. Note that a copy is not made.
   * Doing so causes various system tests to fail due to changes in coverage. We've observed that
   * for some reason the resulting "normal method executions" to drop significantly.
   *
   * @param operations methods under test
   */
  public UniformRandomMethodSelection(List<TypedOperation> operations) {
    this.operations = operations;
  }

  /**
   * Selects an operation with uniform, random probability.
   *
   * @return a random operation
   */
  @Override
  public TypedOperation selectOperation() {
    return Randomness.randomMember(this.operations);
  }

  /**
   * This class does not need to make use of information related to the newly generated sequence
   * that was classified as a regression test.
   *
   * @param sequence newly created sequence that was classified as a regression test
   */
  @Override
  public void newRegressionTestHook(Sequence sequence) {}
}
