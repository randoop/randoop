package randoop.generation;

import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.util.Randomness;
import randoop.util.SimpleList;

import java.util.HashMap;
import java.util.Map;

public class OrienteeringSelection implements InputSequenceSelector {

    /**
     * Contains details about a sequence's execution including the number of times the sequence
     * was executed and information about its size and execution time.
     */
    private static class OrienteeringSeqExecDet {
        public int numSelections;
        public double sumOfExecTimeAndMethodSizeProduct;

        public OrienteeringSeqExecDet(int numSelections, double sumOfExecTimeAndMethodSizeProduct) {
            this.numSelections = numSelections;
            this.sumOfExecTimeAndMethodSizeProduct = sumOfExecTimeAndMethodSizeProduct;
        }
    }

    /**
     * Map from a sequence to its execution details including number of times executed,
     * execution time and number of methods.
     */
    private final Map<Sequence, OrienteeringSeqExecDet> sequenceExecutionCount = new HashMap<>();

    @Override
    public Sequence selectInputSequence(SimpleList<Sequence> candidates) {
        return Randomness.randomMemberWeighted(candidates, weightMap);
    }

    /**
     * Computes eSeq's weight for weightMap according to the Orienteering formula from the GRT paper.
     * @param eSeq the recently executed sequence
     */
    @Override
    public void computeWeightForSequence(ExecutableSequence eSeq) {
        OrienteeringSeqExecDet seqExecDets = sequenceExecutionCount.get(eSeq.sequence);
        if (seqExecDets == null) {
            seqExecDets = new OrienteeringSeqExecDet(0, 0);
            sequenceExecutionCount.put(eSeq.sequence, seqExecDets);
        }

        // Increment the number of times this sequence has been selected.
        // We treat the number of selections as the same as the number of executions since a sequence
        // must have been selected if it was executed.
        seqExecDets.numSelections += 1;
        seqExecDets.sumOfExecTimeAndMethodSizeProduct +=
                eSeq.exectime * Math.sqrt(eSeq.sequence.methodCalls());

        double weight = 1.0 / (seqExecDets.sumOfExecTimeAndMethodSizeProduct);
        weightMap.put(eSeq.sequence, weight);
    }
}
