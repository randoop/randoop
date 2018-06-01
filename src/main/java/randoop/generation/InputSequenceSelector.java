package randoop.generation;

import java.util.List;
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
   * of the sequence as a whole. The Executable Sequence eSeq also contains an ExecutionOutcome for
   * each statement within the entire sequence. The ExecutionOutcome contains the execution time for
   * a given statement. This information can be used to compute the execution time of any
   * subsequence within eSeq.
   *
   * @param inputSequences the sequences that were chosen as the input to the method under test for
   *     creating {@code eSeq} which is a new and unique sequence
   * @param eSeq the recently executed sequence which is new and unique
   */
  void createdExecutableSequenceFromInputs(List<Sequence> inputSequences, ExecutableSequence eSeq);
}
