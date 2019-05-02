package randoop.generation;

/** Representations for outcomes after an operation is selected during generation. */
public enum OperationOutcome {
  /** A sequence was generated and output as an Error-revealing sequence. */
  ERROR_SEQUENCE,

  /** A sequence was generated and output as a regression test sequence. */
  REGRESSION_SEQUENCE,

  /** No sequence was generated because no inputs could be found. */
  NO_INPUTS_FOUND,

  /** The operation was removed from the list. */
  REMOVED,

  /** The generated sequence was discarded because it was subsumed. */
  SUBSUMED,

  /** The generated sequence was discarded for one of several reasons. */
  SEQUENCE_DISCARDED
}
