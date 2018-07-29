package randoop.generation;

import java.util.List;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.util.SimpleList;

/** Interface for selecting sequences as input for creating new sequences. */
public abstract class InputSequenceSelector {
  /**
   * Choose a sequence used as input for creating a new sequence.
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  public abstract Sequence selectInputSequence(SimpleList<Sequence> candidates);

  /**
   * A hook that is called after a new sequence has been created and executed.
   *
   * <p>The default implementation does nothing. Subclasses may override it to add behavior.
   *
   * @param inputSequences the sequences that were chosen as the input to the method under test for
   *     creating {@code eSeq}; each one is a subsequence of {@code eSeq}
   * @param eSeq the recently executed sequence which is new and unique, and has just been executed.
   *     It contains its overall execution time. It also contains, for each statement, an
   *     ExecutionOutcome that gives the statement's execution time.
   */
  public void createdExecutableSequenceFromInputs(
      List<Sequence> inputSequences, ExecutableSequence eSeq) {}
}
