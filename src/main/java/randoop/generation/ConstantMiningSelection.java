package randoop.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.util.Randomness;
import randoop.util.SimpleList;

/**
 * Implements the Constant Mining component, as described by the paper "GRT: Program-Analysis-Guided
 * Random Testing" by Ma et. al (appears in ASE 2015):
 * https://people.kth.se/~artho/papers/lei-ase2015.pdf .
 */
public class ConstantMiningSelection implements InputSequenceSelector {
  /**
   * Map of extracted literal sequences to their static weights. These weights are never changed
   * once initialized.
   */
  private final Map<Sequence, Double> literalWeightMap = new HashMap<>();

  /**
   * Initialize constant mining selection by computing weights for literals that appear in classes
   * under test.
   *
   * @param componentManager reference to component generator from {@link ForwardGenerator} used for
   *     getting the frequency of a literal
   * @param numClasses number of classes under tests
   * @param literalTermFrequencies a map from a literal to the number of times it appears in any
   *     class under test
   */
  public ConstantMiningSelection(
      ComponentManager componentManager,
      int numClasses,
      Map<Sequence, Integer> literalTermFrequencies) {
    if (literalTermFrequencies != null) {
      for (Sequence sequence : componentManager.getSequenceFrequency().keySet()) {
        Integer documentFrequency = componentManager.getSequenceFrequency().get(sequence);
        double tfIdf =
            literalTermFrequencies.get(sequence)
                * Math.log((numClasses + 1.0) / ((numClasses + 1.0) - documentFrequency));
        literalWeightMap.put(sequence, tfIdf);
      }
    }
  }

  /**
   * Pick a sequence from the candidate list using the member's natural weight.
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  @Override
  public Sequence selectInputSequence(SimpleList<Sequence> candidates) {
    return Randomness.randomMemberWeighted(candidates, literalWeightMap);
  }

  /**
   * Unused by this class.
   *
   * @param inputSequences the sequences that were chosen as the input to the method under test for
   *     creating {@code eSeq} which is a new and unique sequence
   * @param eSeq the recently executed sequence which is new and unique
   */
  @Override
  public void createdExecutableSequenceFromInputs(
      List<Sequence> inputSequences, ExecutableSequence eSeq) {}
}
