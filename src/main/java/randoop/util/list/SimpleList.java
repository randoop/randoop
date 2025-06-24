package randoop.util.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.CollectionsPlume;

// Implementation note:
// Randoop's main generator ({@link randoop.generation.ForwardGenerator ForwardGenerator})
// creates new sequences by concatenating existing sequences, thenappending a statement at the end.
// When profiling Randoop, we observed that naive concatenation took up a large portion of the
// tool's running time, and the component set (i.e. the set of stored sequences used to create more
// sequences) quickly exhausted the memory available.

/**
 * An immutable list. Different lists may share structure, making the representation space-efficient
 * and making construction time-efficient. Use this only if you will be creating many lists that
 * share structure. Examples are when one list is the concatenation of other lists, or one list is
 * just like another with a single element added.
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
    int size = list.size();
    if (size == 0) {
      return empty();
    } else if (size == 1) {
      return singleton(list.get(0));
    } else {
      return new SimpleArrayList<>(list);
    }
  }

  /**
   * Returns an empty list.
   *
   * @param <E2> the type of elements of the list
   * @return an empty list
   */
  @SuppressWarnings("unchecked")
  public static <E2> SimpleList<E2> empty() {
    return SimpleEmptyList.it;
  }

  /**
   * Returns a new list containing one element.
   *
   * @param <E2> the type of elements of the list
   * @param elt the element
   * @return a new list containing one element
   */
  public static <E2> SimpleList<E2> singleton(E2 elt) {
    return new SingletonList<>(elt);
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
   * Returns a new list that consists of this one plus one more element. Does not modify this
   * object.
   *
   * @param element the additional element
   * @return a new list that consists of this one plus one more element
   */
  public SimpleList<E> add(E element) {
    return new OneMoreElementList<>(this, element);
  }

  /**
   * Concatenate an array of SimpleLists.
   *
   * @param <E2> the type of list elements
   * @param lists the lists that will compose the newly-created ListOfLists
   * @return the concatenated list
   */
  @SuppressWarnings("unchecked") // heap pollution warning
  public static <E2> SimpleList<E2> concat(SimpleList<E2>... lists) {
    List<SimpleList<E2>> withoutEmpty = new ArrayList<>(lists.length);
    for (SimpleList<E2> sl : lists) {
      if (!sl.isEmpty()) {
        withoutEmpty.add(sl);
      }
    }
    return concatNonEmpty(withoutEmpty);
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
    if (CollectionsPlume.anyMatch(lists, SimpleList::isEmpty)) {
      // Don't side-effect the parameter `lists`; instead, re-assign it.
      lists = new ArrayList<>(lists);
      lists.removeIf((SimpleList<E2> sl) -> sl.isEmpty());
    }
    return concatNonEmpty(lists);
  }

  /**
   * Create a SimpleList from a list of SimpleLists, none of which is empty.
   *
   * @param <E2> the type of list elements
   * @param lists the non-empty lists that will compose the newly-created ListOfLists
   * @return the concatenated list
   */
  @SuppressWarnings({"unchecked"}) // heap pollution warning
  private static <E2> SimpleList<E2> concatNonEmpty(List<SimpleList<E2>> lists) {
    int numLists = lists.size();
    if (numLists == 0) {
      return SimpleList.empty();
    } else if (numLists == 1) {
      return lists.get(0);
    } else if (numLists == 2 && lists.get(1).size() == 1) {
      return new OneMoreElementList<>(lists.get(0), lists.get(1).get(0));
    } else {
      return new ListOfLists<>(lists);
    }
  }

  // **************** accessors ****************

  /**
   * Returns the number of elements in this list.
   *
   * @return the number of elements in this list
   */
  public abstract int size();

  /**
   * Returns true if this list is empty.
   *
   * @return true if this list is empty, false otherwise
   */
  public abstract boolean isEmpty();

  /**
   * Returns the element at the given position of this list.
   *
   * @param index a position in the list
   * @return the element at the index
   */
  public abstract E get(int index);

  /**
   * Returns a view of the portion of this list between the specified fromIndex, inclusive, and
   * toIndex, exclusive.
   *
   * @param fromIndex low endpoint (inclusive) of the subList
   * @param toIndex high endpoint (exclusive) of the subList
   * @return a view of part of this list
   */
  // TODO: Should this be abstract, forcing subclasses to implement?
  public SimpleList<E> subList(int fromIndex, int toIndex) {
    checkRange(fromIndex, toIndex);
    if (fromIndex == toIndex) {
      return empty();
    } else if (toIndex == fromIndex + 1) {
      return singleton(get(fromIndex));
    } else {
      // TODO: ListOfLists and OneMoreElementList can sometimes do better than this.
      return new SimpleSubList<E>(this, fromIndex, toIndex);
    }
  }

  // The result is always an existing SimpleList, the smallest one that contains the index.
  /**
   * Returns an arbitrary sublist of this list that contains the index. The result does not
   * necessarily contain the first or last element of this.
   *
   * @param index the index into this list
   * @return a sublist containing this index
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
  /*package-private*/ final void checkIndex(int index) {
    if (index < 0 || index >= size()) {
      throw new IllegalArgumentException(
          String.format("Bad index %d for list of length %d: %s", index, size(), this));
    }
  }

  /**
   * Throws an exception if the range is not valid for this.
   *
   * @param fromIndex low endpoint (inclusive) of the range
   * @param toIndex high endpoint (exclusive) of the range
   */
  /*package-private*/ final void checkRange(int fromIndex, int toIndex) {
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
