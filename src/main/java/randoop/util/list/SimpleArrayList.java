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
/*package-private*/ class SimpleArrayList<E> extends SimpleList<E> implements Serializable {

  /** serialVersionUID. */
  private static final long serialVersionUID = 20250617;

  // TODO: use an array instead, for efficiency?
  /** The backing storage. */
  ArrayList<E> delegate;

  /**
   * Creates a new SimpleArrayList containing the given elements.
   *
   * @param c the elements of the list
   */
  public SimpleArrayList(Collection<? extends E> c) {
    delegate = new ArrayList<>(c);
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public E get(int index) {
    return delegate.get(index);
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public Iterator<E> iterator() {
    return delegate.iterator();
  }

  @Override
  public SimpleList<E> getSublistContaining(int index) {
    return this;
  }
}
