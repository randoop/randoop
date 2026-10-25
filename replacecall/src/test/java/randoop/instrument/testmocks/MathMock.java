package randoop.instrument.testmocks;

/** A replacement class for {@code java.lang.Math} whose method has the wrong return type. */
public final class MathMock {

  /** Do not instantiate. */
  private MathMock() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * A replacement for {@code Math.abs(int)} that has the wrong return type.
   *
   * @param x an int
   */
  public static void abs(int x) {
    // Do nothing.
  }
}
