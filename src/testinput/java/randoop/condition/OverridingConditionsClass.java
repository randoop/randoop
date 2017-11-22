package randoop.condition;

/**
 * Input for tests of inherited specifications.
 * {@link ConditionsSuperClass#m(String)} returns length of string.
 * Intended to be used with different sets of specifications
 */
public class OverridingConditionsClass extends ConditionsSuperClass implements ConditionsInterface, OtherConditionsInterface {
  public int m(String s) {
    return s.length();
  }
}
