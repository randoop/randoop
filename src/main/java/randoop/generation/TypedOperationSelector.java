package randoop.generation;

import randoop.operation.TypedOperation;

/**
 * This class presents an interface for selecting the next operation for
 * the {@link ForwardGenerator} to use in constructing a new test sequence.
 */
public interface TypedOperationSelector {
    public abstract TypedOperation selectNextOperation();
}
