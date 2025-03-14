package randoop.condition;

/**
 * An input class for checking that parameter types are handled correctly between Toradocu and Randoop.
 * (None of the methods do what they say they do.)
 */
public class ParameterCheckInput {

  public enum StatusType {OK, BAD, UNDEFINED}

  public ParameterCheckInput() {
  }

  /**
   * @param m a positive integer
   * @return the absolute value of {@code m}
   */
  public int abs(int m) {
    return m;
  }

  /**
   * @param a an array of positive integers, not null
   * @return a non-null array with 0 as elements
   */
  public int[] zero(int[] a) {
    return a;
  }

  /**
   * @param s a non-empty string, not null
   * @return the string in uppercase
   */
  public String toUpperCase(String s) {
    return s;
  }

  /**
   * @param a an array of non-empty strings, not null
   * @return an array with the elements of a converted to uppercase
   */
  public String[] convert(String[] a) { return a; }

  /**
   * @param status the status != UNDEFINED
   * @return the confirmed status != UNDEFINED
   */
  public StatusType check(StatusType status) {
    return status;
  }

}
