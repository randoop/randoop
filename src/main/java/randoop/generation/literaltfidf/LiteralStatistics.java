package randoop.generation.literaltfidf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.plumelib.util.SIList;
import randoop.sequence.Sequence;
import randoop.types.Type;

/**
 * This class stores information about literals used in one "scope" of the SUT, where a scope is a
 * class, a package, or the entire SUT.
 *
 * <p>For each literal (represented as a sequence), it stores the number of uses of the sequence and
 * the number of classes that contain the sequence. It also stores the number of classes in the
 * scope. Literals are segregated by their output type.
 */
public class LiteralStatistics {

  /** A map from type to a map from literal to its usage statistics. */
  private Map<Type, Map<Sequence, LiteralUses>> literalUsesMap = new LinkedHashMap<>();

  /** The number of classes in this scope. */
  private int numClasses = 0;

  /** Cached flattened map from literal to its usage statistics. */
  private Map<Sequence, LiteralUses> cachedLiteralUses = null;

  /** Creates a new empty LiteralStatistics. */
  public LiteralStatistics() {}

  /**
   * Returns the number of classes in the current scope.
   *
   * @return the number of classes in the current scope
   */
  public Integer getNumClasses() {
    return numClasses;
  }

  /**
   * Returns true if this is empty.
   *
   * @return true if this is empty
   */
  public boolean isEmpty() {
    return literalUsesMap.isEmpty();
  }

  // TODO: It would likely be more efficient to define an iterator method for clients to use, rather
  // than creating and caching the map.
  /**
   * Returns the map from literal to its usage statistics, flattened across all types.
   *
   * @return the map from literal to its usage statistics
   */
  public Map<Sequence, LiteralUses> getLiteralUses() {
    if (cachedLiteralUses == null) {
      Map<Sequence, LiteralUses> result = new LinkedHashMap<>();
      for (Map<Sequence, LiteralUses> typeMap : literalUsesMap.values()) {
        result.putAll(typeMap);
      }
      cachedLiteralUses = result;
    }
    return cachedLiteralUses;
  }

  /**
   * Returns sequences for a specific type as an SIList for efficient iteration.
   *
   * @param type the type to get sequences for
   * @return the sequences for the given type
   */
  public SIList<Sequence> getSequencesForType(Type type) {
    Map<Sequence, LiteralUses> typeMap = literalUsesMap.get(type);
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
   * Increments the number of uses of a sequence.
   *
   * @param seq a sequence
   * @param num the number of uses of the sequence
   */
  public void incrementNumUses(Sequence seq, int num) {
    LiteralUses currentUses = getLiteralUses(seq);
    currentUses.incrementNumUses(num);
    cachedLiteralUses = null;
  }

  /**
   * Increments the number of classes that contain a sequence.
   *
   * @param seq a sequence
   * @param num the number of classes that contain the sequence
   */
  public void incrementNumClassesWith(Sequence seq, int num) {
    LiteralUses currentUses = getLiteralUses(seq);
    currentUses.incrementNumClassesWith(num);
    cachedLiteralUses = null;
  }

  /**
   * Return the {@link LiteralUses} for the given sequence.
   *
   * @param seq a sequence
   * @return the {@link LiteralUses} for the given sequence
   */
  private LiteralUses getLiteralUses(Sequence seq) {
    Type outputType = seq.getLastVariable().getType();
    Map<Sequence, LiteralUses> typeMap =
        literalUsesMap.computeIfAbsent(outputType, k -> new LinkedHashMap<>());
    LiteralUses currentUses = typeMap.computeIfAbsent(seq, k -> new LiteralUses());
    return currentUses;
  }

  /**
   * Statistics for one literal within one scope: the number of uses of the literal and the number
   * of classes that contain the literal.
   */
  public static class LiteralUses {
    /** The number of uses of the literal. */
    private int numUses;

    /** The number of classes that use the literal. */
    private int numClassesWith;

    /** Creates a new, empty LiteralUses. */
    public LiteralUses() {
      this(0, 0);
    }

    /**
     * Creates a new LiteralUses.
     *
     * @param numUses the number of uses of the literal
     * @param numClassesWith the number of classes that use the literal
     */
    public LiteralUses(int numUses, int numClassesWith) {
      this.numUses = numUses;
      this.numClassesWith = numClassesWith;
    }

    /**
     * Returns the number of uses of the literal.
     *
     * @return the number of uses of the literal
     */
    public int getNumUses() {
      return numUses;
    }

    /**
     * Returns the number of classes that use the literal.
     *
     * @return the number of classes that use the literal
     */
    public int getNumClassesWith() {
      return numClassesWith;
    }

    /**
     * Increments the number of uses of the literal.
     *
     * @param numUses the number of additional uses of the literal
     */
    public void incrementNumUses(int numUses) {
      this.numUses += numUses;
    }

    /**
     * Increments the number of classes that use it the literal.
     *
     * @param numClassesWith the number of additional classes that use the literal.
     */
    public void incrementNumClassesWith(int numClassesWith) {
      this.numClassesWith += numClassesWith;
    }

    @Override
    public String toString() {
      return numUses + " uses in " + numClassesWith + " classes";
    }
  }
}
