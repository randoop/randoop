package randoop.generation;

import randoop.sequence.Sequence;

/**
 * Represents a set of inputs.
 *
 * <p>This is the return type for Impurity's public {@code fuzz} method, which is
 * responsible for xxxxxxxxxxxxx
 */
class ImpurityAndNumStatements {

    /** The sequences that create the inputs. */
    public Sequence sequence;

    /** The number of additional statements that were executed to fuzz the inputs. */
    public int numStatements;


    /**
     * Creates a new ImpurityAndSuccessFlag.
     *
     * @param sequence true if there are component sequences for all the input types
     * @param numStatements the sequence that create the inputs
     */
    public ImpurityAndNumStatements(Sequence sequence, int numStatements) {
        this.sequence = sequence;
        this.numStatements = numStatements;
    }
}
