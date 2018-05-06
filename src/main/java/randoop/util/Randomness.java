package randoop.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import randoop.BugInRandoopException;
import randoop.main.GenInputsAbstract;

/**
 * A simple-to-use wrapper around {@link java.util.Random}.
 *
 * <p>It also supports logging, for debugging of apparently nondeterministic behavior.
 */
public final class Randomness {

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
   * @param caller the name of the method that called Randomness.random
   */
  private static void incrementCallsToRandom(String caller) {
    totalCallsToRandom++;
    Log.logPrintf(
        "randoop.util.Randomness called by %s: %d calls to Random so far, seed = %d%n",
        caller, totalCallsToRandom, getSeed());
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

  /**
   * Returns a randomly-chosen member of the list.
   *
   * @param <T> the type of list elements
   * @param list the list from which to choose a random member
   * @return a randomly-chosen member of the list
   */
  public static <T> T randomMember(List<T> list) {
    if (list == null || list.isEmpty()) {
      throw new IllegalArgumentException("Expected non-empty list");
    }
    int position = nextRandomInt(list.size());
    logSelection(position, "randomMember", list);
    return list.get(position);
  }

  /**
   * Returns a randomly-chosen member of the list.
   *
   * @param <T> the type of list elements
   * @param list the list from which to choose a random member
   * @return a randomly-chosen member of the list
   */
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

    return randomMemberWeighted(list, totalWeight);
  }

  /**
   * Randomly selects an element from a weighted distribution of elements.
   *
   * <p>Efficiency note: iterates through the entire list twice (once to compute interval length,
   * once to select element).
   *
   * @param <T> the type of elements of the list
   * @param list the list from which to choose an element
   * @param totalWeight the total weight of the elements of the list
   * @return a member of {@code list}, chosen according to the weights
   */
  public static <T extends WeightedElement> T randomMemberWeighted(
      SimpleList<T> list, double totalWeight) {
    // Select a random point in interval and find its corresponding element.
    incrementCallsToRandom("randomMemberWeighted(SimpleList)");
    double chosenPoint = Randomness.random.nextDouble() * totalWeight;
    double currentPoint = 0;
    for (int i = 0; i < list.size(); i++) {
      currentPoint += list.get(i).getWeight();
      if (currentPoint > chosenPoint) {
        logSelection(i, "randomMemberWeighted", list);
        return list.get(i);
      }
    }
    throw new BugInRandoopException("Unable to select random member");
  }

  /**
   * Return weight from weights map, or intrinsic weight if elt is not a key for weights.
   *
   * @param <T> type of elt
   * @param elt item to get the weight of
   * @param weights mapping from T to weights
   * @return weight of elt
   */
  private static <T extends WeightedElement> double getWeight(T elt, Map<T, Double> weights) {
    Double weightOrNull = weights.get(elt);
    double weight;
    if (weightOrNull != null) {
      weight = weightOrNull;
    } else {
      weight = elt.getWeight();
      Log.logPrintf(
          "randoop.util.Randomness: key %s not found; using intrinsic weight %d%n", elt, weight);
    }
    return weight;
  }

  /**
   * Randomly selects an element from a weighted distribution of elements. These weights are with
   * respect to each other. They are not normalized (they might add up to any value).
   *
   * @param list the list of elements to select from
   * @param weights the map of elements to their weights; uses the intrinsic weight if the element
   *     is not a key in the map. Each element's weight must be non-negative. An element with a
   *     weight of zero will never be selected.
   * @param <T> the type of the elements in the list
   * @return a randomly selected element from {@code list}
   */
  public static <T extends WeightedElement> T randomMemberWeighted(
      SimpleList<T> list, Map<T, Double> weights) {

    double totalWeight = 0.0;
    for (int i = 0; i < list.size(); i++) {
      T elt = list.get(i);
      double weight = getWeight(elt, weights);
      if (weight < 0) {
        throw new BugInRandoopException("Weight should be positive: " + weight);
      }
      totalWeight += weight;
    }

    return randomMemberWeighted(list, weights, totalWeight);
  }

  /**
   * Randomly selects an element from a weighted distribution of elements. These weights are with
   * respect to each other. They are not normalized (they might add up to any value).
   *
   * @param <T> the type of the elements in the list
   * @param list the list of elements to select from
   * @param weights the map of elements to their weights; uses the intrinsic weight if the element
   *     is not a key in the map. Each element's weight must be non-negative. An element with a
   *     weight of zero will never be selected.
   * @param totalWeight the total weight of the elements of the list
   * @return a randomly selected element from {@code list}
   */
  public static <T extends WeightedElement> T randomMemberWeighted(
      SimpleList<T> list, Map<T, Double> weights, double totalWeight) {

    // Select a random point in interval and find its corresponding element.
    incrementCallsToRandom("randomMemberWeighted(SimpleList)");
    double chosenPoint = Randomness.random.nextDouble() * totalWeight;
    if (GenInputsAbstract.selection_log != null) {
      try {
        GenInputsAbstract.selection_log.write(String.format("chosenPoint = %s%n", chosenPoint));
      } catch (IOException e) {
        throw new Error("Problem writing to selection-log", e);
      }
    }

    double currentPoint = 0;
    for (int i = 0; i < list.size(); i++) {
      currentPoint += getWeight(list.get(i), weights);
      if (currentPoint > chosenPoint) {
        logSelection(i, "randomMemberWeighted", list);
        return list.get(i);
      }
    }
    throw new BugInRandoopException("Unable to select random member");
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
    if (falseProb < 0) {
      throw new IllegalArgumentException("falseProb is " + falseProb + ", must be non-negative");
    }
    if (trueProb < 0) {
      throw new IllegalArgumentException("trueProb is " + trueProb + ", must be non-negative");
    }
    double totalProb = falseProb + trueProb;
    if (totalProb == 0) {
      throw new IllegalArgumentException("falseProb and trueProb are both 0");
    }
    double falseProbNormalized = falseProb / totalProb;
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
    if (GenInputsAbstract.selection_log != null && verbosity > 0) {
      StackTraceElement[] trace = Thread.currentThread().getStackTrace();
      String methodWithArg = methodName;
      if (argument != null) {
        methodWithArg += "(" + toString(argument) + ")";
      }
      try {
        String msg =
            String.format(
                "#%d: %s => %s; seed %s; called from %s%n",
                totalCallsToRandom, methodWithArg, returnValue, getSeed(), trace[3]);
        GenInputsAbstract.selection_log.write(msg);
        GenInputsAbstract.selection_log.flush();
      } catch (IOException e) {
        throw new RandoopLoggingError("Error writing to selection-log: " + e.getMessage());
      }
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
