package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;

/**
 * This class stores the constant mining information. It stores the frequency of the sequence, the
 * number of classes that contain the sequence, and the total number of classes in the current
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
   * Get the frequency information.
   *
   * @return the frequency information
   */
  public Map<Sequence, Integer> getNumUses() {
    return numUses;
  }

  /**
   * Get the classesWithConstant information.
   *
   * @return the classesWithConstant information
   */
  @Nullable public Map<Sequence, Integer> getNumClassesWith() {
    return numClassesWith;
  }

  /**
   * Get the number of classes in the scope.
   *
   * @return the number of classes in the scope
   */
  public Integer getNumClasses() {
    return numClasses;
  }

  /**
   * Increment the number of uses.
   *
   * @param seq a sequence
   * @param num the number of uses of the sequence
   */
  public void addUses(Sequence seq, int num) {
    numUses.put(seq, numUses.getOrDefault(seq, 0) + num);
  }

  /**
   * Add and update the numClassesWith of the sequence.
   *
   * @param seq the sequence to be added
   * @param num the number of classes that contain the sequence to be added
   */
  public void addClassesWith(Sequence seq, int num) {
    numClassesWith.put(seq, numClassesWith.getOrDefault(seq, 0) + num);
  }

  /**
   * Add and update the numClasses.
   *
   * @param num the total number of classes in the current scope
   */
  public void addToTotalClasses(int num) {
    numClasses += num;
  }

  /**
   * Get all sequences that had been recorded.
   *
   * @return the set of sequences that have been recorded
   */
  public Set<Sequence> getSequenceSet() {
    return new HashSet<>(numUses.keySet());
  }
}
