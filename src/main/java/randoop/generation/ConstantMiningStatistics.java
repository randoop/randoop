package randoop.generation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.signedness.qual.Signed;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.util.Log;

/**
 * This class stores constant mining information. T is the scope of the constant mining, which can
 * be ClassOrInterfaceType, Package, or Object, which corresponds to users' input about literal
 * level as CLASS, PACKAGE, or ALL. The storage stores the frequency of the sequence, the number of
 * classes that contain the sequence, and the total number of classes in the current scope.
 *
 * @param <T> the scope of the constant mining
 */
public class ConstantMiningStatistics<T extends @Signed Object> {

  /**
   * A map from a specific scope to its frequency information, which stands for the number of times
   * each constant is used in the current scope.
   */
  Map<T, Map<Sequence, Integer>> numUses;

  /**
   * A map from a specific scope to its classesWithConstant information, which stands for the number
   * of classes in the current scope that contain each constant.
   */
  Map<T, Map<Sequence, Integer>> classesWithConstantInfo;

  /** A map from a scope to the number of classes within the scope. */
  Map<T, Integer> numClasses;

  /**
   * Creates a new ConstantMiningStatistics with empty frequency, classesWithConstant, and
   * numClasses. Different rules are applied to different literals levels.
   */
  public ConstantMiningStatistics() {
    numUses = new HashMap<>();
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        // Since CLASS level regards the class that the constant locate as its scope, no need to
        // store the classesWithConstant and numClasses.
        classesWithConstantInfo = null;
        numClasses = null;
        break;
      case PACKAGE:
        classesWithConstantInfo = new HashMap<>();
        numClasses = new HashMap<>();
        break;
      case ALL:
        // Since the ALL level uses the whole project as its scope, the null key is used to store
        // the classesWithConstant and numClasses.
        numUses.put(null, new HashMap<>());
        classesWithConstantInfo = new HashMap<>();
        classesWithConstantInfo.put(null, new HashMap<>());
        numClasses = new HashMap<>();
        numClasses.put(null, 0);
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
        map = this.numUses.computeIfAbsent(null, __ -> new HashMap<>());
        map.put(seq, map.getOrDefault(seq, 0) + frequency);
        break;
      case PACKAGE:
      case CLASS:
        map = this.numUses.computeIfAbsent(t, __ -> new HashMap<>());
        map.put(seq, map.getOrDefault(seq, 0) + frequency);
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
  }

  /**
   * Add and update the classesWithConstantInfo of the sequence to the current scope.
   *
   * @param t the scope of the constant mining
   * @param seq the sequence to be added
   * @param numClassesWithConstant the number of classes in the current scope that contain the
   *     sequence to be added
   */
  public void addToClassesWithConstantInfo(T t, Sequence seq, int numClassesWithConstant) {
    Map<Sequence, Integer> map;
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        throw new RuntimeException("Should not update numClassesWithConstant in CLASS level");
      case PACKAGE:
        map = this.classesWithConstantInfo.computeIfAbsent(t, __ -> new HashMap<>());
        map.put(seq, map.getOrDefault(seq, 0) + numClassesWithConstant);
        break;
      case ALL:
        map = this.classesWithConstantInfo.computeIfAbsent(null, __ -> new HashMap<>());
        map.put(seq, map.getOrDefault(seq, 0) + numClassesWithConstant);
        break;
      default:
        throw new RuntimeException("Unknown literals level");
    }
  }

  /**
   * Add and update the numClasses of the current scope.
   *
   * @param t the scope of the constant mining
   * @param numClasses the total number of classes in the current scope
   */
  public void addToTotalClasses(T t, int numClasses) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        throw new RuntimeException("Should not update numClasses in CLASS level");
      case PACKAGE:
        this.numClasses.put(t, this.numClasses.getOrDefault(t, 0) + numClasses);
        break;
      case ALL:
        this.numClasses.put(null, this.numClasses.getOrDefault(null, 0) + numClasses);
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
    if (!numUses.containsKey(t)) {
      Log.logPrintf("The scope %s is not found in the frequency information", t);
      return new HashSet<>();
    }

    return numUses.get(t).keySet();
  }

  /**
   * Get the complete frequency information of the current scope.
   *
   * @return the frequency information of the current scope
   */
  public Map<T, Map<Sequence, Integer>> getFrequencyInfo() {
    return numUses;
  }

  /**
   * Get the frequency information of the given type or package.
   *
   * @param t a type or package
   * @return the frequency information of the given type or package
   */
  public Map<Sequence, Integer> getFrequencyInfo(T t) {
    return numUses.get(t);
  }

  /**
   * Get the complete classesWithConstantInfo information of the current scope.
   *
   * @return the classesWithConstantInfo information of the current scope
   */
  public Map<T, Map<Sequence, Integer>> getClassesWithConstantInfo() {
    return classesWithConstantInfo;
  }

  /**
   * Get the classesWithConstantInfo information of the specific type.
   *
   * @param t the specific type
   * @return the classesWithConstantInfo information of the specific type
   */
  public Map<Sequence, Integer> getConstantInfoForType(T t) {
    return classesWithConstantInfo.get(t);
  }

  /**
   * Get the complete numClasses information of the current scope.
   *
   * @return the numClasses information of the current scope
   */
  public Map<T, Integer> getTotalClasses() {
    return numClasses;
  }

  /**
   * Get the numClasses information of the specific type.
   *
   * @param t the specific type
   * @return the numClasses information of the specific type
   */
  public Integer getTotalClassesInScope(T t) {
    // The default value is null to avoid when t is java.lang or other standard libraries
    return numClasses.getOrDefault(t, null);
  }

  /**
   * Outputs a string representation of the map to the StringBuilder.
   *
   * @param   <K2> the type of the map keys
   * @param   <V2> the type of the map values
   * @param sb the destination for the string representation
   * @param indent how many spaces to indent each line of output
   * @param freqMap the map to print
   */
  static <K2 extends @Signed Object, V2 extends @Signed Object> void formatFrequencyMap(
      StringBuilder sb, String indent, Map<K2, V2> freqMap) {
    for (Map.Entry<K2, V2> entry : freqMap.entrySet()) {
      sb.append(indent);
      sb.append(entry.getKey());
      sb.append(" : ");
      sb.append(entry.getValue());
      sb.append(System.lineSeparator());
    }
  }

  /**
   * Outputs a string representation of the frequency info to the StringBuilder.
   *
   * @param <K1> the type of the outer map keys
   * @param <K2> the type of the inner map keys
   * @param <V2> the type of the inner map values
   * @param sb the destination for the string representation
   * @param indent how many spaces to indent each line of output
   * @param header what to print before each inner map
   * @param freqInfo what to print
   */
  static <K1 extends @Signed Object, K2 extends @Signed Object, V2 extends @Signed Object>
      void formatFrequencyInfo(
          StringBuilder sb, String indent, String header, Map<K1, Map<K2, V2>> freqInfo) {
    for (Map.Entry<K1, Map<K2, V2>> entry : freqInfo.entrySet()) {
      sb.append(indent);
      sb.append(header);
      sb.append(entry.getKey());
      sb.append(System.lineSeparator());
      formatFrequencyMap(sb, indent + "  ", entry.getValue());
    }
  }

  @Override
  public String toString() {
    throw new Error("TODO");
  }
}
