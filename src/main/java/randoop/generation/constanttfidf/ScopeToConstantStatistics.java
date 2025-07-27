package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signedness.qual.Signed;
import org.plumelib.util.CollectionsPlume;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;

/** This class stores information about the constants used in the SUT. */
public class ScopeToConstantStatistics {

  /** A special key representing the "all" scope. */
  public static final Object ALL_SCOPE = "ALL_SCOPE";

  /** A map from a specific scope to its constant statistics. */
  // Declared as HashMap rather than as Map because some Map implementations prohibit null keys.
  private HashMap<@Nullable Object, ConstantStatistics> scopeStatisticsMap = new HashMap<>();

  /** Creates a ScopeToConstantStatistics. */
  public ScopeToConstantStatistics() {}

  /**
   * Return information about constants in a specific scope.
   *
   * @param type the type whose scope to access
   * @return information about constants in the scope for {@code type}
   */
  private ConstantStatistics getConstantStatistics(ClassOrInterfaceType type) {
    return scopeStatisticsMap.computeIfAbsent(getScope(type), __ -> new ConstantStatistics());
  }

  /**
   * Register uses of the given constant. Creates an entry or increments an existing entry.
   *
   * @param type the class whose scope is being updated
   * @param seq the sequence to be added
   * @param numUses the number of times the {@code seq} is used in {@code type}
   */
  public void incrementNumUses(ClassOrInterfaceType type, Sequence seq, int numUses) {
    getConstantStatistics(type).incrementNumUses(seq, numUses);
  }

  /**
   * Register the number of classes that use the given constant. Creates an entry or increments an
   * existing entry.
   *
   * @param type the class whose scope is being updated
   * @param seq the sequence to be added
   * @param numClassesWithConstant the number of classes that contain the sequence
   */
  public void incrementNumClassesWith(
      ClassOrInterfaceType type, Sequence seq, int numClassesWithConstant) {
    getConstantStatistics(type).incrementNumClassesWith(seq, numClassesWithConstant);
  }

  /**
   * Register classes. Creates an entry or increments an existing entry.
   *
   * @param type the class whose scope is being updated
   * @param numClasses the number of classes
   */
  public void incrementNumClasses(ClassOrInterfaceType type, int numClasses) {
    getConstantStatistics(type).incrementNumClasses(numClasses);
  }

  /**
   * Returns all sequences that had been recorded under the specific scope, which are the constants
   * extracted by constant mining.
   *
   * @param scope a class, package, or the "all" scope
   * @return the sequences in the scope
   */
  public Set<Sequence> getSequencesForScope(@Nullable @KeyFor("scopeStatisticsMap") Object scope) {
    return scopeStatisticsMap.get(scope).getSequenceSet();
  }

  /**
   * Returns the number of uses map for the given scope.
   *
   * @param scope a scope
   * @return the number of uses map for the given scope
   */
  public Map<Sequence, Integer> getNumUsesMap(Object scope) {
    return scopeStatisticsMap.get(scope).getNumUses();
  }

  /**
   * Returns the number of classes with constant map for the given scope.
   *
   * @param scope a scope
   * @return the number of classes with constant map for the given scope
   */
  public Map<Sequence, Integer> getNumClassesWithMap(Object scope) {
    return scopeStatisticsMap.get(scope).getNumClassesWith();
  }

  /**
   * Returns the number of classes for the given scope.
   *
   * @param scope a scope
   * @return the number of classes for the given scope
   */
  public Integer getNumClasses(Object scope) {
    return scopeStatisticsMap.get(scope).getNumClasses();
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
    HashMap<Object, Map<Sequence, Integer>> numUsesMap = new HashMap<>();
    scopeStatisticsMap.forEach((scope, stats) -> numUsesMap.put(scope, stats.getNumUses()));
    ScopeToConstantStatistics.formatMapMap(
        sb, "  ", GenInputsAbstract.literals_level.toString(), numUsesMap);
    sb.append("Number of classes in scope");
    sb.append(System.lineSeparator());
    HashMap<Object, Map<Sequence, Integer>> numClassesWithMap = new HashMap<>();
    scopeStatisticsMap.forEach(
        (scope, stats) -> numClassesWithMap.put(scope, stats.getNumClassesWith()));
    ScopeToConstantStatistics.formatMapMap(
        sb, "  ", GenInputsAbstract.literals_level.toString(), numClassesWithMap);

    return sb.toString();
  }

  // TODO: Move this method to plume-util?
  /**
   * Outputs a string representation of the number of uses to the given StringBuilder.
   *
   * @param <K1> the type of the outer map keys
   * @param <K2> the type of the inner map keys
   * @param <V2> the type of the inner map values
   * @param sb the destination for the string representation
   * @param indent how many spaces to indent each line of output
   * @param innerHeader what to print before each inner map
   * @param mapMap the map to print
   */
  static <
          K1 extends @Nullable @Signed Object,
          K2 extends @Nullable @Signed Object,
          V2 extends @Nullable @Signed Object>
      void formatMapMap(
          StringBuilder sb, String indent, String innerHeader, Map<K1, Map<K2, V2>> mapMap) {
    if (mapMap.isEmpty()) {
      return;
    }

    for (Map.Entry<K1, Map<K2, V2>> entry : mapMap.entrySet()) {
      sb.append(indent);
      sb.append(innerHeader);
      sb.append(entry.getKey());
      sb.append(System.lineSeparator());
      CollectionsPlume.mapToStringMultiLine(sb, entry.getValue(), indent + "  ");
    }
  }

  /** Information about a scope. */
  public static class ScopeInfo {
    /** the number of times each sequence is used in the scope */
    public final Map<Sequence, Integer> numUsesMap;

    /** A map from a constant to the number of classes in the current scope that contains it. */
    public final Map<Sequence, Integer> classMap;

    /** The number of classes in the current scope. */
    public final Integer classCount;

    /**
     * Creates a ScopeInfo.
     *
     * @param numUsesMap a map from each sequence to the number of times it is used in the scope
     * @param classMap a map from each sequence to the number of classes in the scope that contains
     *     it
     * @param classCount the number of classes in the scope
     */
    public ScopeInfo(
        Map<Sequence, Integer> numUsesMap, Map<Sequence, Integer> classMap, Integer classCount) {
      this.numUsesMap = numUsesMap;
      this.classMap = classMap;
      this.classCount = classCount;
    }
  }
}
