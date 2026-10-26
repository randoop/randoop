package randoop.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.SIList;
import randoop.sequence.Sequence;
import randoop.util.Randomness;

/**
 * Select input sequences, favoring shorter sequences. This makes Randoop produce smaller JUnit
 * tests.
 */
public class SmallTestsSequenceSelection extends InputSequenceSelector {

  /** Map from a sequence to its weight. */
  private final Map<Sequence, Double> weightMap = new HashMap<>();

  /** Creates a SmallTestsSequenceSelection. */
  public SmallTestsSequenceSelection() {}

  /**
   * Pick a sequence from the candidate list using a weighting that favors shorter sequences.
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  @Override
  public Sequence selectInputSequence(SIList<Sequence> candidates) {
    double totalWeight = updateWeightMapForCandidates(candidates);
    return Randomness.randomMemberWeighted(candidates, weightMap, totalWeight);
  }

  /**
   * Update the weight for any sequence not currently in the map, and compute the total weight.
   *
   * @param candidates the elements to compute a weight for
   * @return the total weight of all the candidates
   */
  private double updateWeightMapForCandidates(SIList<Sequence> candidates) {

    double totalWeight = 0.0;
    for (int i = 0; i < candidates.size(); i++) {
      Sequence candidate = candidates.get(i);
      double weight = weightMap.computeIfAbsent(candidate, (Sequence c) -> 1 / (double) c.size());
      totalWeight += weight;
    }
    return totalWeight;
  }

  @Override
  public void componentsCleared(Set<Sequence> retainedSequences) {
    weightMap.keySet().retainAll(retainedSequences);
  }

  /**
   * Returns the weight of the given sequence, or null if this has not computed it.
   *
   * @param sequence a sequence
   * @return the weight of the sequence, or null
   */
  @Nullable Double getWeight(Sequence sequence) {
    return weightMap.get(sequence);
  }
}
