package randoop.generation;

import randoop.sequence.ExecutableSequence;
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

  /**
   * Make use of the given {@link ExecutableSequence} to eventually update the weight map using
   * eSeq's execution time.
   *
   * @param eSeq the recently executed sequence
   */
  void assignExecTimeForSequence(ExecutableSequence eSeq);
}
