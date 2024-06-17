package randoop.generation;

import randoop.sequence.Sequence;

/**
 * Represents the result of fuzzing a sequence of statements, and the number of additional
 * statements that were used to fuzz the inputs.
 */
class GrtImpurityAndNumStatements {

  /** The sequence that create the inputs. */
  public Sequence sequence;

  /**
   * The number of additional statements that were executed to fuzz the inputs. These fuzzing
   * statements are the last `numStatements` statements in the sequence. If `numStatements` is 0,
   * then the sequence does not include any fuzzing statements.
   */
  public int numStatements;

  /**
   * Creates a new GrtImpurityAndNumStatements object.
   *
   * @param sequence the sequence that create the inputs.
   * @param numStatements the number of additional statements that were executed to fuzz the inputs
   */
  public GrtImpurityAndNumStatements(Sequence sequence, int numStatements) {
    this.sequence = sequence;
    this.numStatements = numStatements;
  }
}
