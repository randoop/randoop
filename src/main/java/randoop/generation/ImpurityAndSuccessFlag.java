package randoop.generation;

import randoop.sequence.Sequence;

/**
 * Represents a set of inputs, plus a boolean that is true if this is a good set of inputs.
 *
 * <p>This is the return type for Impurity's public {@code fuzz} method, which is
 * responsible for xxxxxxxxxxxxx
 */
class ImpurityAndSuccessFlag {

    /**
     * True if private method {@code Impurity.fuzz(Sequence chosenSeq)} was able to
     * find xxxxxxxxxx
     */
    public boolean success;

    /** The sequences that create the inputs. */
    public Sequence sequence;

    /** The number of additional statements that were executed to fuzz the inputs. */
    public int numStatements;


    /**
     * Creates a new ImpurityAndSuccessFlag.
     *
     * @param success true if there are component sequences for all the input types
     * @param sequence the sequence that create the inputs
     */
    public ImpurityAndSuccessFlag(boolean success, Sequence sequence, int numStatements) {
        this.success = success;
        this.sequence = sequence;
        this.numStatements = numStatements;
    }
}
