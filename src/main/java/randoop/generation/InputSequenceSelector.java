package randoop.generation;

import randoop.sequence.Sequence;
import randoop.util.SimpleList;

/** Interface for selecting sequences as input for creating new sequences. */
public interface InputSequenceSelector {
  /**
   * Choose a sequence used as input for creating a new sequence.
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  Sequence selectInputSequence(SimpleList<Sequence> candidates);
}
