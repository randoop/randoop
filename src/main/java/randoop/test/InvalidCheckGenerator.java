package randoop.test;

import randoop.sequence.ExecutableSequence;

/** Generates invalid checks. */
public class InvalidCheckGenerator implements TestCheckGenerator {
  @Override
  public TestChecks visit(ExecutableSequence s) {
    TestChecks checks = new InvalidChecks();
    checks.add(new InvalidValueCheck(s, s.sequence.size() - 1));
    return checks;
  }
}
