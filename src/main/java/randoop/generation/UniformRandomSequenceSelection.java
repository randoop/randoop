package randoop.generation;

import java.util.Set;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.util.Randomness;
import randoop.util.SimpleList;

public class UniformRandomSequenceSelection implements InputSequenceSelector {

  /**
   * Pick randomly with uniform probability, a sequence from the candidate list
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  @Override
  public Sequence selectInputSequence(SimpleList<Sequence> candidates) {
    return Randomness.randomMember(candidates);
  }

  /**
   * Unused by this class.
   *
   * @param inputSequences the sequences that were chosen as the input to the method under test for
   *     creating a new and unique sequence
   * @param eSeq the recently executed sequence
   */
  @Override
  public void assignExecTimeForInputSequences(
      Set<Sequence> inputSequences, ExecutableSequence eSeq) {}
}
