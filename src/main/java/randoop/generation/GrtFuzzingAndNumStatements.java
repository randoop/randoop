package randoop.generation;

import randoop.sequence.Sequence;

/**
 * Represents the result of fuzzing a sequence, and the number of additional statements that were
 * used to fuzz the inputs. See {@link randoop.generation.GrtFuzzing}.
 */
class GrtFuzzingAndNumStatements {

  /** The sequence that creates the inputs for method under test, often with fuzzing statements. */
  public Sequence sequence;

  /**
   * The number of additional statements that were executed to fuzz the inputs. These fuzzing
   * statements are the last {@code numStatements} statements in the sequence. If {@code
   * numStatements} is 0, then the sequence does not include any fuzzing statements.
   */
  public int numStatements;

  /**
   * Creates a new GrtFuzzingAndNumStatements object.
   *
   * @param sequence the sequence that creates and fuzzes the inputs
   * @param numStatements the number of additional statements that were executed to fuzz the inputs
   */
  public GrtFuzzingAndNumStatements(Sequence sequence, int numStatements) {
    this.sequence = sequence;
    this.numStatements = numStatements;
  }
}
