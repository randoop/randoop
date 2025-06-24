package randoop.util.list;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;

/**
 * An immutable empty list.
 *
 * @param <E> the type of elements of the list
 */
/*package-private*/ class SimpleEmptyList<E> extends SimpleList<E> implements Serializable {

  /** serialVersionUID */
  private static final long serialVersionUID = 20250617;

  /** The unique empty list. */
  @SuppressWarnings("rawtypes")
  public static SimpleList it = new SimpleEmptyList();

  /** Creates a new empty list. */
  private SimpleEmptyList() {}

  @Override
  public int size() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public E get(int index) {
    checkIndex(index);
    throw new Error("This can't happen.");
  }

  @Override
  public SimpleList<E> subList(int fromIndex, int toIndex) {
    checkRange(fromIndex, toIndex);
    return this;
  }

  @Override
  public SimpleList<E> getSublistContaining(int index) {
    throw new IndexOutOfBoundsException("index " + index + " for empty list");
  }

  @Override
  public Iterator<E> iterator() {
    return Collections.emptyIterator();
  }
}
