package randoop.generation.constanttfidf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.SIList;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;

/** This class stores information about the constants used in the SUT. */
public class ScopeToConstantStatistics {

  /** A special key representing the "all" scope. */
  public static final Object ALL_SCOPE = "ALL_SCOPE";

  /**
   * A map from a specific scope to its constant statistics. A null key represents the unnamed
   * package.
   */
  private LinkedHashMap<@Nullable Object, ConstantStatistics> scopeToStatisticsMap =
      new LinkedHashMap<>();

  /** Creates a ScopeToConstantStatistics. */
  public ScopeToConstantStatistics() {}

  /**
   * Returns information about constants in a specific scope.
   *
   * @param type the type whose scope to access
   * @return information about constants in the scope for {@code type}, including superclass
   *     constants
   */
  public ConstantStatistics getConstantStatistics(ClassOrInterfaceType type) {
    return scopeToStatisticsMap.computeIfAbsent(getScope(type), __ -> new ConstantStatistics());
  }

  /**
   * Returns sequences for a type, including sequences from all supertypes (classes and interfaces),
   * filtered by the desired type.
   *
   * @param type the type to get sequences for
   * @param neededType the type to filter sequences by
   * @return sequences for the type and its supertypes that match the needed type
   */
  public SIList<Sequence> getSequencesIncludingSupertypes(
      ClassOrInterfaceType type, randoop.types.Type neededType) {
    LinkedHashSet<ClassOrInterfaceType> typesToVisit = new LinkedHashSet<>();
    typesToVisit.add(type);
    typesToVisit.addAll(type.getSuperTypes());

    List<SIList<Sequence>> resultLists = new ArrayList<>();
    for (ClassOrInterfaceType t : typesToVisit) {
      SIList<Sequence> typeSequences = getConstantStatistics(t).getSequencesForType(neededType);
      if (!typeSequences.isEmpty()) {
        resultLists.add(typeSequences);
      }
    }

    return SIList.concat(resultLists);
  }

  /**
   * Returns all sequences from all scopes.
   *
   * @return all sequences recorded in this statistics object
   */
  public Set<Sequence> getAllSequences() {
    Set<Sequence> allSequences = new LinkedHashSet<>();
    for (ConstantStatistics stats : scopeToStatisticsMap.values()) {
      allSequences.addAll(stats.getConstantUses().keySet());
    }
    return allSequences;
  }

  /**
   * Registers uses of the given constant. Creates an entry or increments an existing entry.
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
   * @param sequences all the constant sequences in the class
   */
  public void incrementClassesWithSequences(
      ClassOrInterfaceType type, Collection<Sequence> sequences) {
    ConstantStatistics stats = getConstantStatistics(type);
    for (Sequence seq : sequences) {
      stats.incrementNumClassesWith(seq, 1);
    }
    stats.incrementNumClasses(1);
  }

  /**
   * Returns the scope for the given type.
   *
   * @param type a type
   * @return the scope for the given type
   */
  // This is not static so that the result is @KeyFor("scopeToStatisticsMap").
  @SuppressWarnings("keyfor:return") // the result will be used as a key
  public @Nullable @KeyFor("scopeToStatisticsMap") Object getScope(ClassOrInterfaceType type) {
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
    StringJoiner sb = new StringJoiner(System.lineSeparator());

    for (Map.Entry<@Nullable Object, ConstantStatistics> scopeEntry :
        scopeToStatisticsMap.entrySet()) {
      Object scope = scopeEntry.getKey();
      ConstantStatistics stats = scopeEntry.getValue();

      sb.add("Scope: " + scope + " (" + stats.getNumClasses() + " classes)");

      for (Map.Entry<Sequence, ConstantStatistics.ConstantUses> constantEntry :
          stats.getConstantUses().entrySet()) {
        Sequence sequence = constantEntry.getKey();
        ConstantStatistics.ConstantUses constantStats = constantEntry.getValue();

        sb.add("  " + sequence + " -> (" + constantStats.toString() + ")");
      }
    }

    return sb.toString();
  }
}
