package randoop.condition;

/**
 * Created by bjkeller on 4/10/17.
 */
public class OverridingConditionsClass extends ConditionsSuperClass implements ConditionsInterface, OtherConditionsInterface {
  /**
   * @param s a string, not null
   * @return value >= 2 * s.length()
   */
  public int m(String s) {
    return s.length() * 2;
  }
}
