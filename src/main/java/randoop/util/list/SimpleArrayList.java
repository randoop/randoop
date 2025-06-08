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

  /** Creates a new, empty SimpleArrayList. */
  public SimpleArrayList() {
    super();
  }

  /**
   * Creates a new, empty SimpleArrayList with the given capacity.
   *
   * @param initialCapacity the capacity of the new list
   */
  public SimpleArrayList(int initialCapacity) {
    super(initialCapacity);
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
