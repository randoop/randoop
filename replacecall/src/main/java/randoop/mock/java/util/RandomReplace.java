package randoop.mock.java.util;

public class RandomReplace {

  private RandomReplace() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  // Used to replace calls to the constructor Random().
  public static java.util.Random randomWithSeedZero() {
    return new java.util.Random(0);
  }
}
