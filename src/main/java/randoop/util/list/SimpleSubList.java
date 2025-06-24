package randoop.util.list;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A view of part of a SimpleList.
 *
 * @param <E> the type of elements of the list
 */
/*package-private*/ class SimpleSubList<E> extends SimpleList<E> implements Serializable {

  /** serialVersionUID */
  private static final long serialVersionUID = 20250617;

  // TODO: use an array instead, for efficiency?
  /** The backing storage. */
  private final SimpleList<E> delegate;

  /** The index in `delegate` of the first element in this list. */
  private final int fromIndex;

  /** The index in `delegate` of one past the last element in this list. */
  private final int toIndex;

  /**
   * Creates a sublist spanning the given range.
   *
   * @param delegate the list whose sublist to return
   * @param fromIndex the index in `delegate` of the first element in this list
   * @param toIndex the index in `delegate` of one past the last element in this list.
   */
  @SuppressWarnings("nullness:method.invocation") // sufficiently initialized
  /*package-protected*/ SimpleSubList(SimpleList<E> delegate, int fromIndex, int toIndex) {
    this.delegate = delegate;
    this.fromIndex = fromIndex;
    this.toIndex = toIndex;
    checkRange(fromIndex, toIndex);
  }

  @Override
  public int size() {
    return toIndex - fromIndex;
  }

  @Override
  public boolean isEmpty() {
    return toIndex == fromIndex;
  }

  @Override
  public E get(int index) {
    return delegate.get(fromIndex + index);
  }

  @Override
  public SimpleList<E> getSublistContaining(int index) {
    return this;
  }

  // Two possible implementation strategies:
  //  1. Create an iterator for `delegate`, discard `fromIndex` elements of it, and fail after
  // getting to `toIndex`.
  //  2. Use `get` to obtain the elements one by one, from `fromIndex` to `toIndex`.
  // I'm not sure which approach is more efficient.
  /** An iterator over a SimpleSubList. */
  private class SimpleSubListIterator implements Iterator<E> {

    /** The index of the next element to return. */
    int index = fromIndex;

    /** Creates a new SimpleSubListIterator. */
    public SimpleSubListIterator() {}

    @Override
    public boolean hasNext() {
      return index < toIndex;
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return delegate.get(index++);
    }
  }

  @Override
  public Iterator<E> iterator() {
    return new SimpleSubListIterator();
  }
}
