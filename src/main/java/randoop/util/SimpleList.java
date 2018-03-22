package randoop.util;

import java.util.List;

/**
 * Stores a sequence of items, much like a regular {@code List}. Subclasses exist that permit
 * efficient appending and concatenation:
 *
 * <ul>
 *   <li>{@link SimpleArrayList}: a typical list is stored as an array list.
 *   <li>{@link ListOfLists}: a list that only stores pointers to its constituent sub-lists.
 *   <li>{@link OneMoreElementList}: stores a SimpleList plus one additional final element.
 * </ul>
 *
 * <p>IMPLEMENTATION NOTE
 *
 * <p>Randoop's main generator ({@link randoop.generation.ForwardGenerator ForwardGenerator})
 * creates new sequences by concatenating existing sequences and appending a statement at the end.
 * When profiling Randoop, we observed that naive concatenation took up a large portion of the
 * tool's running time, and the component set (i.e. the set of stored sequences used to create more
 * sequences) quickly exhausted the memory available.
 *
 * <p>To improve memory and time efficiency, we now do concatenation differently.
 *
 * <p>When concatenating N Sequences to create a new sequence, we store the concatenated sequence
 * statements in a ListofLists, which takes space (and creation time) proportional to N, not to the
 * length of the new sequence.
 *
 * <p>When extending a Sequence with a new statement, we store the old sequence's statements plus
 * the new statement in a {@code OneMoreElementList}, which takes up only 2 references in memory
 * (and constant creation time).
 */
public interface SimpleList<T> {

  /**
   * Return the number of elements in this list.
   *
   * @return the number of elements in this list
   */
  public int size();

  /**
   * Test if this list is empty.
   *
   * @return true if this list is empty, false otherwise
   */
  public boolean isEmpty();

  /**
   * Return the element at the given position of this list.
   *
   * @param index the position for the element
   * @return the element at the index
   */
  public T get(int index);

  /**
   * Return a sublist of this list that contains the index. Does not necessarily contain the first
   * element.
   *
   * <p>The result is always an existing SimpleList, the smallest one that contains the index.
   * Currently, it is always a {@link SimpleArrayList}.
   *
   * @param index the index into this list
   * @return the sublist containing this index
   */
  public SimpleList<T> getSublist(int index);

  // TODO: Replace some uses of this, such as direct implementations of toString.
  /**
   * Returns a java.util.List version of this list. Caution: this operation can be expensive.
   *
   * @return {@link java.util.List} for this list
   */
  public abstract List<T> toJDKList();
}
