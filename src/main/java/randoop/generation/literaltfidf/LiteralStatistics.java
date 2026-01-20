package randoop.generation.literaltfidf;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
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

  /**
   * Per-output-type index: for each {@link Type}, a map from {@link Sequence} (a literal producer)
   * to its {@link LiteralUses} within the type's scope.
   *
   * <p>Lifecycle: All mutations occur during initialization via incrementNumUses() and
   * incrementNumClassesWith(), driven by ClassLiteralExtractor (bytecode mining) and
   * LiteralFileReader (external literals files), orchestrated by OperationModel. Once this
   * LiteralStatistics is attached to ComponentManager, this map is read-only. It's read once when
   * TfIdfSelector is created for this scope, then never accessed again for this scope.
   */
  private final Map<Type, Map<Sequence, LiteralUses>> literalUsesByType = new LinkedHashMap<>();

  /**
   * The number of classes in this scope.
   *
   * <p>This counts how many classes have contributed literals to this statistics object
   * (incremented once per class by recordSequencesInClass). It is NOT equal to
   * literalUsesByType.size(), which is the number of output types with any literals. There is no
   * direct relationship between these two values.
   */
  private int numClasses = 0;

  /** Creates a new empty LiteralStatistics. */
  public LiteralStatistics() {}

  // ///////////////////////////////////////////////////////////////////////////
  // Observers
  //

  /**
   * Returns true if this is empty.
   *
   * @return true if this is empty
   */
  public boolean isEmpty() {
    return literalUsesByType.isEmpty();
  }

  /**
   * Returns the number of classes in the current scope.
   *
   * @return the number of classes in the current scope
   */
  public int getNumClasses() {
    return numClasses;
  }

  /**
   * Return the {@link LiteralUses} for the given sequence.
   *
   * <p>This method creates an entry for the sequence's output type and a new {@link LiteralUses} if
   * necessary. It is only called from the mutators of this class, namely ({@link #incrementNumUses}
   * and {@link #incrementNumClassesWith}) to ensure an entry exists before updating counts.
   *
   * @param seq a literal-producing sequence
   * @return the {@link LiteralUses} for the given sequence (created if absent)
   */
  private LiteralUses getLiteralUses(Sequence seq) {
    if (!seq.isNonreceiver()) {
      throw new IllegalArgumentException("sequence must be a literal producer: " + seq);
    }
    Type outputType = seq.getLastVariable().getType();
    Map<Sequence, LiteralUses> typeMap =
        literalUsesByType.computeIfAbsent(outputType, k -> new LinkedHashMap<>());
    LiteralUses currentUses = typeMap.computeIfAbsent(seq, k -> new LiteralUses());
    return currentUses;
  }

  /**
   * Returns sequences for a specific type.
   *
   * @param type the type to get sequences for
   * @return the sequences for the given type
   */
  public SIList<Sequence> getSequencesForType(Type type) {
    Map<Sequence, LiteralUses> literalToUses = literalUsesByType.get(type);
    if (literalToUses == null || literalToUses.isEmpty()) {
      return SIList.empty();
    }
    return SIList.from(literalToUses.keySet());
  }

  /**
   * Returns an iterable over all literal-to-usage entries across all output.
   *
   * @return an {@link Iterable} of entries mapping each {@link Sequence} to its {@link LiteralUses}
   */
  public Iterable<Map.Entry<Sequence, LiteralUses>> literalUsesEntries() {
    return () -> // This line makes the method return an Iterable rather than an Iterator.
    new Iterator<Map.Entry<Sequence, LiteralUses>>() {
          private final Iterator<Map<Sequence, LiteralUses>> outer =
              literalUsesByType.values().iterator();
          private Iterator<Map.Entry<Sequence, LiteralUses>> inner = Collections.emptyIterator();

          @Override
          public boolean hasNext() {
            while (!inner.hasNext() && outer.hasNext()) {
              inner = outer.next().entrySet().iterator();
            }
            return inner.hasNext();
          }

          @Override
          public Map.Entry<Sequence, LiteralUses> next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }
            return inner.next();
          }
        };
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Mutators
  //

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
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Helper class: LiteralUses
  //

  /**
   * Statistics for one literal within one scope: the number of uses of the literal and the number
   * of classes that contain the literal.
   *
   * <p>A LiteralUses is mutable.
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
     * Increments the number of classes that use the literal.
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
