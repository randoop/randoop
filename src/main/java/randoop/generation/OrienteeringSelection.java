package randoop.generation;

import java.util.HashMap;
import java.util.Map;
import org.plumelib.util.CollectionsPlume;
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
 *
 * <p>Our implementation of Orienteering differs from that described in the GRT paper in that we do
 * not measure the time of every execution of a sequence. Instead, we assume that a sequence's
 * execution time is equal to the execution time of its first run. We believe this assumption is
 * reasonable since a sequence does not take any inputs (it is self-contained), so its execution
 * time probably does not differ greatly between separate runs.
 *
 * <p>The GRT paper also does not describe how to handle input sequences that have an execution time
 * of zero, such as one that only includes the assignment of a primitive type {@code byte byte0 =
 * (byte)1;}. We assign these input sequences an execution time of 1 nanosecond to prevent division
 * by zero when later computing weights.
 */
public class OrienteeringSelection extends InputSequenceSelector {
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
   * Map from a sequence to its execution time in nanoseconds. Once computed for a given sequence,
   * the value is never updated.
   */
  private final Map<Sequence, Long> sequenceExecutionTime = new HashMap<>();

  /** Total weight of the sequences within {@code weightMap}. */
  private double totalWeight;

  /**
   * Bias input selection towards lower-cost sequences. We first compute and update the weights of
   * all the candidates within the candidate list before making our selection.
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  @Override
  public Sequence selectInputSequence(SimpleList<Sequence> candidates) {
    computeWeightForCandidates(candidates);

    Sequence selectedSequence = Randomness.randomMemberWeighted(candidates, weightMap, totalWeight);
    CollectionsPlume.incrementMap(sequenceSelectionCount, selectedSequence);

    // Compute and update the weight of the selected sequence which will be affected by its
    // increased selection count.
    double oldWeight = weightMap.get(selectedSequence);
    double updatedWeight = computeWeightForCandidate(selectedSequence);
    weightMap.put(selectedSequence, updatedWeight);
    totalWeight = totalWeight - oldWeight + updatedWeight;

    return selectedSequence;
  }

  /**
   * Compute the weights of the candidates in the given list that have not been assigned a weight
   * and updates them in the {@code weightMap}.
   *
   * @param candidates list of candidate sequences
   */
  private void computeWeightForCandidates(SimpleList<Sequence> candidates) {
    // Iterate through the candidate list, computing the weight for a sequence only if it has
    // not yet been computed.
    for (int i = 0; i < candidates.size(); i++) {
      Sequence candidate = candidates.get(i);

      Double weight = weightMap.get(candidate);
      if (weight == null) {
        weight = computeWeightForCandidate(candidate);
        weightMap.put(candidate, weight);
        totalWeight += weight;
      }
    }
  }

  /**
   * Compute the weight of a sequence. The formula for a sequence's weight is:
   *
   * <p>1.0 / (k * seq.exec_time * sqrt(seq.meth_size))
   *
   * <p>where k is the number of selections of seq and exec_time is the execution time of seq and
   * meth_size is the number of method call statements in seq. This formula is a slight
   * simplification of the one described in the GRT paper which maintains a separate exec_time for
   * each execution of seq. However, we assume that every execution time for a sequence is the same
   * as the first execution.
   *
   * @param sequence the sequence to compute a weight for
   * @return the computed weight for the given sequence
   */
  private double computeWeightForCandidate(Sequence sequence) {
    double methodSizeSqrt = getMethodSizeSquareRootForSequence(sequence);

    Integer selectionCount = sequenceSelectionCount.get(sequence);
    // If the sequence has not been selected before, it will not have a selection count. We use
    // a selection count of 1 for the weight computation.
    if (selectionCount == null) {
      selectionCount = 1;
    }

    Long executionTime = sequenceExecutionTime.get(sequence);
    // If the sequence has not been executed before, it will not have an associated execution time.
    // Additionally, single-statement sequences can have a measured execution time of zero units.
    // For both cases, we use an execution time of 1 unit for the weight computation.
    if (executionTime == null || executionTime == 0) {
      executionTime = 1L;
    }

    return 1.0 / (selectionCount * executionTime * methodSizeSqrt);
  }

  /**
   * Retrieves the execution time of the given {@code eSeq} and associates it with the underlying
   * {@link Sequence} if an execution time has not yet been determined for this input sequence. The
   * input sequence's weight is then computed and updated in the {@code weightMap}.
   *
   * <p>We do not update the execution time of an input sequence once it has been assigned. This is
   * because we do not believe that a single input sequence's execution time will change drastically
   * between different runs. This is a simplification of GRT's description of Orienteering, which
   * does differentiate execution times of a given sequence between multiple runs.
   *
   * @param eSeq the recently executed sequence which is new and unique, and has just been executed.
   *     It contains its overall execution time for the underlying {@link Sequence}.
   */
  @Override
  public void createdExecutableSequence(ExecutableSequence eSeq) {
    Sequence inputSequence = eSeq.sequence;

    if (!sequenceExecutionTime.containsKey(inputSequence)) {
      sequenceExecutionTime.put(inputSequence, eSeq.exectime);
      // Update the weight of the input sequence.
      double weight = computeWeightForCandidate(inputSequence);
      weightMap.put(inputSequence, weight);
    }
  }

  /**
   * Retrieve the method size square root of the given sequence. This is the square root of the
   * number of method call statements within the given sequence.
   *
   * <p>To prevent division by zero, we use 1 for a sequence with no method calls.
   *
   * @param sequence the sequence whose the method size square root to get
   * @return square root of the number of method calls in the given sequence
   */
  private double getMethodSizeSquareRootForSequence(Sequence sequence) {
    // If we haven't computed the square root of the method size of this sequence,
    // compute it and permanently store it.
    Double methodSizeSqrt = sequenceMethodSizeSqrt.get(sequence);
    if (methodSizeSqrt == null) {
      methodSizeSqrt = Math.sqrt(sequence.numMethodCalls());
      if (methodSizeSqrt == 0) {
        methodSizeSqrt = 1.0;
      }
      sequenceMethodSizeSqrt.put(sequence, methodSizeSqrt);
    }

    return methodSizeSqrt;
  }
}
