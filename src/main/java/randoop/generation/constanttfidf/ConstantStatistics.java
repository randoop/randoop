package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.plumelib.util.SIList;
import randoop.sequence.Sequence;
import randoop.types.Type;

/**
 * This class stores information about constants used in one "scope" of the SUT, where a scope is a
 * class, a package, or the entire SUT.
 *
 * <p>For each constant (represented as a sequence), it stores the number of uses of the sequence
 * and the number of classes that contain the sequence. It also stores the number of classes in the
 * scope. Constants are segregated by their output type.
 */
public class ConstantStatistics {

  /** A map from type to a map from constant to its usage statistics. */
  private Map<Type, Map<Sequence, ConstantUses>> constantStats = new HashMap<>();

  /** The number of classes in this scope. */
  private int numClasses = 0;

  /** Creates a new empty ConstantStatistics. */
  public ConstantStatistics() {}

  /**
   * Returns the map from constant to its usage statistics, flattened across all types.
   *
   * @return the map from constant to its usage statistics
   */
  public Map<Sequence, ConstantUses> getConstantUses() {
    Map<Sequence, ConstantUses> result = new HashMap<>();
    for (Map<Sequence, ConstantUses> typeMap : constantStats.values()) {
      result.putAll(typeMap);
    }
    return result;
  }

  /**
   * Returns the map from constant to its usage statistics for a specific type.
   *
   * @param type the type to get statistics for
   * @return the map from constant to its usage statistics for the given type
   */
  public Map<Sequence, ConstantUses> getConstantUsesForType(Type type) {
    return constantStats.getOrDefault(type, new HashMap<>());
  }

  /**
   * Returns sequences for a specific type as an SIList for efficient iteration. This method handles
   * type compatibility by checking if the needed type is assignable from any of the stored types.
   *
   * @param type the type to get sequences for
   * @return the sequences for the given type and compatible types
   */
  public SIList<Sequence> getSequencesForType(Type type) {
    java.util.List<Sequence> result = new java.util.ArrayList<>();

    // Check for exact match first
    Map<Sequence, ConstantUses> exactMatch = constantStats.get(type);
    if (exactMatch != null) {
      result.addAll(exactMatch.keySet());
    }

    // Check for compatible types - sequences whose output type can be assigned to the needed type
    for (Map.Entry<Type, Map<Sequence, ConstantUses>> entry : constantStats.entrySet()) {
      Type storedType = entry.getKey();
      if (!storedType.equals(type) && type.isAssignableFrom(storedType)) {
        result.addAll(entry.getValue().keySet());
      }
    }

    return SIList.fromList(result);
  }

  /**
   * Returns the number of classes in the current scope.
   *
   * @return the number of classes in the current scope
   */
  public Integer getNumClasses() {
    return numClasses;
  }

  /**
   * Increments the number of uses.
   *
   * @param seq a sequence
   * @param num the number of uses of the sequence
   */
  public void incrementNumUses(Sequence seq, int num) {
    Type outputType = seq.getLastVariable().getType();
    Map<Sequence, ConstantUses> typeMap =
        constantStats.computeIfAbsent(outputType, k -> new HashMap<>());
    ConstantUses currentStats = typeMap.getOrDefault(seq, new ConstantUses(0, 0));
    typeMap.put(
        seq, new ConstantUses(currentStats.getNumUses() + num, currentStats.getNumClassesWith()));
  }

  /**
   * Increments the number of classes that contain a sequence.
   *
   * @param seq a sequence
   * @param num the number of classes that contain the sequence
   */
  public void incrementNumClassesWith(Sequence seq, int num) {
    Type outputType = seq.getLastVariable().getType();
    Map<Sequence, ConstantUses> typeMap =
        constantStats.computeIfAbsent(outputType, k -> new HashMap<>());
    ConstantUses currentStats = typeMap.getOrDefault(seq, new ConstantUses(0, 0));
    typeMap.put(
        seq, new ConstantUses(currentStats.getNumUses(), currentStats.getNumClassesWith() + num));
  }

  /**
   * Increments the number of classes.
   *
   * @param num the number of classes to add to the current total
   */
  public void incrementNumClasses(int num) {
    numClasses += num;
  }

  /**
   * Returns all sequences that have been recorded across all types.
   *
   * @return the sequences that have been recorded
   */
  public Set<Sequence> getSequenceSet() {
    Set<Sequence> result = new HashSet<>();
    for (Map<Sequence, ConstantUses> typeMap : constantStats.values()) {
      result.addAll(typeMap.keySet());
    }
    return result;
  }

  /**
   * Statistics for one constant within one scope: the number of uses of the constant and the number
   * of classes that contain the constant.
   */
  public static class ConstantUses {
    /** The number of uses of the constant. */
    private final int numUses;

    /** The number of classes that use the constant. */
    private final int numClassesWith;

    /**
     * Creates a new ConstantUses.
     *
     * @param numUses the number of uses of the constant
     * @param numClassesWith the number of classes that use the constant
     */
    public ConstantUses(int numUses, int numClassesWith) {
      this.numUses = numUses;
      this.numClassesWith = numClassesWith;
    }

    /**
     * Returns the number of uses of the constant.
     *
     * @return the number of uses of the constant
     */
    public int getNumUses() {
      return numUses;
    }

    /**
     * Returns the number of classes that use the constant.
     *
     * @return the number of classes that use the constant
     */
    public int getNumClassesWith() {
      return numClassesWith;
    }

    @Override
    public String toString() {
      return numUses + " uses in " + numClassesWith + " classes";
    }
  }
}
