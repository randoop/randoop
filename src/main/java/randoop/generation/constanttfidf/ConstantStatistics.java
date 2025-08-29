package randoop.generation.constanttfidf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
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
  private Map<Type, Map<Sequence, ConstantUses>> constantStats = new LinkedHashMap<>();

  /** The number of classes in this scope. */
  private int numClasses = 0;

  /** Cached flattened map from constant to its usage statistics. */
  private Map<Sequence, ConstantUses> cachedConstantUses = null;

  /** Creates a new empty ConstantStatistics. */
  public ConstantStatistics() {}

  /**
   * Returns the number of classes in the current scope.
   *
   * @return the number of classes in the current scope
   */
  public Integer getNumClasses() {
    return numClasses;
  }

  /**
   * Returns the map from constant to its usage statistics, flattened across all types.
   *
   * @return the map from constant to its usage statistics
   */
  public Map<Sequence, ConstantUses> getConstantUses() {
    if (cachedConstantUses == null) {
      Map<Sequence, ConstantUses> result = new LinkedHashMap<>();
      for (Map<Sequence, ConstantUses> typeMap : constantStats.values()) {
        result.putAll(typeMap);
      }
      cachedConstantUses = result;
    }
    return cachedConstantUses;
  }

  /**
   * Returns sequences for a specific type as an SIList for efficient iteration.
   *
   * @param type the type to get sequences for
   * @return the sequences for the given type
   */
  public SIList<Sequence> getSequencesForType(Type type) {
    Map<Sequence, ConstantUses> typeMap = constantStats.get(type);
    if (typeMap == null || typeMap.isEmpty()) {
      return SIList.empty();
    }
    return SIList.fromList(new ArrayList<>(typeMap.keySet()));
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
   * Increments the number of uses.
   *
   * @param seq a sequence
   * @param num the number of uses of the sequence
   */
  public void incrementNumUses(Sequence seq, int num) {
    Type outputType = seq.getLastVariable().getType();
    Map<Sequence, ConstantUses> typeMap =
        constantStats.computeIfAbsent(outputType, k -> new LinkedHashMap<>());
    ConstantUses currentStats = typeMap.getOrDefault(seq, new ConstantUses(0, 0));
    typeMap.put(
        seq, new ConstantUses(currentStats.getNumUses() + num, currentStats.getNumClassesWith()));
    cachedConstantUses = null;
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
        constantStats.computeIfAbsent(outputType, k -> new LinkedHashMap<>());
    ConstantUses currentStats = typeMap.getOrDefault(seq, new ConstantUses(0, 0));
    typeMap.put(
        seq, new ConstantUses(currentStats.getNumUses(), currentStats.getNumClassesWith() + num));
    cachedConstantUses = null;
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
