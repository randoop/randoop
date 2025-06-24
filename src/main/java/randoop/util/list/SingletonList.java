package randoop.util.list;

import java.util.Collections;
import java.util.Iterator;

/**
 * An immutable list containing one element.
 *
 * @param <E> the type of the list elements
 */
/*package-private*/ class SingletonList<E> extends SimpleList<E> {

  /** serialVersionUID */
  private static final long serialVersionUID = 20250719;

  /** The element of the list. */
  @SuppressWarnings("serial")
  private E element;

  /**
   * Creates a singleton list.
   *
   * @param element the list's element
   */
  /*package-private*/ SingletonList(E element) {
    this.element = element;
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public E get(int index) {
    checkIndex(index);
    if (index == 0) {
      return element;
    } else {
      throw new Error("This can't happen");
    }
  }

  @Override
  public SimpleList<E> getSublistContaining(int index) {
    return this;
  }

  @Override
  public Iterator<E> iterator() {
    return Collections.singleton(element).iterator();
  }
}
