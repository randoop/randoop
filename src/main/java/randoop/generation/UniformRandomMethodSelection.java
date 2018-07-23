package randoop.generation;

import java.util.List;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.util.Randomness;

/** This class selects a method, from the list of methods under test, with uniform probability. */
public class UniformRandomMethodSelection implements TypedOperationSelector {

  /** The list of methods under test. We will select uniformly at random from this list. */
  private final List<TypedOperation> operations;

  /**
   * Create a selector that selects uniformly at random from the given operations.
   *
   * @param operations methods under test
   */
  public UniformRandomMethodSelection(List<TypedOperation> operations) {
    // Temporary implementation note:  a copy is not made.
    // Doing so causes various system tests to fail due to changes in coverage. We discovered that
    // this was caused by {@link ForwardGenerator} which was removing parameter-less operations
    // after testing them once.
    this.operations = operations;
  }

  /**
   * Selects an operation with uniform random probability.
   *
   * @return a random operation
   */
  @Override
  public TypedOperation selectOperation() {
    return Randomness.randomMember(this.operations);
  }

  /**
   * Does nothing. This selection strategy does not need to make use of information related to the
   * newly-generated sequence that was classified as a regression test.
   *
   * @param sequence newly-created sequence that was classified as a regression test
   */
  @Override
  public void newRegressionTestHook(Sequence sequence) {}
}
