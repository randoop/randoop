package randoop.condition;

/**
 * Input for tests of inherited conditions.
 * @see OverridingConditionsClass
 */
public class ConditionsSuperClass extends ConditionsSuperSuperClass {
  public int m(String s) {
    return s.length();
  }
}
