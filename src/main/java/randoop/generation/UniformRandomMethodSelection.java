package randoop.generation;

import java.util.ArrayList;
import java.util.List;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.util.Randomness;

/** This class selects a method with uniform probability. */
public class UniformRandomMethodSelection implements TypedOperationSelector {
  private final List<TypedOperation> operations;

  /**
   * Creates a copy of the list of the methods under test.
   *
   * @param operations methods under test
   */
  public UniformRandomMethodSelection(List<TypedOperation> operations) {
    this.operations = new ArrayList<>(operations);
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
