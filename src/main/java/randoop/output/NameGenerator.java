package randoop.output;

/**
 * A NameGenerator generates a sequence of names as strings in the form "prefix"+i for integer i.
 * Pads the counter with zeros to ensure a minimum number of digits determined by field digits.
 */
public class NameGenerator {

  private int initialValue;
  private int counter;
  private String format;

  /*
   * Creates an instance that generates names beginning with prefix, count
   * starting at the initialValue, and 0-padded to digits digits.
   *
   * @param prefix a string to be used as the prefix for all generated names
   *
   * @param initialValue integer starting value for name counter
   *
   * @param digits the minimum number of digits (determines 0-padding)
   */
  NameGenerator(String prefix, int initialValue, int digits) {
    this.initialValue = initialValue;
    this.counter = initialValue;

    this.format = prefix + formatString(digits);
  }

  /*
   * Generates names without 0-padding on counter.
   *
   * @param prefix is a string to be used as a prefix for all names generated
   */
  public NameGenerator(String prefix) {
    this(prefix, 0, 0);
  }

  public String next() {
    String name = String.format(format, counter);
    counter++;
    return name;
  }

  int nameCount() {
    return counter - initialValue;
  }

  /**
   * Returns the number of digits in the printed representation of the argument.
   *
   * @param n the number
   * @return the number of digits in string form of given number
   */
  public static int numDigits(int n) {
    return (int) Math.log10(n) + 1;
  }

  public static String formatString(int numDigits) {
    if (numDigits > 0) {
      return "%0" + numDigits + "d";
    }
    return "%d";
  }
}
