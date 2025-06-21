package randoop.util.list;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;

/**
 * A view of part of a SimpleList.
 *
 * @param <E> the type of elements of the list
 */
public class SimpleEmptyList<E> extends SimpleList<E> implements Serializable {

  /** serialVersionUID */
  private static final long serialVersionUID = 20250617;

  @SuppressWarnings("rawtypes")
  public static SimpleList it = new SimpleEmptyList<Object>();

  /** Creates a new empty list. */
  private SimpleEmptyList() {}

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public E get(int index) {
    throw new IndexOutOfBoundsException();
  }

  @Override
  public SimpleList<E> subList(int fromIndex, int toIndex) {
    if (fromIndex != 0 || toIndex != 0) {
      throw new IllegalArgumentException(
          String.format("Tried to take subList(%d, %d) of %s", fromIndex, toIndex, this));
    }
    return this;
  }

  @Override
  // Return the entire list.
  public SimpleList<E> getSublistContaining(int index) {
    return this;
  }

  @Override
  public Iterator<E> iterator() {
    return Collections.emptyIterator();
  }
}
