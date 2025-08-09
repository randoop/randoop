package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import randoop.sequence.Sequence;

/**
 * This class stores information about constants used in one "scope" of the SUT, where a scope is a
 * class, a package, or the entire SUT.
 *
 * <p>For each constant (represented as a sequence), it stores the number of uses of the sequence
 * and the number of classes that contain the sequence. It also stores the number of classes in the
 * scope.
 */
public class ConstantStatistics {

  /** A map from a constant to its usage statistics. */
  private Map<Sequence, ConstantUses> constantStats = new HashMap<>();

  /** The number of classes in this scope. */
  private int numClasses = 0;

  /** Creates a new empty ConstantStatistics. */
  public ConstantStatistics() {}

  /**
   * Returns the map from constant to its usage statistics.
   *
   * @return the map from constant to its usage statistics
   */
  public Map<Sequence, ConstantUses> getConstantUses() {
    return constantStats;
  }

  /**
   * Returns the number of classes in the current scope.
   *
   * @return the number of classes in the current scope
   */
  public Integer getNumClasses() {
    return numClasses;
  }

  /**
   * Records a constant sequence.
   *
   * @param seq a sequence
   * @param numUses the number of uses of the sequence
   * @param numClassesWith the number of classes that contain the sequence
   */
  // TODO: Is this ever called more than once with a given sequence?
  public void incrementNumsForSequence(Sequence seq, int numUses, int numClassesWith) {
    ConstantUses currentStats = constantStats.getOrDefault(seq, new ConstantUses(0, 0));
    constantStats.put(
        seq,
        new ConstantUses(
            currentStats.getNumUses() + numUses,
            currentStats.getNumClassesWith() + numClassesWith));
  }

  /**
   * Increments the number of uses.
   *
   * @param seq a sequence
   * @param num the number of uses of the sequence
   */
  public void incrementNumUses(Sequence seq, int num) {
    ConstantUses currentStats = constantStats.getOrDefault(seq, new ConstantUses(0, 0));
    constantStats.put(
        seq, new ConstantUses(currentStats.getNumUses() + num, currentStats.getNumClassesWith()));
  }

  /**
   * Increments the number of classes that contain a sequence.
   *
   * @param seq a sequence
   * @param num the number of classes that contain the sequence
   */
  public void incrementNumClassesWith(Sequence seq, int num) {
    ConstantUses currentStats = constantStats.getOrDefault(seq, new ConstantUses(0, 0));
    constantStats.put(
        seq, new ConstantUses(currentStats.getNumUses(), currentStats.getNumClassesWith() + num));
  }

  /**
   * Increments the number of classes.
   *
   * @param num the number of classes to add to the current total
   */
  public void incrementNumClasses(int num) {
    numClasses += num;
  }

  /**
   * Returns all sequences that have been recorded.
   *
   * @return the sequences that have been recorded
   */
  public Set<Sequence> getSequenceSet() {
    return new HashSet<>(constantStats.keySet());
  }

  /**
   * Statistics for one constant within one scope: the number of uses of the constant and the number
   * of classes that contain the constant.
   */
  public static class ConstantUses {
    /** The number of uses of the constant. */
    private final int numUses;

    /** The number of classes that use the constant. */
    private final int numClassesWith;

    /**
     * Creates a new ConstantUses.
     *
     * @param numUses the number of uses of the constant
     * @param numClassesWith the number of classes that use the constant
     */
    public ConstantUses(int numUses, int numClassesWith) {
      this.numUses = numUses;
      this.numClassesWith = numClassesWith;
    }

    /**
     * Returns the number of uses of the constant.
     *
     * @return the number of uses of the constant
     */
    public int getNumUses() {
      return numUses;
    }

    /**
     * Returns the number of classes that use the constant.
     *
     * @return the number of classes that use the constant
     */
    public int getNumClassesWith() {
      return numClassesWith;
    }
  }
}
