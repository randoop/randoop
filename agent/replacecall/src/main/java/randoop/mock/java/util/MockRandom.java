package randoop.mock.java.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This class defines replacements for calls to java.util.Random's {@code next*} methods, such as
 * nextInt() and nextDouble().
 *
 * <p>The replacements don't use the provided receiver, but a deterministically-created one
 * (instantiated as {@code Random(0)}) instead. This makes calls to {@code next*} methods behave
 * deterministically.
 */
public class MockRandom {
  /**
   * Map from each instance of Random() in the client's code to a unique instance of Random that was
   * deterministically created as Random(0)..
   */
  private static final Map<Random, Random> delegateMap = new HashMap<Random, Random>();

  /** Instances of mock classes should not be created. */
  private MockRandom() {
    throw new Error("Do not instantiate");
  }

  /**
   * Returns the instance of Random(0) associated with the parameter {@code random}. If none exists
   * in the map, a new instance of Random(0) is created, added to the map and returned.
   *
   * @param random instance to map from.
   * @return instance of Random(0) mapped from random.
   */
  private static Random getDelegate(Random random) {
    Random delegate = delegateMap.get(random);
    if (delegate == null) {
      delegate = new Random(0);
      delegateMap.put(random, delegate);
    }
    return delegate;
  }

  public static int nextInt(Random random) {
    return getDelegate(random).nextInt();
  }

  public static int nextInt(Random random, int bound) {
    return getDelegate(random).nextInt(bound);
  }

  public static double nextDouble(Random random) {
    return getDelegate(random).nextDouble();
  }

  public static float nextFloat(Random random) {
    return getDelegate(random).nextFloat();
  }

  public static long nextLong(Random random) {
    return getDelegate(random).nextLong();
  }

  public static boolean nextBoolean(Random random) {
    return getDelegate(random).nextBoolean();
  }

  public static double nextGaussian(Random random) {
    return getDelegate(random).nextGaussian();
  }

  public static void nextBytes(Random random, byte[] bytes) {
    getDelegate(random).nextBytes(bytes);
  }

  public static void setSeed(Random random, long seed) {
    getDelegate(random).setSeed(seed);
  }

  // Used to replace calls to the constructor Random().
  // These methods are currently not used as constructor replacements are not yet supported.
  // Once constructor replacements are supported, the delegates and replacements that use them
  // are no longer necessary.
  /*
  public static java.util.Random randomWithSeedZero() {
      return new java.util.Random(0);
  }

  public static java.util.Random randomWithSeedZero(java.util.Random random) {
      return new java.util.Random(0);
  }*/
}
