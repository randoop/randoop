package randoop.generation.constanttfidf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signedness.qual.Signed;
import randoop.main.GenInputsAbstract;
import randoop.main.GenInputsAbstract.ClassLiteralsMode;
import randoop.main.RandoopBug;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Log;

/** This class stores constant mining information. */
public class ConstantMiningStatistics {

  /** A special key representing the "all" scope. */
  public static final Object ALL_SCOPE = "ALL_SCOPE";

  /**
   * A map from a specific scope to its constant statistics. It contains each constant's number of
   * times it is used, the number of classes it is contained, and the number of classes within the
   * given scope. A scope may be a class, package, or the ALL_SCOPE (for "all").
   */
  private Map<Object, ScopeStatistics> scopeStatisticsMap;

  /** Creates a ConstantMiningStatistics. */
  public ConstantMiningStatistics() {
    scopeStatisticsMap = new HashMap<>();
  }

  /**
   * Add and update the number of times the sequence is used to the current scope.
   *
   * @param type the type of the class
   * @param seq the sequence to be added
   * @param frequency the number of times the sequence is used to be added
   */
  public void addUses(ClassOrInterfaceType type, Sequence seq, int frequency) {
    scopeStatisticsMap
        .computeIfAbsent(getScope(type), __ -> new ScopeStatistics())
        .addUses(seq, frequency);
  }

  /**
   * Add and update the number of classes the sequence is contained to the current scope.
   *
   * @param type the type of the class
   * @param seq the sequence to be added
   * @param numClassesWithConstant the number of classes in the current scope that contain the
   *     sequence to be added
   */
  public void addToNumClassesWith(
      ClassOrInterfaceType type, Sequence seq, int numClassesWithConstant) {
    if (GenInputsAbstract.literals_level == ClassLiteralsMode.CLASS) {
      throw new RuntimeException("Should not update numClassesWith in CLASS level");
    }
    scopeStatisticsMap
        .computeIfAbsent(getScope(type), __ -> new ScopeStatistics())
        .addClassesWith(seq, numClassesWithConstant);
  }

  /**
   * Add and update the total number of classes in the current scope.
   *
   * @param type the type of the class
   * @param numClasses the total number of classes in the current scope
   */
  public void addToTotalClasses(ClassOrInterfaceType type, int numClasses) {
    if (GenInputsAbstract.literals_level == ClassLiteralsMode.CLASS) {
      throw new RuntimeException("Should not update totalClasses in CLASS level");
    }
    scopeStatisticsMap
        .computeIfAbsent(getScope(type), __ -> new ScopeStatistics())
        .addToTotalClasses(numClasses);
  }

  /**
   * Get all sequences that had been recorded under the specific scope, which are the constants
   * extracted by constant mining.
   *
   * @param scope the specific package, class, or the "all" scope
   * @return the set of sequences that have been recorded under the specific scope
   */
  public Set<Sequence> getSequencesForScope(Object scope) {
    ScopeStatistics stats = scopeStatisticsMap.get(scope);
    if (stats == null) {
      Log.logPrintf("Scope %s is not a key in scopeStatisticsMap%n", scope);
      return Collections.emptySet();
    }

    return stats.getSequenceSet();
  }

  /**
   * Get the map from every constant to the number of times it is used under all scopes.
   *
   * @return the map from every constant to the number of times it is used under all scopes
   */
  public Map<Object, Map<Sequence, Integer>> getNumUses() {
    Map<Object, Map<Sequence, Integer>> res = new HashMap<>();
    scopeStatisticsMap.forEach((key, value) -> res.put(key, value.getNumUses()));
    return res;
  }

  /**
   * Get the map from every constant to the number of times it is used under the given scope.
   *
   * @param scope a type, a package, or the "all" scope
   * @return the map from every constant to the number of times it is used under the given scop
   */
  public Map<Sequence, Integer> getNumUses(Object scope) {
    return scopeStatisticsMap.get(scope).getNumUses();
  }

  /**
   * Get the map from every constant to the number of classes in all scopes that contains it.
   *
   * @return the map from every constant to the number of classes in all scopes that contains it
   */
  public Map<Object, Map<Sequence, Integer>> getNumClassesWith() {
    Map<Object, Map<Sequence, Integer>> res = new HashMap<>();
    scopeStatisticsMap.forEach((key, value) -> res.put(key, value.getNumClassesWith()));
    return res;
  }

  /**
   * Get the map from every constant to the number of classes in the given scope that contains it.
   *
   * @param scope a scope
   * @return the map from every constant to the number of classes in the given scope that contains
   *     it
   */
  public Map<Sequence, Integer> getNumClassesWith(Object scope) {
    if (GenInputsAbstract.literals_level == ClassLiteralsMode.CLASS) {
      throw new RandoopBug("Should not get numClassesWith in CLASS level");
    }
    return getNumClassesWith().get(scope);
  }

  /**
   * Get the number of classes in the given scope.
   *
   * @param scope a scope
   * @return the number of classes in the given scope
   */
  public Integer getTotalClassesInScope(@Nullable Object scope) {
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
    if (mapMap.isEmpty()) {
      return;
    }

    for (Map.Entry<K1, Map<K2, V2>> entry : mapMap.entrySet()) {
      sb.append(indent);
      sb.append(header);
      sb.append(entry.getKey());
      sb.append(System.lineSeparator());
      formatMap(sb, indent + "  ", entry.getValue());
    }
  }

  /**
   * Get the scope for the given type based on the literals level.
   *
   * @param type the type of the class
   * @return the scope for the given type
   */
  public static Object getScope(@Nullable ClassOrInterfaceType type) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        return type;
      case PACKAGE:
        return type.getPackage();
      case ALL:
        return ALL_SCOPE;
      default:
        throw new RandoopBug("Unexpected literals level: " + GenInputsAbstract.literals_level);
    }
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("Frequency Map");
    sb.append(System.lineSeparator());
    ConstantMiningStatistics.formatMapMap(
        sb, "  ", GenInputsAbstract.literals_level.toString(), getNumUses());
    sb.append("ClassWithConstant Map");
    sb.append(System.lineSeparator());
    ConstantMiningStatistics.formatMapMap(
        sb, "  ", GenInputsAbstract.literals_level.toString(), getNumClassesWith());

    return sb.toString();
  }
}
