package randoop.test;

import randoop.sequence.ExecutableSequence;

/** Returns an empty TestChecks. */
public class DummyCheckGenerator implements TestCheckGenerator {

  @Override
  public TestChecks generateTestChecks(ExecutableSequence eseq) {
    return new RegressionChecks();
  }
}
