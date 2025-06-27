package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
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

  /**
   * A map from a constant to the number of classes in the current scope that contains it. Null if
   * the literals level is CLASS.
   */
  @Nullable Map<Sequence, Integer> numClassesWith;

  /** The number of classes in the given scope. */
  int numClasses;

  /** Creates a new empty ScopeStatistics. */
  public ScopeStatistics() {
    numUses = new HashMap<>();
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        // Since CLASS level regards the class that the constant locate as its scope, no need to
        // store the classesWithConstant and numClasses.
        numClassesWith = null;
        numClasses = 1;
        break;
      case PACKAGE:
      case ALL:
        // Since the ALL level uses the whole project as its scope, the ALL_SCOPE key is used to
        // store the classesWithConstant and numClasses.
        numClassesWith = new HashMap<>();
        numClasses = 0;
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
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
    if (numClassesWith == null) {
      throw new RandoopBug("Should not call getNumClassesWith in CLASS level");
    }
    return numClassesWith;
  }

  /**
   * Returns the number of classes in the scope.
   *
   * @return the number of classes in the scope
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
  @RequiresNonNull("numClassesWith")
  public void addClassesWith(Sequence seq, int num) {
    numClassesWith.put(seq, numClassesWith.getOrDefault(seq, 0) + num);
  }

  /**
   * Increments the numClasses.
   *
   * @param num the total number of classes in the current scope
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
