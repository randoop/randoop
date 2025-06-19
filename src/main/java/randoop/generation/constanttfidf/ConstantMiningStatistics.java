package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.main.GenInputsAbstract;
import randoop.main.GenInputsAbstract.ClassLiteralsMode;
import randoop.main.RandoopBug;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Log;

/** This class stores constant mining information. */
public class ConstantMiningStatistics {

  /** A special key representing the "all" scope. */
  public static final Object ALL = "ALL_SCOPE";

  /**
   * A map from a specific scope to its constant statistics. It contains each constant's number of
   * times it is used, the number of classes it is contained, and the number of classes within the
   * given scope. A scope may be a class, package, or null (for "all").
   */
  private Map<Object, ScopeStatistics> scopeStatisticsMap;

  /** Creates a ConstantMiningStatistics. */
  public ConstantMiningStatistics() {
    scopeStatisticsMap = new HashMap<>();
  }

  /**
   * Add and update the frequency of the sequence to the current scope.
   *
   * @param type the type of the class
   * @param seq the sequence to be added
   * @param frequency the frequency of the sequence to be added
   */
  public void addUses(Object type, Sequence seq, int frequency) {
    scopeStatisticsMap
        .computeIfAbsent(getScope(type), __ -> new ScopeStatistics())
        .addUses(seq, frequency);
  }

  /**
   * Add and update the numClassesWith of the sequence to the current scope.
   *
   * @param type the type of the class
   * @param seq the sequence to be added
   * @param numClassesWithConstant the number of classes in the current scope that contain the
   *     sequence to be added
   */
  public void addToNumClassesWith(Object type, Sequence seq, int numClassesWithConstant) {
    if (GenInputsAbstract.literals_level == ClassLiteralsMode.CLASS) {
      throw new RuntimeException("Should not update numClassesWith in CLASS level");
    }
    scopeStatisticsMap
        .computeIfAbsent(getScope(type), __ -> new ScopeStatistics())
        .addClassesWith(seq, numClassesWithConstant);
  }

  /**
   * Add and update the numClasses of the current scope.
   *
   * @param type the type of the class
   * @param numClasses the total number of classes in the current scope
   */
  public void addToTotalClasses(Object type, int numClasses) {
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
  public Map<Object, Map<Sequence, Integer>> getNumUses() {
    Map<Object, Map<Sequence, Integer>> res = new HashMap<>();
    scopeStatisticsMap.forEach((key, value) -> res.put(key, value.getNumUses()));
    return res;
  }

  /**
   * Get the frequency information of the given scope.
   *
   * @param scope a type, a package, or the "all" scope
   * @return the frequency information of the given scope
   */
  public Map<Sequence, Integer> getNumUses(Object scope) {
    return scopeStatisticsMap.get(scope).getNumUses();
  }

  /**
   * Get the numClassesWith information for all scopes.
   *
   * @return the numClassesWith information for all scopes
   */
  public Map<Object, Map<Sequence, Integer>> getNumClassesWith() {
    Map<Object, Map<Sequence, Integer>> res = new HashMap<>();
    scopeStatisticsMap.forEach((key, value) -> res.put(key, value.getNumClassesWith()));
    return res;
  }

  /**
   * Get the numClassesWith information of the specific scope.
   *
   * @param scope the specific scope
   * @return the numClassesWith information of the specific scope
   */
  public Map<Sequence, Integer> getNumClassesWith(Object scope) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        throw new RandoopBug("Should not get classesWithConstant in CLASS level");
      case PACKAGE:
        return scopeStatisticsMap.get((Package) scope).getNumClassesWith();
      case ALL:
        return getNumClassesWith().get(ALL);
      default:
        throw new RandoopBug("Unexpected literals level: " + GenInputsAbstract.literals_level);
    }
  }

  /**
   * Get the numClasses information of the specific scope.
   *
   * @param scope the specific scope
   * @return the numClasses information of the specific scope
   */
  public Integer getTotalClassesInScope(@Nullable Object scope) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        throw new RandoopBug("Should not get totalClasses in CLASS level");
      case PACKAGE:
        if (scope == null) {
          throw new RandoopBug("literals_level is PACKAGE and scope is null");
        }
        break;
      case ALL:
        if (scope != null) {
          throw new RandoopBug("literals_level is ALL and scope is " + scope);
        }
        break;
      default:
        throw new RandoopBug("Unexpected literals level: " + GenInputsAbstract.literals_level);
    }
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
  static <K2, V2> void formatMap(StringBuilder sb, String indent, Map<K2, V2> map) {
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
  static <K1, K2, V2> void formatMapMap(
      StringBuilder sb, String indent, String header, Map<K1, Map<K2, V2>> mapMap) {
    for (Map.Entry<K1, Map<K2, V2>> entry : mapMap.entrySet()) {
      sb.append(indent);
      sb.append(header);
      sb.append(entry.getKey());
      sb.append(System.lineSeparator());
      formatMap(sb, indent + "  ", entry.getValue());
    }
  }

  public Object getScope(Object type) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        return (ClassOrInterfaceType) type;
      case PACKAGE:
        return ((ClassOrInterfaceType) type).getPackage();
      case ALL:
        return ALL;
      default:
        throw new RandoopBug("Unexpected literals level: " + GenInputsAbstract.literals_level);
    }
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        sb.append("Class Level");
        sb.append(System.lineSeparator());
        sb.append("Class Frequency Map");
        sb.append(System.lineSeparator());
        ConstantMiningStatistics.formatMapMap(sb, "  ", "class=", getNumUses());
        break;
      case PACKAGE:
        sb.append("Package Level");
        sb.append(System.lineSeparator());
        sb.append("Package Frequency Map");
        sb.append(System.lineSeparator());
        ConstantMiningStatistics.formatMapMap(sb, "  ", "package=", getNumUses());
        sb.append("Package classWithConstant Map");
        sb.append(System.lineSeparator());
        ConstantMiningStatistics.formatMapMap(sb, "  ", "class=", getNumClassesWith());
        break;
      case ALL:
        sb.append("All Level");
        sb.append(System.lineSeparator());
        sb.append("Global Frequency Map");
        sb.append(System.lineSeparator());
        ConstantMiningStatistics.formatMap(sb, "  ", getNumUses().get(ALL));
        sb.append("Global classesWithConstants Map");
        sb.append(System.lineSeparator());
        ConstantMiningStatistics.formatMap(sb, "  ", getNumClassesWith().get(ALL));
        break;
      default:
        throw new RandoopBug("Unexpected literals level: " + GenInputsAbstract.literals_level);
    }

    return sb.toString();
  }
}
