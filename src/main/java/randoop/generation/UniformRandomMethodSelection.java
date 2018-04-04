package randoop.generation;

import java.util.List;
import randoop.operation.TypedOperation;
import randoop.util.Randomness;

/** This class selects a method with uniform probability. */
public class UniformRandomMethodSelection implements TypedOperationSelector {
  private final List<TypedOperation> operations;

  /**
   * Saves a reference to the list of methods under test.
   *
   * @param operations methods under test.
   */
  public UniformRandomMethodSelection(List<TypedOperation> operations) {
    this.operations = operations;
  }

  /**
   * Selects an operation with uniform probability.
   *
   * @return a random operation.
   */
  @Override
  public TypedOperation selectOperation() {
    return Randomness.randomMember(this.operations);
  }
}
