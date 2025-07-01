package randoop.generation;

import org.plumelib.util.SIList;
import randoop.sequence.Sequence;
import randoop.util.Randomness;

/** Select sequences uniformly at random. */
public class UniformRandomSequenceSelection extends InputSequenceSelector {

  /**
   * Pick randomly with uniform probability, a sequence from the candidate list.
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  @Override
  public Sequence selectInputSequence(SIList<Sequence> candidates) {
    return Randomness.randomMember(candidates);
  }
}
