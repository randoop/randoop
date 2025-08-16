package randoop.generation.constanttfidf;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import randoop.types.JavaTypes;

/** This class stores information about the constants used in the SUT. */
public class ScopeToConstantStatistics {

  /** A special key representing the "all" scope. */
  public static final Object ALL_SCOPE = "ALL_SCOPE";

  /**
   * A map from a specific scope to its constant statistics. A null key represents the unnamed
   * package.
   */
  // Declared as HashMap rather than as Map because some Map implementations prohibit null keys.
  private HashMap<@Nullable Object, ConstantStatistics> scopeToStatisticsMap = new HashMap<>();

  /** Creates a ScopeToConstantStatistics. */
  public ScopeToConstantStatistics() {}

  /**
   * Returns information about constants in a specific scope, including constants from superclasses.
   * This mimics the behavior of the old ClassLiterals system.
   *
   * @param type the type whose scope to access
   * @return information about constants in the scope for {@code type}, including superclass
   *     constants
   */
  public ConstantStatistics getConstantStatistics(ClassOrInterfaceType type) {
    return scopeToStatisticsMap.computeIfAbsent(getScope(type), __ -> new ConstantStatistics());
  }

  /**
   * Returns all sequences for a type, including sequences from superclasses. This provides the
   * inheritance behavior that was present in ClassLiterals.
   *
   * @param type the type to get sequences for
   * @return all sequences for the type and its superclasses
   */
  public Set<Sequence> getSequencesIncludingSuperclasses(ClassOrInterfaceType type) {
    Set<Sequence> allSequences = new HashSet<>();
    Set<ClassOrInterfaceType> superClasses = getSuperClasses(type);
    for (ClassOrInterfaceType superClass : superClasses) {
      ConstantStatistics superStats = getConstantStatistics(superClass);
      allSequences.addAll(superStats.getSequenceSet());
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
   * Adds sequences from a literals file with default frequency of 1 use per sequence. Used for
   * literals loaded from external files.
   *
   * @param type the class whose scope is being updated
   * @param sequences the sequences to add
   */
  public void addLiteralsFromFile(ClassOrInterfaceType type, Collection<Sequence> sequences) {
    for (Sequence seq : sequences) {
      incrementNumUses(type, seq, 1); // Default frequency for file literals
    }
    incrementClassesWithSequences(type, sequences);
  }

  /**
   * Returns all sequences from all scopes.
   *
   * @return all sequences recorded in this statistics object
   */
  public Set<Sequence> getAllSequences() {
    Set<Sequence> allSequences = new HashSet<>();
    for (ConstantStatistics stats : scopeToStatisticsMap.values()) {
      allSequences.addAll(stats.getSequenceSet());
    }
    return allSequences;
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

  /**
   * Gets superclasses for the given class. Stops at null or Object (excludes Object from result).
   * This mimics the behavior from ClassLiterals.
   *
   * @param cls the class/interface type
   * @return the superclasses for the given type
   */
  private Set<ClassOrInterfaceType> getSuperClasses(ClassOrInterfaceType cls) {
    Set<ClassOrInterfaceType> ret = new LinkedHashSet<>();
    ClassOrInterfaceType sup = cls;
    while (sup != null && !sup.equals(JavaTypes.OBJECT_TYPE)) {
      ret.add(sup);
      sup = sup.getSuperclass();
    }
    return ret;
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
