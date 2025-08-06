package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import randoop.sequence.Sequence;

/**
 * This class stores information about constants used in part of the SUT. There is one {@code
 * ConstantStatistics} per "scope", where a scope is a class, a package, or the entire SUT.
 *
 * <p>It stores the number of uses of the sequence, the number of classes that contain the sequence,
 * and the total number of classes in the current scope.
 */
public class ConstantStatistics {

  /**
   * A class that holds statistics for a constant: the number of uses and the number of classes that
   * contain it.
   */
  public static class ConstantStats {
    private final int numUses;
    private final int numClassesWith;

    public ConstantStats(int numUses, int numClassesWith) {
      this.numUses = numUses;
      this.numClassesWith = numClassesWith;
    }

    public int getNumUses() {
      return numUses;
    }

    public int getNumClassesWith() {
      return numClassesWith;
    }
  }

  /** A map from a constant to its usage statistics. */
  private Map<Sequence, ConstantStats> constantStats = new HashMap<>();

  /** The number of classes in this scope. */
  private int numClasses = 0;

  /** Creates a new empty ConstantStatistics. */
  public ConstantStatistics() {}

  /**
   * Returns the map from constant to its usage statistics.
   *
   * @return the map from constant to its usage statistics
   */
  public Map<Sequence, ConstantStats> getConstantStats() {
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
   * Increments the number of uses.
   *
   * @param seq a sequence
   * @param num the number of uses of the sequence
   */
  public void incrementNumUses(Sequence seq, int num) {
    ConstantStats currentStats = constantStats.getOrDefault(seq, new ConstantStats(0, 0));
    constantStats.put(
        seq, new ConstantStats(currentStats.getNumUses() + num, currentStats.getNumClassesWith()));
  }

  /**
   * Increments the number of classes that contain the sequence.
   *
   * @param seq the sequence to be added
   * @param num the number of classes that contain the sequence to be added
   */
  public void incrementNumClassesWith(Sequence seq, int num) {
    ConstantStats currentStats = constantStats.getOrDefault(seq, new ConstantStats(0, 0));
    constantStats.put(
        seq, new ConstantStats(currentStats.getNumUses(), currentStats.getNumClassesWith() + num));
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
   * @return the set of sequences that have been recorded
   */
  public Set<Sequence> getSequenceSet() {
    return new HashSet<>(constantStats.keySet());
  }
}
