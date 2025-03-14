package randoop.generation;

import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;

/**
 * An interface for selecting an operation for the {@link ForwardGenerator} to use in constructing a
 * new test sequence.
 */
public interface TypedOperationSelector {

  /**
   * Select a method, from the set of methods under test, to use to create a new test sequence.
   *
   * @return the selected method
   */
  public abstract TypedOperation selectOperation();

  /**
   * Take action based on the given {@link Sequence} was classified as a regression test.
   *
   * @param sequence newly created sequence that was classified as a regression test
   */
  public abstract void newRegressionTestHook(Sequence sequence);
}
