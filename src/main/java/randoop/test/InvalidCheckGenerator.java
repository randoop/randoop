package randoop.test;

import randoop.sequence.ExecutableSequence;

/** Generates invalid checks. */
public class InvalidCheckGenerator extends TestCheckGenerator {

  /** Creates a InvalidCheckGenerator. */
  public InvalidCheckGenerator() {}

  @Override
  public InvalidChecks generateTestChecks(ExecutableSequence eseq) {
    return new InvalidChecks(new InvalidValueCheck(eseq, eseq.sequence.size() - 1));
  }
}
