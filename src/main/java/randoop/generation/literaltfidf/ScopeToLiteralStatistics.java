package randoop.generation.literaltfidf;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;

/** This class stores information about the literals used in the SUT. */
public class ScopeToLiteralStatistics {

  /** A special key representing the "all" scope. */
  public static final Object ALL_SCOPE = "ALL_SCOPE";

  /**
   * A map from a specific scope to its literal statistics. A null key represents the unnamed
   * package.
   */
  private LinkedHashMap<@Nullable Object, LiteralStatistics> scopeToStatisticsMap =
      new LinkedHashMap<>();

  /** Creates a ScopeToLiteralStatistics. */
  public ScopeToLiteralStatistics() {}

  /**
   * Returns information about literals in a specific scope.
   *
   * @param type the type whose scope to access
   * @return information about literals in the scope for {@code type}, including superclass literals
   */
  public LiteralStatistics getLiteralStatistics(ClassOrInterfaceType type) {
    return scopeToStatisticsMap.computeIfAbsent(getScope(type), __ -> new LiteralStatistics());
  }

  /**
   * Returns all literal sequences from all scopes.
   *
   * @return all literal sequences from all scopes
   */
  public Set<Sequence> getAllSequences() {
    Set<Sequence> allSequences = new LinkedHashSet<>();
    for (LiteralStatistics stats : scopeToStatisticsMap.values()) {
      for (Map.Entry<Sequence, LiteralStatistics.LiteralUses> e : stats.literalUsesEntries()) {
        allSequences.add(e.getKey());
      }
    }
    return allSequences;
  }

  /**
   * Registers uses of the given literal.
   *
   * @param usingType the class whose scope is being updated
   * @param seq the sequence to be added
   * @param numUses the number of times the {@code seq} is used in {@code usingType}
   */
  public void incrementNumUses(ClassOrInterfaceType usingType, Sequence seq, int numUses) {
    getLiteralStatistics(usingType).incrementNumUses(seq, numUses);
  }

  /**
   * Records that a class uses the given sequences and increments the total class count. The class
   * might use a literal one time or many times; it makes no difference to this method.
   *
   * @param usingType the class whose scope is being updated
   * @param sequences all the literal sequences in the class
   */
  public void recordSequencesInClass(
      ClassOrInterfaceType usingType, Collection<Sequence> sequences) {
    LiteralStatistics stats = getLiteralStatistics(usingType);
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
  // If this method were `static`, the Nullness Checker would issue a warning because the result
  // would be @KeyFor("ScopeToLiteralStatistics.scopeToStatisticsMap") rather than
  // `@KeyFor("scopeToStatisticsMap")` which it needs to be for the Nullness Checker.
  @SuppressWarnings("keyfor:return") // the result will be added to the map as a key
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
    StringJoiner sj = new StringJoiner(System.lineSeparator());

    for (Map.Entry<@Nullable Object, LiteralStatistics> scopeEntry :
        scopeToStatisticsMap.entrySet()) {
      Object scope = scopeEntry.getKey();
      LiteralStatistics stats = scopeEntry.getValue();

      sj.add("Scope: " + scope + " (" + stats.getNumClasses() + " classes)");
      for (Map.Entry<Sequence, LiteralStatistics.LiteralUses> e : stats.literalUsesEntries()) {
        sj.add("  " + e.getKey() + " -> " + e.getValue());
      }
    }

    return sj.toString();
  }
}
