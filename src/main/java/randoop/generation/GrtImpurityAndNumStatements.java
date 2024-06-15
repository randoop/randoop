package randoop.generation;

import randoop.sequence.Sequence;

/**
 * Represents the result of fuzzing a sequence of statements, and the number of additional
 * statements that were used to fuzz the inputs.
 */
class GrtImpurityAndNumStatements {

  /** The sequences that create the inputs. */
  public Sequence sequence;

  /** The number of additional statements that were executed to fuzz the inputs. */
  public int numStatements;

  /**
   * Creates a new GrtImpurityAndNumStatements object.
   *
   * @param sequence the sequence that create the inputs
   * @param numStatements the number of additional statements that were executed to fuzz the inputs
   */
  public GrtImpurityAndNumStatements(Sequence sequence, int numStatements) {
    this.sequence = sequence;
    this.numStatements = numStatements;
  }
}
