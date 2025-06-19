package randoop.util.list;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An immutable list. Different lists may share structure, making the representation space-efficient
 * and making construction time-efficient. Use this only if you will be creating many lists that
 * share structure. Examples are when one list is the concatenation of other lists, or one list is
 * just like another, with a single element added.
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
 *
 * @param <E> the type of elements of the list
 */
public abstract class SimpleList<E> implements /*Iterable<E>,*/ Serializable {

  /** Serial version UID. */
  static final long serialVersionUID = 20250617;

  // **************** producers ****************

  /** Creates a SimpleList. */
  /*package-private*/ SimpleList() {}

  /**
   * Create a SimpleList from a JDK list.
   *
   * @param <E2> the type of list elements
   * @param list the elements of the new list
   * @return the list
   */
  @SuppressWarnings({"unchecked"}) // heap pollution warning
  public static <E2> SimpleList<E2> fromList(List<E2> list) {
    return new SimpleArrayList<>(list);
  }

  /**
   * Returns an empty list.
   *
   * @param <E2> the type of elements of the list
   * @return an empty list
   */
  public static <E2> SimpleList<E2> empty() {
    List<E2> lst = Collections.emptyList();
    return new SimpleArrayList<>(lst);
  }

  /**
   * Returns a new SimpleArrayList containing one element.
   *
   * @param <E2> the type of elements of the list
   * @param elt the element
   * @return a new SimpleArrayList containing one element
   */
  public static <E2> SimpleList<E2> singleton(E2 elt) {
    List<E2> lst = Collections.singletonList(elt);
    return new SimpleArrayList<>(lst);
  }

  /**
   * Returns a new SimpleArrayList containing zero or one element.
   *
   * @param <E2> the type of elements of the list
   * @param elt the element
   * @return a new SimpleArrayList containing the element if it is non-null; if the element is null,
   *     returns an empty list
   */
  public static <E2> SimpleList<E2> singletonOrEmpty(@Nullable E2 elt) {
    if (elt == null) {
      return empty();
    } else {
      return singleton(elt);
    }
  }

  /**
   * Concatenate an array of SimpleLists.
   *
   * @param <E2> the type of list elements
   * @param lists the lists that will compose the newly-created ListOfLists
   * @return the concatenated list
   */
  @SuppressWarnings({"unchecked"}) // heap pollution warning
  public static <E2> SimpleList<E2> concat(SimpleList<E2>... lists) {
    return ListOfLists.create(Arrays.asList(lists));
  }

  /**
   * Create a SimpleList from a list of SimpleLists.
   *
   * @param <E2> the type of list elements
   * @param lists the lists that will compose the newly-created ListOfLists
   * @return the concatenated list
   */
  @SuppressWarnings({"unchecked"}) // heap pollution warning
  public static <E2> SimpleList<E2> concat(List<SimpleList<E2>> lists) {
    return ListOfLists.create(lists);
  }

  // **************** accessors ****************

  /**
   * Return the number of elements in this list.
   *
   * @return the number of elements in this list
   */
  public abstract int size();

  /**
   * Test if this list is empty.
   *
   * @return true if this list is empty, false otherwise
   */
  public abstract boolean isEmpty();

  /**
   * Return the element at the given position of this list.
   *
   * @param index the position for the element
   * @return the element at the index
   */
  public abstract E get(int index);

  // TODO: Replace some uses of this, such as direct implementations of toString.
  /**
   * Returns a java.util.List version of this list. Caution: this operation can be expensive.
   *
   * @return {@link java.util.List} for this list
   */
  public abstract List<E> toJDKList();

  /**
   * Return an arbitrary sublist of this list that contains the index. The result does not
   * necessarily contain the first element of this.
   *
   * <p>The result is always an existing SimpleList, the smallest one that contains the index.
   * Currently, it is always a {@link SimpleArrayList}.
   *
   * @param index the index into this list
   * @return the sublist containing this index
   */
  public abstract SimpleList<E> getSublistContaining(int index);

  /*
  @Override
  public String toString() {
    StringJoiner sj = new StringJoiner(", ", "S[", "]");
    for (E elt : this) {
      sj.add(elt.toString());
    }
    return sj.toString();
  }
  */

  // **************** diagnostics ****************

  // /**
  //  * Throws an exception if the index is not valid for this.
  //  *
  //  * @param index an index into this
  //  */
  // private final void checkIndex(int index) {
  //   if (index < 0 || index >= size()) {
  //     throw new IllegalArgumentException(
  //         String.format("Bad index %d for list of length %d: %s", index, size(), this));
  //   }
  // }

  // /**
  //  * Throws an exception if the range is not valid for this.
  //  *
  //  * @param fromIndex - low endpoint (inclusive) of the range
  //  * @param toIndex - high endpoint (exclusive) of the range
  //  */
  // private final void checkRange(int fromIndex, int toIndex) {
  //   if (fromIndex < 0 || fromIndex > toIndex || toIndex > size()) {
  //     throw new IllegalArgumentException(
  //         String.format(
  //             "Bad range (%d,%d) for list of length %d: %s", fromIndex, toIndex, size(), this));
  //   }
  // }
}
