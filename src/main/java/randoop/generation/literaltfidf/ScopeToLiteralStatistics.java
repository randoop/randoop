package randoop.generation.literaltfidf;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

  /** A special key representing the unnamed (default) package. */
  public static final Object UNNAMED_PACKAGE = "UNNAMED_PACKAGE";

  /**
   * A map from a specific scope to its literal statistics. The constant {@link UNNAMED_PACKAGE}
   * represents the unnamed package.
   */
  private LinkedHashMap<@Nullable Object, LiteralStatistics> scopeToStatisticsMap =
      new LinkedHashMap<>();

  /** Creates a ScopeToLiteralStatistics. */
  public ScopeToLiteralStatistics() {}

  /**
   * Returns information about literals in the scope for {@code type}. Includes superclass literals
   * if {@link GenInputsAbstract#include_superclass_literals} is true.
   *
   * @param type the type whose scope to access
   * @return information about literals in the scope for {@code type}
   */
  public LiteralStatistics getLiteralStatistics(ClassOrInterfaceType type) {
    return getLiteralStatistics(type, GenInputsAbstract.include_superclass_literals);
  }

  /**
   * Returns information about literals in a specific scope, optionally including superclass
   * literals.
   *
   * @param type the type whose scope to access
   * @param includeSuperclassLiterals whether to include literals from superclasses
   * @return information about literals in the scope for {@code type}, optionally including
   *     superclass literals
   */
  private LiteralStatistics getLiteralStatistics(
      ClassOrInterfaceType type, boolean includeSuperclassLiterals) {
    // Only aggregate superclass literals when using CLASS level and the option is enabled.
    if (includeSuperclassLiterals
        && GenInputsAbstract.literals_level == GenInputsAbstract.ClassLiteralsMode.CLASS) {
      return createStatisticsWithSuperclasses(type);
    } else {
      return getLiteralStatisticsNoSuperclass(type);
    }
  }

  /**
   * Returns information about literals in the scope for {@code type}, without any superclass
   * literals.
   *
   * @param type the type whose scope to access
   * @return information about literals in the scope for {@code type}
   */
  public LiteralStatistics getLiteralStatisticsNoSuperclass(ClassOrInterfaceType type) {
    return scopeToStatisticsMap.computeIfAbsent(getScope(type), __ -> new LiteralStatistics());
  }

  /** A cache to speed up {@link #createStatisticsWithSuperclasses}. */
  private HashMap<ClassOrInterfaceType, LiteralStatistics> createStatisticsWithSuperclassesCache =
      new HashMap<>();

  /**
   * Creates a LiteralStatistics object that includes literals from the given type and all its
   * superclasses. Each class in the hierarchy is treated as a separate document: if a literal
   * appears in both a superclass and subclass, it contributes to the count from both.
   *
   * @param type the type whose literals and superclass literals to return
   * @return a new LiteralStatistics object containing literals from {@code type} and its
   *     superclasses. The client should not modify this object, because it may be reused from call
   *     to call.
   */
  private LiteralStatistics createStatisticsWithSuperclasses(ClassOrInterfaceType type) {

    LiteralStatistics cached = createStatisticsWithSuperclassesCache.get(type);
    if (cached != null) {
      return cached;
    }

    LiteralStatistics result;

    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        result = new LiteralStatistics();
        result.addAll(getLiteralStatisticsNoSuperclass(type));
        ClassOrInterfaceType supertype = type.getSuperclass();
        if (supertype != null) {
          result.addAll(createStatisticsWithSuperclasses(supertype));
        }
        break;

      case PACKAGE:
        result = new LiteralStatistics();
        // The algorithm walks all the superclasses, but it only calls addAll for a given
        // LiteralStatistics once.  We could track that in terms of LiteralStatistics or scopes.
        HashSet<LiteralStatistics> visitedStats = new HashSet<>();
        // Traverse the class hierarchy from current type up to Object.
        for (ClassOrInterfaceType current = type;
            current != null;
            current = current.getSuperclass()) {
          LiteralStatistics currentStats = getLiteralStatisticsNoSuperclass(type);
          if (visitedStats.add(currentStats)) {
            result.addAll(currentStats);
          }
        }
        break;

      case ALL:
        result = getLiteralStatisticsNoSuperclass(type);
        break;

      default:
        throw new RandoopBug("Bad literal level: " + GenInputsAbstract.literals_level);
    }

    createStatisticsWithSuperclassesCache.put(type, result);

    return result;
  }

  /**
   * Returns all literal sequences from all scopes, without duplicates.
   *
   * @return all literal sequences from all scopes
   */
  public Set<Sequence> getAllSequences() {
    Set<Sequence> allSequences = new LinkedHashSet<>();
    for (LiteralStatistics stats : scopeToStatisticsMap.values()) {
      // Cannot use `keySet()` because `stats.literalUsesEntries()` is not a `Map`.
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
    getLiteralStatisticsNoSuperclass(usingType).incrementNumUses(seq, numUses);
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
    LiteralStatistics stats = getLiteralStatisticsNoSuperclass(usingType);
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
        Package pkg = type.getPackage();
        return pkg != null ? pkg : UNNAMED_PACKAGE;
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
