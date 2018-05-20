package randoop.generation;

import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.util.SimpleList;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for selecting sequences as input for creating new sequences.
 */
public interface InputSequenceSelector {
    /**
     * Map from a sequences to its weight.
     */
    Map<Sequence, Double> weightMap = new HashMap<>();

    /**
     * Choose a sequence used as input for creating a new sequence.
     * @param candidates sequences to choose from
     * @return the chosen sequence
     */
    Sequence selectInputSequence(SimpleList<Sequence> candidates);

    /**
     * Computes eSeq's weight for weightMap.
     *
     * @param eSeq the recently executed sequence
     */
    void computeWeightForSequence(ExecutableSequence eSeq);
}
