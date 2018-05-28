package randoop.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.util.Randomness;
import randoop.util.SimpleList;

/**
 * Implements the Orienteering component, as described by the paper "GRT: Program-Analysis-Guided
 * Random Testing" by Ma et. al (appears in ASE 2015):
 * https://people.kth.se/~artho/papers/lei-ase2015.pdf .
 *
 * <p>Biases input selection towards sequences that have lower execution cost. Execution cost is
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
   * Bias input selection towards lower cost sequences. We first compute and update the weights of
   * all the candidates within the candidate list before making our selection.
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  @Override
  public Sequence selectInputSequence(SimpleList<Sequence> candidates) {
    updateWeightMapForCandidates(candidates);

    Sequence selectedSequence = Randomness.randomMemberWeighted(candidates, weightMap);
    incrementCountInMap(sequenceSelectionCount, selectedSequence);
    return selectedSequence;
  }

  /**
   * Update the weights of the candidates in the given list. If an input sequence has not been
   * selected or executed before, it will be assigned its default weight, computed by {@code
   * Sequence.getSize()}. The formula for updating a sequence's weight if selection count and
   * execution time information are both available is
   *
   * <p>1.0 / (k * seq.exec_time * sqrt(seq.meth_size))
   *
   * <p>Where k is the number of selections of seq and exec_time is the execution time of seq and
   * meth_size is the number of method call statements in seq. This formula is a slight
   * simplification of the one described in the GRT paper which maintains a separate exec_time for
   * each execution of seq. However, we assume that the execution times of a sequence are the same
   * as the first execution.
   *
   * @param candidates list of candidate sequences
   */
  private void updateWeightMapForCandidates(SimpleList<Sequence> candidates) {
    for (int i = 0; i < candidates.size(); i++) {
      Sequence candidate = candidates.get(i);

      double methodSizeSqrt = getMethodSizeSquareRootForSequence(candidate);
      Integer selectionCount = sequenceSelectionCount.get(candidate);
      Long executionTime = sequenceExecutionTime.get(candidate);

      // Recompute and update this sequence's weight.
      if (selectionCount != null && executionTime != null) {
        weightMap.put(candidate, 1.0 / (selectionCount * executionTime * methodSizeSqrt));
      } else {
        weightMap.put(candidate, candidate.getWeight());
      }
    }
  }

  /**
   * Each {@link Sequence} within {@code inputSequences} is a subsequence of {@code eSeq}. At this
   * point, the given {@link ExecutableSequence} has been executed and contains the execution time
   * of the sequence as a whole. Since we are not able to measure the execution time of each input
   * sequence, we make the simplifying assumption that each input sequence's execution is equal to
   * that of the entire {@link ExecutableSequence}. Furthermore, we do not update the execution time
   * of an input sequence once it has been assigned. This is because we expect newer {@link
   * ExecutableSequence}s to be longer in length as we extend existing sequences. Thus, the first
   * measured execution time should be a closer approximation of the input sequence's execution
   * time.
   *
   * @param inputSequences the sequences that were chosen as the input to the method under test for
   *     creating a new and unique sequence
   * @param eSeq the recently executed, new sequence
   */
  @Override
  public void assignExecTimeForInputSequences(
      Set<Sequence> inputSequences, ExecutableSequence eSeq) {
    for (Sequence inputSequence : inputSequences) {
      Long executionTime = sequenceExecutionTime.get(inputSequence);
      if (executionTime == null) {
        sequenceExecutionTime.put(inputSequence, eSeq.exectime);
      }
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

  /**
   * Retrieve the method size square root of the given sequence. This is the square root of the
   * number of method call statements within the given sequence.
   *
   * @param sequence the sequence to get the method size square root of
   * @return square root of the number of method calls in the given sequence
   */
  private double getMethodSizeSquareRootForSequence(Sequence sequence) {
    // If we haven't computed the square root of the method size of this sequence,
    // compute it and permanently store it.
    Double methodSizeSqrt = sequenceMethodSizeSqrt.get(sequence);
    if (methodSizeSqrt == null) {
      methodSizeSqrt = Math.sqrt(sequence.numMethodCalls());
      sequenceMethodSizeSqrt.put(sequence, methodSizeSqrt);
    }

    return methodSizeSqrt;
  }
}
