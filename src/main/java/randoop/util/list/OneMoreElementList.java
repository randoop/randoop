package randoop.util.list;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.checkerframework.checker.lock.qual.GuardSatisfied;

/**
 * A list that consists of a list, plus one more element.
 *
 * @param <E> the type of elements of the list
 */
public final class OneMoreElementList<E> extends SimpleList<E> implements Serializable {

  /** serialVersionUID */
  private static final long serialVersionUID = 1332963552183905833L;

  /** All but the last element in this. */
  @SuppressWarnings("serial") // TODO: use a serializable type.
  private final SimpleList<E> list;

  /** The last element in this. */
  @SuppressWarnings("serial")
  private final E lastElement;

  /** The size of this. */
  private final int size;

  /**
   * Creates a OneMoreElementList.
   *
   * @param list the list to extend; it is not side-effected
   * @param extraElement the additional element
   */
  public OneMoreElementList(SimpleList<E> list, E extraElement) {
    this.list = list;
    this.lastElement = extraElement;
    this.size = list.size() + 1;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public E get(int index) {
    checkIndex(index);
    if (index < size - 1) {
      return list.get(index);
    }
    if (index == size - 1) {
      return lastElement;
    }
    throw new Error("This can't happen.");
  }

  @Override
  public Iterator<E> iterator() {
    return new IteratorPlusOne<E>(list.iterator(), lastElement);
  }

  @Override
  public SimpleList<E> getSublistContaining(int index) {
    checkIndex(index);
    if (index < size - 1) {
      // Not the last element, so recurse.
      return list.getSublistContaining(index);
    }
    if (index == size - 1) { // is lastElement
      // The index is to the last element, which is `lastElement`.
      return this;
    }
    throw new Error("This can't happen.");
  }

  // TODO: Use the version in CollectionsPlume, which is copied to here.  (Check that their bodies
  // are identical.)
  /**
   * An Iterator that returns first the elements returned by its first argument, then its second
   * argument.
   *
   * @param <T> the type of elements of the iterator
   */
  public static final class IteratorPlusOne<T> implements Iterator<T> {
    /** The iterator that this object yields first. */
    Iterator<T> itor1;

    /** The last element that this iterator returns. */
    T lastElement;

    /**
     * True if this iterator has not yet yielded the lastElement element, and therefore is not done.
     */
    boolean hasPlusOne = true;

    /**
     * Create an iterator that returns the elements of {@code itor1} then {@code lastElement}.
     *
     * @param itor1 an Iterator
     * @param lastElement one element
     */
    public IteratorPlusOne(Iterator<T> itor1, T lastElement) {
      this.itor1 = itor1;
      this.lastElement = lastElement;
    }

    @Override
    public boolean hasNext(@GuardSatisfied IteratorPlusOne<T> this) {
      return itor1.hasNext() || hasPlusOne;
    }

    @Override
    public T next(@GuardSatisfied IteratorPlusOne<T> this) {
      if (itor1.hasNext()) {
        return itor1.next();
      } else if (hasPlusOne) {
        hasPlusOne = false;
        return lastElement;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove(@GuardSatisfied IteratorPlusOne<T> this) {
      throw new UnsupportedOperationException();
    }
  }
}
