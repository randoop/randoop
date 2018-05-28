package randoop.generation;

import java.util.Set;
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
   * Each {@link Sequence} within {@code inputSequences} is a subsequence of {@code eSeq}. At this
   * point, the given {@link ExecutableSequence} has been executed and contains the execution time
   * of the sequence as a whole. Make use of the execution time value that is stored within eSeq and
   * associate it with each of the input sequences.
   *
   * @param inputSequences the sequences that were chosen as the input to the method under test for
   *     creating a new and unique sequence
   * @param eSeq the recently executed sequence
   */
  void assignExecTimeForInputSequences(Set<Sequence> inputSequences, ExecutableSequence eSeq);
}
