package randoop.util.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A SimpleList backed by an ArrayList.
 *
 * @param <E> the type of elements of the list
 */
public class SimpleArrayList<E> extends ArrayList<E> implements SimpleList<E>, Serializable {

  /** serialVersionUID */
  private static final long serialVersionUID = 20180317;

  /**
   * Creates a new SimpleArrayList containing the given elements.
   *
   * @param c the elements of the list
   */
  public SimpleArrayList(Collection<? extends E> c) {
    super(c);
  }

  /** Creates a new, empty SimpleArrayList. */
  public SimpleArrayList() {
    super();
  }

  /**
   * Returns a new SimpleArrayList containing one element.
   *
   * @param <E2> the type of elements of the list
   * @param elt the element
   * @return a new SimpleArrayList containing one element
   */
  public static <E2> SimpleArrayList<E2> singleton(E2 elt) {
    List<E2> lst = Collections.singletonList(elt);
    return new SimpleArrayList<>(lst);
  }

  /**
   * Returns a new empty SimpleArrayList.
   *
   * @param <E2> the type of elements of the list
   * @return a new empty SimpleArrayList
   */
  public static <E2> SimpleArrayList<E2> empty() {
    List<E2> lst = Collections.emptyList();
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
  public static <E2> SimpleArrayList<E2> singletonOrEmpty(@Nullable E2 elt) {
    if (elt == null) {
      return empty();
    } else {
      return singleton(elt);
    }
  }

  @Override
  public boolean add(E elt) {
    throw new Error("Do not add to a SimpleArrayList");
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    throw new Error("Do not add to a SimpleArrayList");
  }

  @Override
  // Return the entire list.
  public SimpleList<E> getSublist(int index) {
    return this;
  }

  @Override
  public List<E> toJDKList() {
    return new ArrayList<>(this);
  }
}
