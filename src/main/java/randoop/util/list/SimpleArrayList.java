package randoop.util.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

  /** Creates a new, empty SimpleArrayList. Clients should use {@link #empty()} instead. */
  /*package-private*/ SimpleArrayList() {
    super();
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
  public SimpleList<E> getSublistContaining(int index) {
    return this;
  }

  @Override
  public List<E> toJDKList() {
    return new ArrayList<>(this);
  }
}
