package randoop.generation;

import java.util.HashMap;
import java.util.Map;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.util.Randomness;
import randoop.util.SimpleList;

/**
 * Biases input selection towards sequences that have lower execution cost. Execution cost is
 * measured by the number of method calls in a sequence and the time it takes to execute.
 */
public class OrienteeringSelection implements InputSequenceSelector {
  /** Map from a sequence to its weight. */
  private final Map<Sequence, Double> weightMap = new HashMap<>();

  /** Map from a sequence to the number of times it was selected. */
  private final Map<Sequence, Integer> sequenceSelectionCount = new HashMap<>();

  /**
   * Cache for the square root of the number of method calls in a sequence of statements. Once
   * computed for a given sequence, the value is never updated.
   */
  private final Map<Sequence, Double> sequenceMethodSizeSqrt = new HashMap<>();

  /**
   * Map from a sequence to its approximate execution time in milliseconds. Once computed for a
   * given sequence, the value is never updated.
   */
  private final Map<Sequence, Long> sequenceExecutionTime = new HashMap<>();

  /**
   * The sequence that was last selected by Orienteering. A reference is kept so that we can update
   * its execution time once {@link ForwardGenerator} finishes executing the newly constructed
   * sequence.
   */
  private Sequence lastSelectedSequence;

  /**
   * Bias input selection towards lower cost sequences.
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  @Override
  public Sequence selectInputSequence(SimpleList<Sequence> candidates) {
    updateWeightMapForCandidates(candidates);
    lastSelectedSequence = Randomness.randomMemberWeighted(candidates, weightMap);
    incrementCountInMap(sequenceSelectionCount, lastSelectedSequence);
    return lastSelectedSequence;
  }

  /**
   * Update the weights of the candidates in the given list
   *
   * @param candidates list of candidates
   */
  private void updateWeightMapForCandidates(SimpleList<Sequence> candidates) {
    for (int i = 0; i < candidates.size(); i++) {
      Sequence candidate = candidates.get(i);

      // If we haven't computed the square root of the method size of this sequence,
      // compute it and permanently store it.
      Double methodSizeSqrt = sequenceMethodSizeSqrt.get(candidate);
      if (methodSizeSqrt == null) {
        methodSizeSqrt = Math.sqrt(candidate.numMethodCalls());
        sequenceMethodSizeSqrt.put(candidate, methodSizeSqrt);
      }

      // If this sequence has not been selected yet, don't compute its weight and
      // use its default value.
      Integer selectionCount = sequenceSelectionCount.get(candidate);
      if (candidate == null) {
        continue;
      }

      // If this sequence has not been executed yet, don't compute its weight and
      // use its default value.
      Long executionTime = sequenceExecutionTime.get(candidate);
      if (executionTime == null) {
        continue;
      }

      // Recompute and update this sequence's weight.
      weightMap.put(candidate, 1.0 / (selectionCount * executionTime * methodSizeSqrt));
    }
  }

  /**
   * Assign the last selected sequence's execution time to be that of this recently executed
   * executable sequence.
   *
   * @param eSeq the recently executed sequence
   */
  @Override
  public void assignExecTimeForSequence(ExecutableSequence eSeq) {
    Long executionTime = sequenceExecutionTime.get(lastSelectedSequence);
    if (executionTime == null) {
      sequenceExecutionTime.put(lastSelectedSequence, eSeq.exectime);
    }
  }

  /**
   * Increments the value mapped to by the key in the given map. If the map does not contain the
   * value, the value is set to 1 in the map.
   *
   * @param map input map
   * @param key given key
   * @param <K> any reference type
   */
  private <K> void incrementCountInMap(Map<K, Integer> map, K key) {
    Integer count = map.get(key);
    if (count == null) {
      count = 0;
    }
    map.put(key, count + 1);
  }
}
