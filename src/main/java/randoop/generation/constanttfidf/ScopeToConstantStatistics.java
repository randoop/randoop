package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;
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
   * Records that a class contains the given sequences and increments the total class count.
   *
   * @param type the class whose scope is being updated
   * @param sequences the sequences that exist in this class
   */
  public void incrementClassesWithSequences(
      ClassOrInterfaceType type, java.util.Collection<Sequence> sequences) {
    ConstantStatistics stats = getConstantStatistics(type);

    for (Sequence seq : sequences) {
      stats.incrementNumClassesWith(seq, 1);
    }

    stats.incrementNumClasses(1);
  }

  /**
   * Returns all sequences that have been recorded under the specific scope.
   *
   * @param scope a class, package, or the "all" scope
   * @return the sequences in the scope
   */
  public Set<Sequence> getSequences(@Nullable @KeyFor("scopeStatisticsMap") Object scope) {
    return scopeStatisticsMap.get(scope).getSequenceSet();
  }

  /**
   * Returns the constant statistics for the given scope.
   *
   * @param scope a scope
   * @return the constant statistics for the given scope
   */
  public ConstantStatistics getConstantStatistics(
      @Nullable @KeyFor("scopeStatisticsMap") Object scope) {
    return scopeStatisticsMap.get(scope);
  }

  /**
   * Returns the number of classes for the given scope.
   *
   * @param scope a scope
   * @return the number of classes for the given scope
   */
  public int getNumClasses(@Nullable @KeyFor("scopeStatisticsMap") Object scope) {
    return scopeStatisticsMap.get(scope).getNumClasses();
  }

  /**
   * Returns the scope for the given type.
   *
   * @param type a type
   * @return the scope for the given type
   */
  // This is not static so the result is @KeyFor("scopeStatisticsMap").
  public @Nullable @KeyFor("scopeStatisticsMap") Object getScope(ClassOrInterfaceType type) {
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

    for (Map.Entry<@Nullable Object, ConstantStatistics> scopeEntry :
        scopeStatisticsMap.entrySet()) {
      Object scope = scopeEntry.getKey();
      ConstantStatistics stats = scopeEntry.getValue();

      sb.append("Scope: ")
          .append(scope)
          .append(" (")
          .append(stats.getNumClasses())
          .append(" classes)")
          .append(System.lineSeparator());

      for (Map.Entry<Sequence, ConstantStatistics.ConstantUses> constantEntry :
          stats.getConstantUses().entrySet()) {
        Sequence sequence = constantEntry.getKey();
        ConstantStatistics.ConstantUses constantStats = constantEntry.getValue();

        sb.append("  ")
            .append(sequence)
            .append(" -> uses: ")
            .append(constantStats.getNumUses())
            .append(", classes: ")
            .append(constantStats.getNumClassesWith())
            .append(System.lineSeparator());
      }
      sb.append(System.lineSeparator());
    }

    return sb.toString();
  }
}
