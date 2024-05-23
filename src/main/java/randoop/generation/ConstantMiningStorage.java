package randoop.generation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.util.Log;

/**
 * This class is a generic storage for the constant mining information. T is the scope of the
 * constant mining, which can be ClassOrInterfaceType, Package, or Object, which corresponds to
 * users' input about literal level as CLASS, PACKAGE, or ALL. The storage stores the frequency of
 * the sequence, the number of classes that contain the sequence, and the total number of classes in
 * the current scope.
 *
 * @param <T> the scope of the constant mining
 */
public class ConstantMiningStorage<T> {

  /**
   * A map from a specific scope to its frequency information, which stands for the number of times
   * each constant is used in the current scope
   */
  Map<T, Map<Sequence, Integer>> frequencyInfo;

  /**
   * A map from a specific scope to its classesWithConstant information, which stands for the number
   * of classes in the current scope that contain each constant
   */
  Map<T, Map<Sequence, Integer>> classesWithConstantInfo;

  /**
   * A map from a specific scope to its totalClasses information, which stands for the number of
   * classes under the current scope
   */
  Map<T, Integer> totalClasses;

  /**
   * Creates a new ConstantMiningStorage with empty frequency, classesWithConstant, and
   * totalClasses. Different rules are applied to different literals levels.
   */
  public ConstantMiningStorage() {
    frequencyInfo = new HashMap<>();
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        // Since CLASS level regard the class that the constant locate as its scope, no need to
        // store the classesWithConstant and totalClasses
        classesWithConstantInfo = null;
        totalClasses = null;
        break;
      case PACKAGE:
        classesWithConstantInfo = new HashMap<>();
        totalClasses = new HashMap<>();
        break;
      case ALL:
        // Since the ALL level uses the whole project as its scope, the null key is used to store
        // the classesWithConstant and totalClasses
        frequencyInfo.put(null, new HashMap<>());
        classesWithConstantInfo = new HashMap<>();
        classesWithConstantInfo.put(null, new HashMap<>());
        totalClasses = new HashMap<>();
        totalClasses.put(null, 0);
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
  }

  /**
   * Add and update the frequency of the sequence to the current scope.
   *
   * @param t the scope of the constant mining
   * @param seq the sequence to be added
   * @param frequency the frequency of the sequence to be added
   */
  public void addFrequency(T t, Sequence seq, int frequency) {
    Map<Sequence, Integer> map;
    switch (GenInputsAbstract.literals_level) {
      case ALL:
        map = this.frequencyInfo.computeIfAbsent(null, __ -> new HashMap<>());
        map.put(seq, map.getOrDefault(seq, 0) + frequency);
        break;
      case PACKAGE:
      case CLASS:
        map = this.frequencyInfo.computeIfAbsent(t, __ -> new HashMap<>());
        map.put(seq, map.getOrDefault(seq, 0) + frequency);
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
  }

  /**
   * Add and update the classesWithConstant of the sequence to the current scope.
   *
   * @param t the scope of the constant mining
   * @param seq the sequence to be added
   * @param classesWithConstant the number of classes in the current scope that contain the sequence
   *     to be added
   */
  public void addClassesWithConstant(T t, Sequence seq, int classesWithConstant) {
    Map<Sequence, Integer> map;
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        throw new RuntimeException("Should not update classesWithConstant in CLASS level");
      case PACKAGE:
        map = this.classesWithConstantInfo.computeIfAbsent(t, __ -> new HashMap<>());
        map.put(seq, map.getOrDefault(seq, 0) + classesWithConstant);
        break;
      case ALL:
        map = this.classesWithConstantInfo.computeIfAbsent(null, __ -> new HashMap<>());
        map.put(seq, map.getOrDefault(seq, 0) + classesWithConstant);
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
  }

  /**
   * Add and update the totalClasses of the current scope.
   *
   * @param t the scope of the constant mining
   * @param totalClasses the total number of classes in the current scope
   */
  public void addTotalClasses(T t, int totalClasses) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        throw new RuntimeException("Should not update totalClasses in CLASS level");
      case PACKAGE:
        this.totalClasses.put(t, this.totalClasses.getOrDefault(t, 0) + totalClasses);
        break;
      case ALL:
        this.totalClasses.put(null, this.totalClasses.getOrDefault(null, 0) + totalClasses);
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
  }

  /**
   * Get all sequences that had been recorded under the specific scope, which are the constants
   * extracted by constant mining.
   *
   * @param t the specific package, class, or null
   * @return the set of sequences that recorded under the specific scope
   */
  public Set<Sequence> getSequencesForScope(T t) {
    if (!frequencyInfo.containsKey(t)) {
      Log.logPrintf("The scope %s is not found in the frequency information");
      return new HashSet<>();
    }

    return frequencyInfo.get(t).keySet();
  }

  /**
   * Get the complete frequency information of the current scope.
   *
   * @return the frequency information of the current scope
   */
  public Map<T, Map<Sequence, Integer>> getFrequencyInfo() {
    return frequencyInfo;
  }

  /**
   * Get the frequency information of the specific type.
   *
   * @param t the specific type
   * @return the frequency information of the specific type
   */
  public Map<Sequence, Integer> getFrequencyInfoForType(T t) {
    return frequencyInfo.get(t);
  }

  /**
   * Get the complete classesWithConstant information of the current scope.
   *
   * @return the classesWithConstant information of the current scope
   */
  public Map<T, Map<Sequence, Integer>> getClassesWithConstantInfo() {
    return classesWithConstantInfo;
  }

  /**
   * Get the classesWithConstant information of the specific type.
   *
   * @param t the specific type
   * @return the classesWithConstant information of the specific type
   */
  public Map<Sequence, Integer> getClassesWithConstantInfoForType(T t) {
    return classesWithConstantInfo.get(t);
  }

  /**
   * Get the complete totalClasses information of the current scope.
   *
   * @return the totalClasses information of the current scope
   */
  public Map<T, Integer> getTotalClasses() {
    return totalClasses;
  }

  /**
   * Get the totalClasses information of the specific type.
   *
   * @param t the specific type
   * @return the totalClasses information of the specific type
   */
  public Integer getTotalClassesForType(T t) {
    // The default value is null to avoid when t is java.lang or other standard libraries
    return totalClasses.getOrDefault(t, null);
  }
}
