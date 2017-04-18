package randoop.test;

import randoop.sequence.ExecutableSequence;

/** Generates invalid checks. */
public class InvalidCheckGenerator implements TestCheckGenerator {
  @Override
  public TestChecks visit(ExecutableSequence s) {
    // this is a hack to ensure that if get here from a throws-condition that counted as param
    s.conditionType = ExecutableSequence.ConditionType.PARAM;
    TestChecks checks = new InvalidChecks();
    checks.add(new InvalidValueCheck(s, s.sequence.size() - 1));
    return checks;
  }

  @Override
  public TestCheckGenerator getGenerator() {
    return this;
  }
}
