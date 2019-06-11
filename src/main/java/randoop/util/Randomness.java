package randoop.util;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;

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

  /** The default initial seed for the random number generator. */
  // Public so it can be accessed by GenInputsAbstract.java
  public static final long DEFAULT_SEED = 0;

  /**
   * The random generator that makes random choices. (Developer note: do not declare new Random
   * objects; use this one instead).
   */
  private static Random random = new Random(DEFAULT_SEED);

  /**
   * Sets the seed of this random number generator.
   *
   * @param seed the initial seed
   */
  public static void setSeed(long seed) {
    random.setSeed(seed);
    totalCallsToRandom = 0;
    logSelection("[Random object]", "setSeed", seed);
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
        "randoop.util.Randomness called by %s: %d calls to Random so far%n",
        caller, totalCallsToRandom);
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
   * Randomly selects an element from a weighted distribution of elements. These weights are with
   * respect to each other. They are not normalized (they might add up to any value).
   *
   * @param list the list of elements to select from
   * @param weights the map of elements to their weights. Each element's weight must be
   *     non-negative. An element with a weight of zero will never be selected.
   * @param <T> the type of the elements in the list
   * @return a randomly selected element from {@code list}
   */
  public static <T> T randomMemberWeighted(SimpleList<T> list, Map<T, Double> weights) {

    if (list.size() == 0) {
      throw new IllegalArgumentException("Empty list");
    }

    double totalWeight = 0.0;
    for (int i = 0; i < list.size(); i++) {
      T elt = list.get(i);
      double weight = weights.get(elt);
      if (weight < 0) {
        throw new RandoopBug("Weight should be positive: " + weight);
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
   * @param weights the map of elements to their weights. Each element's weight must be
   *     non-negative. An element with a weight of zero will never be selected.
   * @param totalWeight the total weight of the elements of the list
   * @return a randomly selected element from {@code list}
   */
  public static <T> T randomMemberWeighted(
      SimpleList<T> list, Map<T, Double> weights, double totalWeight) {

    if (list.size() == 0) {
      throw new IllegalArgumentException("Empty list");
    }

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
      currentPoint += weights.get(list.get(i));
      if (currentPoint > chosenPoint) {
        logSelection(i, "randomMemberWeighted", list);
        return list.get(i);
      }
    }
    System.out.printf("totalWeight=%f%n", totalWeight);
    System.out.printf("currentPoint=%f%n", currentPoint);
    System.out.printf("list.size()=%d%n", list.size());
    for (int i = 0; i < list.size(); i++) {
      System.out.printf("%d, %f%n", i, weights.get(list.get(i)));
    }
    throw new RandoopBug("Unable to select random member");
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
                "#%d: %s => %s; calls so far %d; called from %s%n",
                totalCallsToRandom, methodWithArg, returnValue, totalCallsToRandom, trace[3]);
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
