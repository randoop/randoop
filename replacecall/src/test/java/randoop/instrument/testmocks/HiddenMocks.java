package randoop.instrument.testmocks;

/** A non-public class of replacement methods, for testing the replacement file reader. */
final class HiddenMocks {

  /** Do not instantiate. */
  private HiddenMocks() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * A replacement for {@code Math.abs(int)} in a non-public class.
   *
   * @param x an int
   * @return {@code x}
   */
  @SuppressWarnings("PMD.PublicMemberInNonPublicType") // tests a public member of a non-public type
  public static int abs(int x) {
    return x;
  }
}
