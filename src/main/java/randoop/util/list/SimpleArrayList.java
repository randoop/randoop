package randoop.util.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A SimpleList backed by an ArrayList.
 *
 * @param <E> the type of elements of the list
 */
public class SimpleArrayList<E> extends SimpleList<E> implements Serializable {

  // TODO: use an array instead, for efficiency?
  /** The backing storage. */
  ArrayList<E> delegate;

  /** serialVersionUID */
  private static final long serialVersionUID = 20250617;

  /**
   * Creates a new SimpleArrayList containing the given elements.
   *
   * @param c the elements of the list
   */
  public SimpleArrayList(Collection<? extends E> c) {
    delegate = new ArrayList<>(c);
  }

  @Override
  boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public E get(int index) {
    return delegate.get(index);
  }

  @Override
  public Iterator<E> iterator() {
    return delegate.iterator();
  }

  @Override
  // Return the entire list.
  public SimpleList<E> getSublistContaining(int index) {
    return this;
  }

  /*
  @Override
  public List<E> toJDKList() {
    return new ArrayList<>(this);
  }
  */
}
