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
public class ScopeToScopeStatistics {

  /** A special key representing the "all" scope. */
  public static final Object ALL_SCOPE = "ALL_SCOPE";

  /**
   * A map from a specific scope to its constant statistics. It contains each constant's number of
   * times it is used, the number of classes it is contained, and the number of classes within the
   * given scope. A scope may be a class, package, or {@link ScopeToScopeStatistics#ALL_SCOPE}.
   */
  private Map<@Nullable Object, ScopeStatistics> scopeStatisticsMap;

  /** Creates a ScopeToScopeStatistics. */
  public ScopeToScopeStatistics() {
    scopeStatisticsMap = new HashMap<>();
  }

  /**
   * Register uses of the given constant. Creates an entry or increments an existing entry.
   *
   * @param type the type of the class
   * @param seq the sequence to be added
   * @param numUses the number of times the sequence is used
   */
  public void incrementNumUses(ClassOrInterfaceType type, Sequence seq, int numUses) {
    scopeStatisticsMap
        .computeIfAbsent(getScope(type), __ -> new ScopeStatistics())
        .incrementNumUses(seq, numUses);
  }

  /**
   * Register the number of classes that use the given constant. Creates an entry or increments an
   * existing entry.
   *
   * @param type the type of the class
   * @param seq the sequence to be added
   * @param numClassesWithConstant the number of classes that contain the sequence
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
   * Register classes. Creates an entry or increments an existing entry.
   *
   * @param type the type of the class
   * @param numClasses the number of classes
   */
  public void incrementNumClasses(ClassOrInterfaceType type, int numClasses) {
    if (GenInputsAbstract.literals_level == ClassLiteralsMode.CLASS) {
      throw new RuntimeException("Should not update totalClasses in CLASS level");
    }
    scopeStatisticsMap
        .computeIfAbsent(getScope(type), __ -> new ScopeStatistics())
        .incrementNumClasses(numClasses);
  }

  /**
   * Returns all sequences that had been recorded under the specific scope, which are the constants
   * extracted by constant mining.
   *
   * @param scope a class, package, or the "all" scope
   * @return the sequences in the scope
   */
  public Set<Sequence> getSequencesForScope(@Nullable Object scope) {
    ScopeStatistics stats = scopeStatisticsMap.get(scope);
    if (stats == null) {
      Log.logPrintf("Scope %s is not a key in scopeStatisticsMap%n", scope);
      return Collections.emptySet();
    }

    return stats.getSequenceSet();
  }

  /**
   * Returns a map from a scope to a map from every constant to its total number in the scope
   *
   * @return the map from every scope to a map from each constant to its total number of uses in the
   *     scope
   */
  public Map<Object, Map<Sequence, Integer>> getNumUses() {
    Map<Object, Map<Sequence, Integer>> res = new HashMap<>();
    scopeStatisticsMap.forEach((scope, stats) -> res.put(scope, stats.getNumUses()));
    return res;
  }

  /**
   * Returns the map from every constant to the number of times it is used in the given scope.
   *
   * @param scope a type, a package, or the "all" scope
   * @return the map from every constant to the number of times it is used in the given scope
   */
  public Map<Sequence, Integer> getNumUses(@Nullable Object scope) {
    ScopeStatistics stats = scopeStatisticsMap.get(scope);
    if (stats == null) {
      Log.logPrintf("Scope %s is not a key in scopeStatisticsMap%n", scope);
      return Collections.emptyMap();
    }
    return stats.getNumUses();
  }

  /**
   * Returns a map from a scrope to a map from every constant to the number of classes in the scopes
   * that use it.
   *
   * @return a map from a scrope to a map from every constant to the number of classes in the scopes
   *     that use it
   */
  public Map<Object, Map<Sequence, Integer>> getNumClassesWith() {
    Map<Object, Map<Sequence, Integer>> res = new HashMap<>();
    scopeStatisticsMap.forEach((scope, stats) -> res.put(scope, stats.getNumClassesWith()));
    return res;
  }

  /**
   * Returns the map from every constant to the number of classes in the given scope that contains
   * it.
   *
   * @param scope a scope
   * @return the map from every constant to the number of classes in the given scope that contains
   *     it
   */
  public Map<Sequence, Integer> getNumClassesWith(@Nullable Object scope) {
    if (GenInputsAbstract.literals_level == ClassLiteralsMode.CLASS) {
      throw new RandoopBug("Should not get numClassesWith in CLASS level");
    }
    ScopeStatistics stats = scopeStatisticsMap.get(scope);
    if (stats == null) {
      Log.logPrintf("Scope %s is not a key in scopeStatisticsMap.%n", scope);
      return Collections.emptyMap();
    }
    return stats.getNumClassesWith();
  }

  /**
   * Returns the number of classes in the given scope.
   *
   * @param scope a scope
   * @return the number of classes in the given scope
   */
  public Integer getTotalClassesInScope(@Nullable Object scope) {
    ScopeStatistics stats = scopeStatisticsMap.get(scope);
    // The default value is null to avoid when scope is java.lang or other standard libraries
    if (stats == null) {
      throw new RandoopBug(String.format("Scope %s is not a key in scopeStatisticsMap.%n", scope));
    }
    return stats.getNumClasses();
  }

  /**
   * Outputs a string representation of the map to the given StringBuilder.
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
   * Outputs a string representation of the number of uses to the given StringBuilder.
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
   * Returns the scope for the given type.
   *
   * @param type the type of the class
   * @return the scope for the given type
   */
  public static @Nullable Object getScope(ClassOrInterfaceType type) {
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

    sb.append("Number of uses");
    sb.append(System.lineSeparator());
    ScopeToScopeStatistics.formatMapMap(
        sb, "  ", GenInputsAbstract.literals_level.toString(), getNumUses());
    sb.append("Number of classes in scope");
    sb.append(System.lineSeparator());
    ScopeToScopeStatistics.formatMapMap(
        sb, "  ", GenInputsAbstract.literals_level.toString(), getNumClassesWith());

    return sb.toString();
  }
}
