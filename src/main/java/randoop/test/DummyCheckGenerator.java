package randoop.test;

import randoop.sequence.ExecutableSequence;

/** Returns an empty TestChecks. */
public class DummyCheckGenerator extends TestCheckGenerator {

  @Override
  public TestChecks<?> generateTestChecks(ExecutableSequence eseq) {
    return RegressionChecks.EMPTY;
  }
}
