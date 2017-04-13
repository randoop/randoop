package randoop.condition;

/**
 * Input for tests of inherited conditions.
 * @see OverridingConditionsClass
 */
public interface OtherConditionsInterface {
  /**
   * @param s s != null && !s.isEmpty()
   * @return value > s.length()
   */
  int m(String s);
}
