package randoop.test;

/**
 * Test for Randoop's {@link randoop.main.GenInputsAbstract#string_maxlen} option.
 */
public class LongString {

  // No regression assertions should be created for this method.
  public static String tooLongString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < 100000; i++) {
      b.append("a");
    }
    return b.toString();
  }

  // Regression assertions should created for this method.
  public static String okString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      b.append("a");
    }
    return b.toString();
  }
}
