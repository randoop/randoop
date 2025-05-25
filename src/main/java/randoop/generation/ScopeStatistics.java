package randoop.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;

/**
 * This classes stores the constant mining information. It stores the frequency of the sequence, the
 * number of classes that contain the sequence, and the total number of classes in the current
 * scope.
 */
public class ScopeStatistics {

  /** A map from a constant to the number of times it is used in the current scope. */
  Map<Sequence, Integer> numUses;

  /**
   * A map from a constant to the number of classes in the current scope that contains it. Null if
   * the literals level is CLASS
   */
  Map<Sequence, Integer> classesWithConstantInfo;

  /** The number of classes in the given scope. Null if the literals level is CLASS. */
  Integer numClasses;

  /**
   * Creates a new ScopeStatistics with empty frequency, classWithConstant, and numClasses.
   * Different rules are applied to different literals levels.
   */
  public ScopeStatistics() {
    numUses = new HashMap<>();
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        // Since CLASS level regards the class that the constant locate as its scope, no need to
        // store the classesWithConstant and numClasses.
        classesWithConstantInfo = null;
        numClasses = null;
        break;
      case PACKAGE:
      case ALL:
        // Since the ALL level uses the whole project as its scope, the null key is used to store
        // the classesWithConstant and numClasses.
        classesWithConstantInfo = new HashMap<>();
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
  public Map<Sequence, Integer> getClassesWithConstantInfo() {
    return classesWithConstantInfo;
  }

  /**
   * Get the number of classes information.
   *
   * @return the number of classes information
   */
  public Integer getNumClasses() {
    return numClasses;
  }

  /**
   * Add and update the frequency of the sequence.
   *
   * @param seq the sequence to be added
   * @param frequency the frequency of the sequence to be added
   */
  public void addFrequency(Sequence seq, int frequency) {
    numUses.put(seq, numUses.getOrDefault(seq, 0) + frequency);
  }

  /**
   * Add and update the classesWithConstantInfo of the sequence.
   *
   * @param seq the sequence to be added
   * @param numClassesWithConstant the number of classes that contain the sequence to be added
   */
  public void addToClassWithConstantInfo(Sequence seq, int numClassesWithConstant) {
    classesWithConstantInfo.put(
        seq, classesWithConstantInfo.getOrDefault(seq, 0) + numClassesWithConstant);
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
    return numUses.keySet();
  }
}
