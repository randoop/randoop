package randoop.instrument.testmocks;

/** Replacement methods that are not valid replacements, for testing the replacement file reader. */
public final class MethodMocks {

  /** Do not instantiate. */
  private MethodMocks() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * A private replacement for {@code Math.abs(int)}.
   *
   * @param x an int
   * @return {@code x}
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used by replacement files
  private static int abs(int x) {
    return x;
  }

  /**
   * A replacement for {@code Object()} that has the wrong return type.
   *
   * @return the empty string
   */
  public static String newObject() {
    return "";
  }
}
