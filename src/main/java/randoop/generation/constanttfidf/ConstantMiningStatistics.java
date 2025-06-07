package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
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

  /** Creates a ConstantMiningStatistics. */
  public ConstantMiningStatistics() {
    scopeStatisticsMap = new HashMap<>();
  }

  /**
   * Add and update the frequency of the sequence to the current scope.
   *
   * @param scope the scope of the constant mining
   * @param seq the sequence to be added
   * @param frequency the frequency of the sequence to be added
   */
  public void addUses(T scope, Sequence seq, int frequency) {
    scopeStatisticsMap.computeIfAbsent(scope, __ -> new ScopeStatistics()).addUses(seq, frequency);
  }

  /**
   * Add and update the numClassesWith of the sequence to the current scope.
   *
   * @param scope the scope of the constant mining
   * @param seq the sequence to be added
   * @param numClassesWithConstant the number of classes in the current scope that contain the
   *     sequence to be added
   */
  public void addToNumClassesWith(T scope, Sequence seq, int numClassesWithConstant) {
    scopeStatisticsMap
        .computeIfAbsent(scope, __ -> new ScopeStatistics())
        .addClassesWith(seq, numClassesWithConstant);
  }

  /**
   * Add and update the numClasses of the current scope.
   *
   * @param scope the scope of the constant mining
   * @param numClasses the total number of classes in the current scope
   */
  public void addToTotalClasses(T scope, int numClasses) {
    scopeStatisticsMap
        .computeIfAbsent(scope, __ -> new ScopeStatistics())
        .addToTotalClasses(numClasses);
  }

  /**
   * Get all sequences that had been recorded under the specific scope, which are the constants
   * extracted by constant mining.
   *
   * @param scope the specific package, class, or null (a package can be null too)
   * @return the set of sequences that have been recorded under the specific scope
   */
  public Set<Sequence> getSequencesForScope(@Nullable T scope) {
    ScopeStatistics stats = scopeStatisticsMap.get(scope);
    if (stats == null) {
      Log.logPrintf("The scope %s is not found in the frequency information", scope);
      return new HashSet<>();
    }

    return stats.getSequenceSet();
  }

  /**
   * Get the frequency information for all scopes.
   *
   * @return the frequency information for all scopes
   */
  public Map<T, Map<Sequence, Integer>> getNumUses() {
    Map<T, Map<Sequence, Integer>> res = new HashMap<>();
    scopeStatisticsMap.forEach((key, value) -> res.put(key, value.getNumUses()));
    return res;
  }

  /**
   * Get the frequency information of the given scope.
   *
   * @param scope a type, a package, or null
   * @return the frequency information of the given scope
   */
  public Map<Sequence, Integer> getNumUses(T scope) {
    return scopeStatisticsMap.get(scope).getNumUses();
  }

  /**
   * Get the numClassesWith information for all scopes.
   *
   * @return the numClassesWith information for all scopes
   */
  public Map<T, Map<Sequence, Integer>> getNumClassesWith() {
    Map<T, Map<Sequence, Integer>> res = new HashMap<>();
    scopeStatisticsMap.forEach((key, value) -> res.put(key, value.getNumClassesWith()));
    return res;
  }

  /**
   * Get the numClassesWith information of the specific scope.
   *
   * @param scope the specific scope
   * @return the numClassesWith information of the specific scope
   */
  public Map<Sequence, Integer> getNumClassesWith(T scope) {
    return scopeStatisticsMap.get(scope).getNumClassesWith();
  }

  /**
   * Get the numClasses information of the specific scope.
   *
   * @param scope the specific scope
   * @return the numClasses information of the specific scope
   */
  public Integer getTotalClassesInScope(T scope) {
    // The default value is null to avoid when scope is java.lang or other standard libraries
    if (!scopeStatisticsMap.containsKey(scope)) {
      return null;
    }
    return scopeStatisticsMap.get(scope).getNumClasses();
  }

  /**
   * Outputs a string representation of the map to the StringBuilder.
   *
   * @param   <K2> the type of the map keys
   * @param   <V2> the type of the map values
   * @param sb the destination for the string representation
   * @param indent how many spaces to indent each line of output
   * @param map the map to print
   */
  static <K2 extends @Signed Object, V2 extends @Signed Object> void formatMap(
      StringBuilder sb, String indent, Map<K2, V2> map) {
    for (Map.Entry<K2, V2> entry : map.entrySet()) {
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
   * @param mapMap what to print
   */
  static <K1 extends @Signed Object, K2 extends @Signed Object, V2 extends @Signed Object>
      void formatMapMap(
          StringBuilder sb, String indent, String header, Map<K1, Map<K2, V2>> mapMap) {
    for (Map.Entry<K1, Map<K2, V2>> entry : mapMap.entrySet()) {
      sb.append(indent);
      sb.append(header);
      sb.append(entry.getKey());
      sb.append(System.lineSeparator());
      formatMap(sb, indent + "  ", entry.getValue());
    }
  }

  @Override
  public String toString() {
    throw new Error("TODO");
  }
}
