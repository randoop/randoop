package randoop.generation;

import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.util.Randomness;
import randoop.util.SimpleList;

public class SmallTestsSequenceSelection implements InputSequenceSelector {

    /**
     * Pick a sequence from the candidate list using the member's natural weight.
     * @param candidates sequences to choose from
     * @return the chosen sequence
     */
    @Override
    public Sequence selectInputSequence(SimpleList<Sequence> candidates) {
        return Randomness.randomMemberWeighted(candidates);
    }

    /**
     * Unused by this class.
     * @param eSeq the recently executed sequence
     */
    @Override
    public void computeWeightForSequence(ExecutableSequence eSeq) {

    }
}
