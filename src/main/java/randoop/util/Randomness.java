package randoop.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import plume.SimpleLog;
import randoop.BugInRandoopException;

/**
 * A simple-to-use wrapper around {@link java.util.Random}.
 *
 * <p>It also supports logging, for debugging of apparently nondeterministic behavior.
 */
public final class Randomness {

  public static SimpleLog selectionLog = new SimpleLog(false);
  /** 0 = no output, 1 = brief output, 2 = verbose output */
  public static int verbosity = 1;

  private Randomness() {
    throw new IllegalStateException("no instances");
  }

  public static final long SEED = 0;

  /**
   * The random generator that makes random choices. (Developer note: do not declare new Random
   * objects; use this one instead).
   */
  static Random random = new Random(SEED);

  public static void setSeed(long newSeed) {
    random.setSeed(newSeed);
    totalCallsToRandom = 0;
    logSelection("[Random object]", "setSeed", newSeed);
  }

  private static Field seedField;

  static {
    try {
      seedField = Random.class.getDeclaredField("seed");
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
    seedField.setAccessible(true);
  }

  /**
   * Get the seed.
   *
   * <p>To exactly reproduce the state, the result needs to be XOR'd with 0x5DEECE66DL if passed to
   * setSeed, because setSeed internally XORs with that value.
   *
   * @return the internal random seed
   */
  public static long getSeed() {
    try {
      return ((AtomicLong) seedField.get(Randomness.random)).get();
    } catch (IllegalAccessException e) {
      throw new Error(e);
    }
  }

  /** Number of calls to the underlying Random instance that this wraps. */
  private static int totalCallsToRandom = 0;

  /**
   * Call this before every use of Randomness.random.
   *
   * @param caller the name of the method that called Randomness.random.
   */
  private static void incrementCallsToRandom(String caller) {
    totalCallsToRandom++;
    if (Log.isLoggingOn()) {
      Log.logLine(
          "randoop.util.Randomness called by "
              + caller
              + ": "
              + totalCallsToRandom
              + " calls to Random so far, seed = "
              + getSeed());
    }
  }

  /**
   * Uniformly random int from [0, i)
   *
   * @param i upper bound on range for generated values
   * @return a value selected from range [0, i)
   */
  public static int nextRandomInt(int i) {
    incrementCallsToRandom("nextRandomInt");
    int value = Randomness.random.nextInt(i);
    logSelection(value, "nextRandomInt", i);
    return value;
  }

  public static <T> T randomMember(List<T> list) {
    if (list == null || list.isEmpty()) {
      throw new IllegalArgumentException("Expected non-empty list");
    }
    int position = nextRandomInt(list.size());
    logSelection(position, "randomMember", list);
    return list.get(position);
  }

  public static <T> T randomMember(SimpleList<T> list) {
    if (list == null || list.isEmpty()) {
      throw new IllegalArgumentException("Expected non-empty list");
    }
    int position = nextRandomInt(list.size());
    logSelection(position, "randomMember", list);
    return list.get(position);
  }

  /**
   * Randomly selects an element from a weighted distribution of elements.
   *
   * <p>Efficiency note: iterates through the entire list twice (once to compute interval length,
   * once to select element).
   *
   * @param <T> the type of elements of the list
   * @param list the list from which to choose an element
   * @return a member of {@code list}, chosen according to the weights
   */
  public static <T extends WeightedElement> T randomMemberWeighted(SimpleList<T> list) {

    double totalWeight = 0.0;
    for (int i = 0; i < list.size(); i++) {
      double weight = list.get(i).getWeight();
      if (weight <= 0) {
        throw new BugInRandoopException("Weight should be positive: " + weight);
      }
      totalWeight += weight;
    }

    // Select a random point in interval and find its corresponding element.
    incrementCallsToRandom("randomMemberWeighted(SimpleList)");
    double chosenPoint = Randomness.random.nextDouble() * totalWeight;
    double currentPoint = 0;
    for (int i = 0; i < list.size(); i++) {
      currentPoint += list.get(i).getWeight();
      if (currentPoint >= chosenPoint) {
        logSelection(i, "randomMemberWeighted", list);
        return list.get(i);
      }
    }
    throw new BugInRandoopException("Unable to select random member");
  }

  /**
   * Randomly selects an element from a weighted distribution of elements. These weights are with
   * respect to each other. They are not normalized (they might add up to any value.)
   *
   * <p>Iterates through the entire list once, then does a binary search to select the element.
   *
   * <p>Used internally when the {@code --weighted-constants} and/or {@code --weighted-sequences}
   * options are used.
   *
   * @param list the list of elements to select from
   * @param weights the map of elements to their weights; uses the intrinsic weight if the element
   *     is not a key in the map
   * @param <T> the type of the elements in the list
   * @return a randomly selected element from {@code list}
   */
  public static <T extends WeightedElement> T randomMemberWeighted(
      SimpleList<T> list, Map<T, Double> weights) {

    double totalWeight = 0.0;
    // The ith element is the cumulative weight of all elements preceding the ith (that is,
    // exclusive rather than inclusive).  The last (i+1)th element is the weight of all elements.
    double[] cumulativeWeights = new double[list.size() + 1];
    cumulativeWeights[0] = 0.0;
    for (int i = 0; i < list.size(); i++) {
      T elt = list.get(i);
      Double weightOrNull = weights.get(elt);
      double weight;
      if (weightOrNull != null) {
        weight = weightOrNull;
      } else {
        weight = elt.getWeight();
        Log.logLine(
            "randoop.util.Randomness: weights map does not contain an entry for "
                + elt
                + "; using intrinsic weight "
                + weight);
      }
      if (weight <= 0) {
        throw new BugInRandoopException("Weight should be positive: " + weight);
      }
      cumulativeWeights[i] = totalWeight;
      totalWeight += weight;
    }
    cumulativeWeights[list.size()] = totalWeight;

    // Select a random point in interval and find its corresponding element.
    incrementCallsToRandom("randomMemberWeighted(SimpleList, Map)");
    double chosenPoint = Randomness.random.nextDouble() * totalWeight;
    if (selectionLog.enabled()) {
      selectionLog.log(
          "chosenPoint = %s, cumulativeWeights = %s%n", chosenPoint, cumulativeWeights);
    }

    int index = binarySearchForIndex(cumulativeWeights, chosenPoint);
    if (selectionLog.enabled()) { // body is expensive
      logSelection(
          index,
          "randomMemberWeighted(List,Map)",
          String.format(
              "%n << %s%n    (class %s),%n    %s%n    (class %s, size %s)>>",
              list, list.getClass(), weights, weights.getClass(), weights.size()));
    }
    return list.get(index);
  }

  /**
   * Performs a binary search on a cumulative weight distribution and returns the corresponding
   * index i such that {@code cumulativeWeights.get(i) < point <= cumulativeWeights.get(i + 1)} for
   * {@code 0 <= i < cumulativeWeights.length}.
   *
   * @param cumulativeWeights the cumulative weight distribution to search through. The ith element
   *     is the cumulative weight of all elements before the ith (that is, exclusive rather than
   *     inclusive). The last (i+1)th element is the weight of all elements.
   * @param point the value used to find the index within the cumulative weight distribution
   * @return the index corresponding to point's location in the cumulative weight distribution
   */
  private static int binarySearchForIndex(double[] cumulativeWeights, double point) {
    int low = 0;
    int high = cumulativeWeights.length;
    int mid = (low + high) / 2;
    while (!(cumulativeWeights[mid] < point && point <= cumulativeWeights[mid + 1])) {
      if (cumulativeWeights[mid] < point) {
        low = mid;
      } else {
        high = mid;
      }
      mid = (low + high) / 2;
    }
    return mid;
  }

  /**
   * Return a random member of the set, selected uniformly at random.
   *
   * @param <T> the type of elements of the set param set the collection from which to choose an
   *     element
   * @param set the collection from which to select an element
   * @return a randomly-selected member of the set
   */
  public static <T> T randomSetMember(Collection<T> set) {
    int setSize = set.size();
    int randIndex = Randomness.nextRandomInt(setSize);
    logSelection(randIndex, "randomSetMember", set);
    return CollectionsExt.getNthIteratedElement(set, randIndex);
  }

  /**
   * Return true with probability {@code trueProb}, otherwise false.
   *
   * @param trueProb the likelihood that true is returned; must be within [0..1]
   * @return true with likelihood {@code trueProb}; otherwise false
   */
  public static boolean weightedCoinFlip(double trueProb) {
    if (trueProb < 0 || trueProb > 1) {
      throw new IllegalArgumentException("arg must be between 0 and 1.");
    }
    double falseProb = 1 - trueProb;
    incrementCallsToRandom("weightedCoinFlip");
    boolean result = Randomness.random.nextDouble() >= falseProb;
    logSelection(result, "weightedCoinFlip", trueProb);
    return result;
  }

  /**
   * Return true or false with the given relative probabilites, which need not add to 1.
   *
   * @param falseProb the likelihood that true is returned; an arbitrary non-negative number
   * @param trueProb the likelihood that true is returned; an arbitrary non-negative number
   * @return true or false, with the given probabilities
   */
  public static boolean randomBoolFromDistribution(double falseProb, double trueProb) {
    if (trueProb < 0 || falseProb > 1) {
      throw new IllegalArgumentException("arg must be between 0 and 1.");
    }
    double falseProbNormalized = falseProb / (falseProb + trueProb);
    incrementCallsToRandom("randomBoolFromDistribution");
    boolean result = Randomness.random.nextDouble() >= falseProbNormalized;
    logSelection(result, "randomBoolFromDistribution", falseProb + ", " + trueProb);
    return result;
  }

  /**
   * Logs the value that was randomly selected, along with the calling method and its argument.
   *
   * @param returnValue the value randomly selected
   * @param methodName the name of the method called
   * @param argument the method argument
   */
  private static void logSelection(Object returnValue, String methodName, Object argument) {
    if (selectionLog.enabled() && verbosity > 0) {
      StackTraceElement[] trace = Thread.currentThread().getStackTrace();
      String methodWithArg = methodName;
      if (argument != null) {
        methodWithArg += "(" + toString(argument) + ")";
      }
      selectionLog.log(
          "#%d: %s => %s; seed %s; called from %s%n",
          totalCallsToRandom, methodWithArg, returnValue, getSeed(), trace[3]);
    }
  }

  /**
   * Produces a printed representation of the object, depending on the verbosity level.
   *
   * @param o the object to produce a printed representation of
   * @return a printed representation of the argument
   * @see #verbosity
   */
  private static String toString(Object o) {
    if (o instanceof Collection<?>) {
      Collection<?> coll = (Collection<?>) o;
      switch (verbosity) {
        case 1:
          return coll.getClass() + " of size " + coll.size();
        case 2:
          return coll.toString();
        default:
          throw new Error("verbosity = " + verbosity);
      }
    } else if (o instanceof SimpleList<?>) {
      SimpleList<?> sl = (SimpleList<?>) o;
      switch (verbosity) {
        case 1:
          return sl.getClass() + " of size " + sl.size();
        case 2:
          return sl.toJDKList().toString();
        default:
          throw new Error("verbosity = " + verbosity);
      }
    } else {
      return o.toString();
    }
  }
}
