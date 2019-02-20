package randoop.output;

/**
 * A NameGenerator generates a sequence of names as strings in the form "prefix"+i for integer i.
 * Pads the counter with zeros to ensure a minimum number of digits determined by field digits.
 */
public class NameGenerator {

  /** The number to use for the next name to generate. */
  private int counter;
  /** The format string to generate a name; takes one integer parameter. */
  private String format;

  /**
   * Creates an instance that generates names beginning with prefix, counts starting at the
   * initialValue, and 0-padded to enough digits for {@code lastValue}.
   *
   * @param prefix a string to be used as the prefix for all generated names
   * @param initialValue integer starting value for name counter
   * @param lastValue the last expected number, to determine 0-padding; 0 for no padding
   */
  public NameGenerator(String prefix, int initialValue, int lastValue) {
    this.counter = initialValue;
    this.format =
        prefix + "%" + (lastValue == 0 ? "" : ("0" + ((int) (Math.log10(lastValue) + 1)))) + "d";
  }

  /**
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
}
