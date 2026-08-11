package randoop.test;

import randoop.condition.RandoopSpecificationError;
import randoop.sequence.ExecutableSequence;

/** Returns an empty TestChecks. */
public class DummyCheckGenerator extends TestCheckGenerator {

  /** Creates a DummyCheckGenerator. */
  public DummyCheckGenerator() {}

  @Override
  public TestChecks<?> generateTestChecks(ExecutableSequence eseq)
      throws RandoopSpecificationError {
    return RegressionChecks.EMPTY;
  }
}
