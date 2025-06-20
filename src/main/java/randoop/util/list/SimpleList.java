package randoop.util.list;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
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
public abstract class SimpleList<E> implements Iterable<E>, Serializable {

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
   * Returns a new list containing one element.
   *
   * @param <E2> the type of elements of the list
   * @param elt the element
   * @return a new list containing one element
   */
  public static <E2> SimpleList<E2> singleton(E2 elt) {
    List<E2> lst = Collections.singletonList(elt);
    return new SimpleArrayList<>(lst);
  }

  /**
   * Returns a new list containing zero or one element.
   *
   * @param <E2> the type of elements of the list
   * @param elt the element
   * @return a new list containing the element if it is non-null; if the element is null, returns an
   *     empty list
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
    return new ListOfLists<>(Arrays.asList(lists));
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
    return new ListOfLists<>(lists);
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

  /**
   * Return an arbitrary sublist of this list that contains the index. The result does not
   * necessarily contain the first element of this.
   *
   * <p>The result is always an existing SimpleList, the smallest one that contains the index.
   *
   * @param index the index into this list
   * @return the sublist containing this index
   */
  public abstract SimpleList<E> getSublistContaining(int index);

  @Override
  public String toString() {
    StringJoiner sj = new StringJoiner(", ", "SI[", "]");
    for (E elt : this) {
      sj.add(Objects.toString(elt));
    }
    return sj.toString();
  }

  // **************** diagnostics ****************

  /**
   * Throws an exception if the index is not valid for this.
   *
   * @param index an index into this
   */
  /*package-protected*/ final void checkIndex(int index) {
    if (index < 0 || index >= size()) {
      throw new IllegalArgumentException(
          String.format("Bad index %d for list of length %d: %s", index, size(), this));
    }
  }

  /**
   * Throws an exception if the range is not valid for this.
   *
   * @param fromIndex - low endpoint (inclusive) of the range
   * @param toIndex - high endpoint (exclusive) of the range
   */
  /*package-protected*/ final void checkRange(int fromIndex, int toIndex) {
    if (fromIndex < 0 || fromIndex > toIndex || toIndex > size()) {
      throw new IllegalArgumentException(
          String.format(
              "Bad range (%d,%d) for list of length %d: %s", fromIndex, toIndex, size(), this));
    }
  }

  // **************** temporary ****************

  // Replace this by the version in CollectionsPlume, when CollectionsPlume 1.10.2 is released.
  /**
   * Adds all elements of the Iterable to the collection. This method is just like {@code
   * Collection.addAll()}, but that method takes only a Collection, not any Iterable, as its
   * arguments.
   *
   * @param <T> the type of elements
   * @param c the collection into which elements are to be inserted
   * @param elements the elements to insert into c
   * @return true if the collection changed as a result of the call
   */
  public static <T> boolean addAll(Collection<? super T> c, Iterable<? extends T> elements) {
    boolean added = false;
    for (T elt : elements) {
      if (c.add(elt)) {
        added = true;
      }
    }
    return added;
  }
}
