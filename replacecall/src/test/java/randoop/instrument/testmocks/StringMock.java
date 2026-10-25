package randoop.instrument.testmocks;

/** A replacement class for {@code java.lang.String} whose method lacks the receiver parameter. */
public final class StringMock {

  /** Do not instantiate. */
  private StringMock() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * A replacement for {@code String.length()} that lacks the receiver parameter.
   *
   * @return zero
   */
  public static int length() {
    return 0;
  }
}
