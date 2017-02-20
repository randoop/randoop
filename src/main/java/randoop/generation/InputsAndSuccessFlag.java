package randoop.generation;

import java.util.LinkedList;
import java.util.List;

import randoop.sequence.Sequence;

/**
 * Return type for ForwardGenerator's private selectInputs method, which is responsible
 * for selecting a set of component sequences to be concatenated into a longer
 * sequence.
 *
 */
class InputsAndSuccessFlag {

  /**
   * True if private method {@code ForwardGenerator.selectInputs(TypedOperation operation)}
   * was able to find component sequences for all the input types required
   * by the given statement.
   */
  public boolean success;

  public List<Sequence> sequences;
  public List<Integer> indices;

  public InputsAndSuccessFlag(boolean success, List<Sequence> sequences, List<Integer> vars) {

    if (sequences == null) {
      throw new IllegalArgumentException("Argument 'sequences' cannot be null");
    }

    if (vars == null) {
      throw new IllegalArgumentException("Argument 'vars' cannot be null");
    }

    this.success = success;
    this.sequences = sequences;
    this.indices = vars;
  }

  private int getLastIndexFromCurrentSequences() {
    int lastIndex = this.sequences.stream().map(s -> s.size()).reduce(0, (l1, l2) -> l1 + l2);

    return lastIndex;
  }

  public InputsAndSuccessFlag concatenate(InputsAndSuccessFlag tail) {
    if (tail == null) {
      return this;
    } else {
      boolean concatSuccess = this.success && tail.success;

      List<Sequence> concatSeqs = new LinkedList<>(this.sequences);
      concatSeqs.addAll(tail.sequences);

      List<Integer> concatIndices = new LinkedList<>(this.indices);
      int offset = getLastIndexFromCurrentSequences() + 1;

      for (Integer oldIndex : tail.indices) {
        concatIndices.add(oldIndex + offset);
      }

      InputsAndSuccessFlag concatenation =
          new InputsAndSuccessFlag(concatSuccess, concatSeqs, concatIndices);

      return concatenation;
    }
  }
}
