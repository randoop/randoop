package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import randoop.sequence.Sequence;

/**
 * This class stores the constant mining information. It stores the number of uses of the sequence,
 * the number of classes that contain the sequence, and the total number of classes in the current
 * scope.
 */
public class ScopeStatistics {

  // These two fields have the same keyset.

  /** A map from a constant to the number of times it is used in the current scope. */
  Map<Sequence, Integer> numUses;

  /** A map from a constant to the number of classes in the current scope that contains it. */
  Map<Sequence, Integer> numClassesWith;

  /** The number of classes in the given scope. */
  int numClasses;

  /** Creates a new empty ScopeStatistics. */
  public ScopeStatistics() {
    numUses = new HashMap<>();
    numClassesWith = new HashMap<>();
    numClasses = 0;
  }

  /**
   * Returns the number of uses of each sequence.
   *
   * @return the number of uses of each sequence
   */
  public Map<Sequence, Integer> getNumUses() {
    return numUses;
  }

  /**
   * Returns the classesWithConstant information.
   *
   * @return the classesWithConstant information
   */
  public Map<Sequence, Integer> getNumClassesWith() {
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
  public void incrementNumUses(Sequence seq, int num) {
    numUses.put(seq, numUses.getOrDefault(seq, 0) + num);
  }

  /**
   * Increments the numClassesWith of the sequence.
   *
   * @param seq the sequence to be added
   * @param num the number of classes that contain the sequence to be added
   */
  public void addClassesWith(Sequence seq, int num) {
    numClassesWith.put(seq, numClassesWith.getOrDefault(seq, 0) + num);
  }

  /**
   * Increments the numClasses.
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
    return new HashSet<>(numUses.keySet());
  }
}
