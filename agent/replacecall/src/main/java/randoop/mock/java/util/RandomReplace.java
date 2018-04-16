package randoop.mock.java.util;

public class RandomReplace {

  // I believe that mock classes should never be instantiated.
  // This should be checked, and then probably documented in the Randoop manual.
  private RandomReplace() {
    throw new Error("Do not instantiate");
  }

  // Used to replace calls to the constructor Random().
  public static java.util.Random randomWithSeedZero() {
    return new java.util.Random(0);
  }
}
