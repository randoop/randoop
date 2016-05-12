package randoop.util;

import java.util.List;

/**
 * List implementation used by Randoop to store the sequence of
 * <code>Statement</code>s making up a Sequence.
 * <p>
 *
 * IMPLEMENTATION NOTE
 * <p>
 *
 * Randoop's main generator ({@link randoop.generation.ForwardGenerator
 * ForwardGenerator}) creates new sequences by concatenating existing sequences
 * and appending a statement at the end. A naive implementation of concatenation
 * copies the elements of the concatenated sub-sequences into a new list. The
 * first implementation of Sequence concatenation took this approach.
 *
 * <p>
 * When profiling Randoop, we observed that naive concatenation took up a large
 * portion of the tool's running time, and the component set (i.e. the set of
 * stored sequences used to create more sequences) quickly exhausted the memory
 * available.
 *
 * <p>
 * To improve memory and time efficiency, we now do concatenation differently.
 * We store the list of statements in a Sequence in a SimpleList, an abstract
 * class that has three subclasses:
 *
 * <ul>
 * <li>{@link ArrayListSimpleList}: a typical list is stored as an array list.
 * <li>{@link ListOfLists}: a list that only stores pointers to its constituent
 * sub-lists.
 * <li>{@link OneMoreElementList}: stores a SimpleList plus one additional final
 * element.
 * </ul>
 * <p>
 * When concatenating N Sequences to create a new sequence, we store the
 * concatenated sequence statements in a ListofLists, which takes space (and
 * creation time) proportional to N, not to the length of the new sequence.
 * <p>
 * When extending a Sequence with a new statement, we store the old sequence's
 * statements plus the new statement in a {@code OneMoreElementList}, which
 * takes up only 2 references in memory (and constant creation time).
 */
public abstract class SimpleList<T> {

  /**
   * Return the number of elements in this list.
   *
   * @return the number of elements in this list
   */
  public abstract int size();

  /**
   * Return the element at the given position of this list.
   *
   * @param index
   *          the position for the element
   * @return the element at the index
   */
  public abstract T get(int index);

  /**
   * Return the shortest sublist of this list that contains the index based on
   * the compositional structure of this list.
   *
   * @param index
   *          the index into this list
   * @return the sublist containing this list
   */
  public abstract SimpleList<T> getSublist(int index);

  /**
   * Test if this list is empty.
   *
   * @return true if this list is empty, false otherwise
   */
  public final boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns a java.util.List version of this list.
   *
   * @return {@link java.util.List} for this list.
   */
  public abstract List<T> toJDKList();
}
