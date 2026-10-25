package randoop.test;

import randoop.condition.RandoopSpecificationError;
import randoop.sequence.ExecutableSequence;

/** Generates invalid checks. */
public class InvalidCheckGenerator extends TestCheckGenerator {

  /** Creates an InvalidCheckGenerator. */
  public InvalidCheckGenerator() {}

  @Override
  public InvalidChecks generateTestChecks(ExecutableSequence eseq)
      throws RandoopSpecificationError {
    return new InvalidChecks(new InvalidValueCheck(eseq, eseq.sequence.size() - 1));
  }
}
