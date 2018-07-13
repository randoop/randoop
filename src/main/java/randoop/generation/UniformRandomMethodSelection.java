package randoop.generation;

import java.util.List;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.util.Randomness;

/** This class selects a method, from the list of methods under test, with uniform probability. */
public class UniformRandomMethodSelection implements TypedOperationSelector {

  /** The list of methods under test from which we will select. */
  private final List<TypedOperation> operations;

  /**
   * Maintains a reference to the list of the methods under test. Note that a copy is not made.
   * Doing so causes various system tests to fail due to changes in coverage. We discovered that
   * this was caused by {@link ForwardGenerator} which was removing parameter-less operations after
   * testing them once.
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
