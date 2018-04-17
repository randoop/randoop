package randoop.generation;

import randoop.operation.TypedOperation;

/**
 * An interface for selecting an operation for the {@link ForwardGenerator} to use in constructing a
 * new test sequence.
 */
public interface TypedOperationSelector {

  /**
   * Select a method, from the set of methods under test, to use to create a new and unique test
   * sequence.
   *
   * @return the selected method
   */
  public abstract TypedOperation selectOperation();
}
