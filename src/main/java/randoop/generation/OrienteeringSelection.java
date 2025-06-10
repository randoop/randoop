package randoop.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.util.Randomness;
import randoop.util.list.SimpleList;

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
 * by zero when computing weights.
 *
 * <p>The GRT paper also does not describe how to handle input sequences that have not yet been
 * selected. We start ecah input sequences with a selection count of 1 to prevent division by zero
 * when computing weights.
 */
public class OrienteeringSelection extends InputSequenceSelector {
  /** Map from a sequence to its details used for computing its weight. */
  private final Map<Sequence, SequenceDetails> sequenceDetailsMap = new HashMap<>();

  /**
   * Map from a sequence to its weight. For every sequence s, {@code weightMap.get(s) ==
   * sequenceDetailsMap.get(s).getWeight()}. This is needed because {@code
   * Randomneess#randomMemberWeighted} takes a {@code Map<T, Double>} as an argument.
   */
  private final Map<Sequence, Double> weightMap = new HashMap<>();

  /** Information used by Orienteering to compute a weight for a sequence. */
  private static class SequenceDetails {
    /** The square root of the number of method calls in the sequence. */
    private final double methodSizeSqrt;

    /** The execution time of the sequence, in nanoseconds. */
    private final long executionTimeNanos;

    /** Number of times this sequence has been selected by {@link OrienteeringSelection}. */
    private int selectionCount;

    /**
     * A {@link Sequence}'s weight. This is computed from the other fields and is updated when
     * {@link #selectionCount} is.
     */
    private double weight;

    /**
     * Create a SequenceDetails for the given sequence, but using the given execution time.
     *
     * @param sequence a sequence
     * @param executionTimeNanos execution time in nanoseconds
     */
    SequenceDetails(Sequence sequence, long executionTimeNanos) {
      this(methodSizeSquareRoot(sequence), executionTimeNanos);
    }

    /**
     * Create a SequenceDetails.
     *
     * @param methodSizeSqrt the square root of the number of method calls
     * @param executionTimeNanos the execution time, in nanoseconds
     */
    public SequenceDetails(double methodSizeSqrt, long executionTimeNanos) {
      this.methodSizeSqrt = methodSizeSqrt;
      this.executionTimeNanos = executionTimeNanos;
      // Prevent division by zero: start the count at 1.
      this.selectionCount = 1;
      updateWeight();
    }

    /** Increments the selection count. */
    public void incrementSelectionCount() {
      selectionCount++;
      updateWeight();
    }

    /**
     * Returns the weight of the sequence.
     *
     * @return the weight of the sequence
     */
    public double getWeight() {
      return weight;
    }

    /**
     * Compute the weight of a sequence. The formula for a sequence's weight is:
     *
     * <p>1.0 / (k * seq.exec_time * sqrt(seq.meth_size))
     *
     * <p>where k is the number of selections of seq and exec_time is the execution time of seq and
     * meth_size is the number of method call statements in seq. This formula is a slight
     * simplification of the one described in the GRT paper which maintains a separate exec_time for
     * each execution of seq. However, we assume that every execution time for a sequence is the
     * same as the first execution.
     */
    private void updateWeight(@UnknownInitialization SequenceDetails this) {
      weight = 1.0 / (selectionCount * executionTimeNanos * methodSizeSqrt);
    }
  }

  /**
   * Initialize {@link OrienteeringSelection} and assign a weight to each {@link Sequence} within
   * the given set of seed sequences. This ensures that later, Orienteering will always have a
   * corresponding {@link SequenceDetails} and therefore a corresponding weight for every {@link
   * Sequence} within a list of candidates for selection.
   *
   * @param seedSequences set of seed sequences
   */
  public OrienteeringSelection(Set<Sequence> seedSequences) {
    for (Sequence seedSequence : seedSequences) {
      // Treat every seed sequence as having an execution time of 1 nanosecond.
      createSequenceDetailsWithExecutionTime(seedSequence, 1L);
    }
  }

  /**
   * Bias input selection towards lower-cost sequences.
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  @Override
  public Sequence selectInputSequence(SimpleList<Sequence> candidates) {
    // One could imagine caching the total weight to avoid this iteration over the list.  However,
    // this candidate list is provided by the client, there are many such lists, and we don't want
    // to inject code into all the places that the list may be computed.  Also, there might be
    // issues with floating-point precision from repeatedly updating a running total for each list.
    double totalWeight = computeTotalWeightForCandidates(candidates);

    Sequence selectedSequence = Randomness.randomMemberWeighted(candidates, weightMap, totalWeight);

    // Compute and update the weight of the selected sequence which will be affected by its
    // increased selection count.
    SequenceDetails sequenceDetails = sequenceDetailsMap.get(selectedSequence);
    sequenceDetails.incrementSelectionCount();
    weightMap.put(selectedSequence, sequenceDetails.getWeight());

    return selectedSequence;
  }

  /**
   * Compute the total weight of the list of candidate {@link Sequence}s.
   *
   * @param candidates list of candidate sequences
   * @return the total weight of the input candidate list
   */
  private double computeTotalWeightForCandidates(SimpleList<Sequence> candidates) {
    double totalWeight = 0;
    for (int i = 0; i < candidates.size(); i++) {
      Sequence candidate = candidates.get(i);
      SequenceDetails details = sequenceDetailsMap.get(candidate);
      if (details == null) {
        // This might be a literal that was created by ComponentManager.getSequencesForType().
        createdExecutableSequence(new ExecutableSequence(candidate));
        details = sequenceDetailsMap.get(candidate);
      }
      totalWeight += details.getWeight();
    }
    return totalWeight;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation creates and stores a {@link SequenceDetails} for the underlying {@link
   * Sequence} in the given {@link ExecutableSequence}.
   *
   * @param eSeq the recently executed sequence which is new and unique, and has just been executed.
   *     It contains its overall execution time for the underlying {@link Sequence}.
   */
  @Override
  public void createdExecutableSequence(ExecutableSequence eSeq) {
    // For sequences with negligible run times
    if (eSeq.exectime <= 0) {
      eSeq.exectime = 1;
    }
    createSequenceDetailsWithExecutionTime(eSeq.sequence, eSeq.exectime);
  }

  /**
   * Creates and stores a {@link SequenceDetails} for the given {@link Sequence} with the
   * corresponding execution time.
   *
   * @param sequence the sequence to add
   * @param executionTimeNanos the execution time of the sequence, in nanoseconds
   */
  private void createSequenceDetailsWithExecutionTime(Sequence sequence, long executionTimeNanos) {
    SequenceDetails sequenceDetails = new SequenceDetails(sequence, executionTimeNanos);

    sequenceDetailsMap.put(sequence, sequenceDetails);
    weightMap.put(sequence, sequenceDetails.getWeight());
  }

  /**
   * Returns the the square root of the number of method call statements within the given sequence.
   *
   * <p>To prevent division by zero, we use 1 for a sequence with no method calls.
   *
   * @param sequence a sequence
   * @return square root of the number of method calls in the given sequence
   */
  private static double methodSizeSquareRoot(Sequence sequence) {
    int methodSize = sequence.numMethodCalls();
    if (methodSize == 0) {
      return 1.0;
    } else {
      return Math.sqrt(methodSize);
    }
  }
}
