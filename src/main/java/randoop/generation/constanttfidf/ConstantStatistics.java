package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.KeyFor;
import randoop.sequence.Sequence;

/**
 * This class stores information about constants used in part of the SUT. There is one {@code
 * ConstantStatistics} per "scope", where a scope is a class, a package, or the entire SUT.
 *
 * <p>It stores the number of uses of the sequence, the number of classes that contain the sequence,
 * and the total number of classes in the current scope.
 */
public class ConstantStatistics {

  /** A map from a constant to the number of times it is used in the current scope. */
  private Map<@KeyFor({"numUses", "numClassesWith"}) Sequence, Integer> numUses = new HashMap<>();

  /** A map from a constant to the number of classes in the current scope that contains it. */
  private Map<@KeyFor({"numClassesWith", "numUses"}) Sequence, Integer> numClassesWith =
      new HashMap<>();

  /** The number of classes in the given scope. */
  private int numClasses = 0;

  /** Creates a new empty ConstantStatistics. */
  public ConstantStatistics() {}

  /**
   * Returns a map from constant to the number of uses of each the constant.
   *
   * @return a map from constant to the number of uses of each the constant
   */
  public Map<@KeyFor("this.numClassesWith") Sequence, Integer> getNumUses() {
    return numUses;
  }

  /**
   * Returns a map from constant to the number of classes that use the constant.
   *
   * @return a map from constant to the number of classes that use the constant
   */
  public Map<@KeyFor("this.numUses") Sequence, Integer> getNumClassesWith() {
    return numClassesWith;
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
  public void incrementNumUses(@KeyFor("this.numClassesWith") Sequence seq, int num) {
    numUses.put(seq, numUses.getOrDefault(seq, 0) + num);
  }

  /**
   * Increments the numClassesWith of the sequence.
   *
   * @param seq the sequence to be added
   * @param num the number of classes that contain the sequence to be added
   */
  public void incrementNumClassesWith(@KeyFor("this.numUses") Sequence seq, int num) {
    numClassesWith.put(seq, numClassesWith.getOrDefault(seq, 0) + num);
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
  public Set<@KeyFor("this.numUses") Sequence> getSequenceSet() {
    return new HashSet<>(numUses.keySet());
  }
}
