package randoop.condition;

/**
 * Created by bjkeller on 4/10/17.
 */
public interface OtherConditionsInterface {
  /**
   * @param s s != null && !s.isEmpty()
   * @return value > s.length()
   */
  int m(String s);
}
