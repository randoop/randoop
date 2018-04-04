package randoop.generation;

import randoop.operation.TypedOperation;

/**
 * An interface for selecting an operation for the {@link ForwardGenerator} to use in constructing a
 * new test sequence.
 */
public interface TypedOperationSelector {
  public abstract TypedOperation selectOperation();
}
