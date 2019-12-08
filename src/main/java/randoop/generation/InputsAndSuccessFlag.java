package randoop.generation;

import java.util.List;
import randoop.sequence.Sequence;

/**
 * Represents a set of inputs, plus a boolean that is true if this is a good set of inputs.
 *
 * <p>This is the return type for ForwardGenerator's private {@code selectInputs} method, which is
 * responsible for selecting a set of component sequences to be concatenated into a longer sequence.
 */
class InputsAndSuccessFlag {

  /**
   * True if private method {@code ForwardGenerator.selectInputs(TypedOperation operation)} was able
   * to find component sequences for all the input types required by the given statement.
   */
  public boolean success;

  /** The sequences that create the inputs. */
  public List<Sequence> sequences;

  /**
   * Same length as {@code sequences}. Each integer is an index into the corresponding sequence, and
   * is a statement that creates/returns a value.
   */
  public List<Integer> indices;

  public InputsAndSuccessFlag(boolean success, List<Sequence> sequences, List<Integer> vars) {
    this.success = success;
    this.sequences = sequences;
    this.indices = vars;
  }
}
