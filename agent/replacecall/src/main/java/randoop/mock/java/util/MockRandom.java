package randoop.mock.java.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This class is intended to define replacements to calls to java.util.Random's next[Type] method.
 * For example, nextInt(), nextDouble, nextFloat(), and nextLong(). Each instance of Random() is
 * mapped to its own instance of Random(0). This will allow programs that make use of Random() to
 * behave deterministically in this regard.
 */
public class MockRandom {
  // Map from each instance of Random() in the client's code to a unique instance of Random(0).
  private static final Map<Random, Random> delegateMap = new HashMap<Random, Random>();

  /** Instances of mock classes should not be created. */
  private MockRandom() {
    throw new Error("Do not instantiate");
  }

  /**
   * Returns the instance of Random(0) associated with the parameter random. If none exists in the
   * map, a new instance of Random(0) is created, added to the map and returned.
   *
   * @param random instance to map from.
   * @return instance of Random(0) mapped from random.
   */
  private static Random getDelegateForInstance(Random random) {
    Random delegate = delegateMap.get(random);
    if (delegate == null) {
      delegate = new Random(0);
      delegateMap.put(random, delegate);
    }
    return delegate;
  }

  public static int nextInt(Random random) {
    return getDelegateForInstance(random).nextInt();
  }

  public static int nextInt(Random random, int bound) {
    return getDelegateForInstance(random).nextInt(bound);
  }

  public static double nextDouble(Random random) {
    return getDelegateForInstance(random).nextDouble();
  }

  public static float nextFloat(Random random) {
    return getDelegateForInstance(random).nextFloat();
  }

  public static long nextLong(Random random) {
    return getDelegateForInstance(random).nextLong();
  }

  public static boolean nextBoolean(Random random) {
    return getDelegateForInstance(random).nextBoolean();
  }

  public static double nextGaussian(Random random) {
    return getDelegateForInstance(random).nextGaussian();
  }

  public static void nextBytes(Random random, byte[] bytes) {
    getDelegateForInstance(random).nextBytes(bytes);
  }

  public static void setSeed(Random random, long seed) {
    getDelegateForInstance(random).setSeed(seed);
  }

  // Used to replace calls to the constructor Random().
  // These methods are currently not used as constructor replacements are not yet supported.
  /*
  public static java.util.Random randomWithSeedZero() {
      return new java.util.Random(0);
  }

  public static java.util.Random randomWithSeedZero(java.util.Random random) {
      return new java.util.Random(0);
  }*/
}
