package randoop.generation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.signedness.qual.Signed;
import randoop.sequence.Sequence;
import randoop.util.Log;

/**
 * This class stores constant mining information by the given literals level. T is the scope of the
 * constant mining, which can be ClassOrInterfaceType, Package, or Object, which corresponds to
 * users' input about literal level as CLASS, PACKAGE, or ALL. The scope statistics stores the
 * information about constants within the given scope.
 *
 * @param <T> the scope of the constant mining
 */
public class ConstantMiningStatistics<T extends @Signed Object> {

  /**
   * A map from a specific scope to its constant statistics. It contains each constant's number of
   * times it is used, the number of classes it is contained, and the number of classes within the
   * given scope.
   */
  Map<T, ScopeStatistics> scopeStatisticsMap;

  /** Creates a new ConstantMiningStatistics with scopeStatistics. */
  public ConstantMiningStatistics() {
    scopeStatisticsMap = new HashMap<>();
  }

  /**
   * Add and update the frequency of the sequence to the current scope.
   *
   * @param t the scope of the constant mining
   * @param seq the sequence to be added
   * @param frequency the frequency of the sequence to be added
   */
  public void addFrequency(T t, Sequence seq, int frequency) {
    scopeStatisticsMap.computeIfAbsent(t, __ -> new ScopeStatistics()).addFrequency(seq, frequency);
    ;
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
    scopeStatisticsMap
        .computeIfAbsent(t, __ -> new ScopeStatistics())
        .addToClassWithConstantInfo(seq, numClassesWithConstant);
  }

  /**
   * Add and update the numClasses of the current scope.
   *
   * @param t the scope of the constant mining
   * @param numClasses the total number of classes in the current scope
   */
  public void addToTotalClasses(T t, int numClasses) {
    scopeStatisticsMap
        .computeIfAbsent(t, __ -> new ScopeStatistics())
        .addToTotalClasses(numClasses);
  }

  /**
   * Get all sequences that had been recorded under the specific scope, which are the constants
   * extracted by constant mining.
   *
   * @param t the specific package, class, or null
   * @return the set of sequences that have been recorded under the specific scope
   */
  public Set<Sequence> getSequencesForScope(T t) {
    if (!scopeStatisticsMap.containsKey(t)) {
      Log.logPrintf("The scope %s is not found in the frequency information", t);
      return new HashSet<>();
    }

    return scopeStatisticsMap.get(t).getSequenceSet();
  }

  /**
   * Get the complete frequency information of the current scope.
   *
   * @return the frequency information of the current scope
   */
  public Map<T, Map<Sequence, Integer>> getFrequencyInfo() {
    Map<T, Map<Sequence, Integer>> res = new HashMap<>();
    scopeStatisticsMap.forEach((key, value) -> res.put(key, value.getNumUses()));
    return res;
  }

  /**
   * Get the frequency information of the given type or package.
   *
   * @param t a type or package
   * @return the frequency information of the given type or package
   */
  public Map<Sequence, Integer> getFrequencyInfo(T t) {
    return scopeStatisticsMap.get(t).getNumUses();
  }

  /**
   * Get the complete classesWithConstantInfo information of the current scope.
   *
   * @return the classesWithConstantInfo information of the current scope
   */
  public Map<T, Map<Sequence, Integer>> getClassesWithConstantInfo() {
    Map<T, Map<Sequence, Integer>> res = new HashMap<>();
    scopeStatisticsMap.forEach((key, value) -> res.put(key, value.getClassesWithConstantInfo()));
    return res;
  }

  /**
   * Get the classesWithConstantInfo information of the specific type.
   *
   * @param t the specific scope
   * @return the classesWithConstantInfo information of the specific type
   */
  public Map<Sequence, Integer> getClassesWithConstantForType(T t) {
    return scopeStatisticsMap.get(t).getClassesWithConstantInfo();
  }

  /**
   * Get the numClasses information of the specific type.
   *
   * @param t the specific type
   * @return the numClasses information of the specific type
   */
  public Integer getTotalClassesInScope(T t) {
    // The default value is null to avoid when t is java.lang or other standard libraries
    if (!scopeStatisticsMap.containsKey(t)) {
      return null;
    }
    return scopeStatisticsMap.get(t).getNumClasses();
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
