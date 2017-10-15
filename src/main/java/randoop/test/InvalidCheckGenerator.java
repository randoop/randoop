package randoop.test;

import randoop.sequence.ExecutableSequence;

/** Generates invalid checks. */
public class InvalidCheckGenerator implements TestCheckGenerator {
  @Override
  public TestChecks visit(ExecutableSequence eseq) {
    TestChecks checks = new InvalidChecks();
    checks.add(new InvalidValueCheck(eseq, eseq.sequence.size() - 1));
    return checks;
  }

  @Override
  public TestCheckGenerator getGenerator() {
    return this;
  }
}
