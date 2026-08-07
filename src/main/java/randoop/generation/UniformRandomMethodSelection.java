package randoop.generation;

import java.util.ArrayList;
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
    this.operations = new ArrayList<>(operations);
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

  /**
   * Stops considering the given operation, by removing it from the list that {@link
   * #selectOperation} chooses from.
   *
   * @param operation the operation that is no longer under test
   */
  @Override
  public void removeOperation(TypedOperation operation) {
    operations.remove(operation);
  }
}
