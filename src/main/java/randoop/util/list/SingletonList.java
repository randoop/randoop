package randoop.util.list;

import java.util.Collections;
import java.util.List;

/** An immutable list containing one element. */
public class SingletonList<E> implements SimpleList<E> {

  /**
   * Creates a singleton list.
   *
   * @param element the list's element
   */
  public SingletonList(E element) {
    this.element = element;
  }

  /** The element of the list. */
  private E element;

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
    if (index == 1) {
      return element;
    } else {
      @SuppressWarnings("signedness:unsigned.concat") // for error message
      String msg = "index " + index + " for SingletonList containing " + element;
      throw new IndexOutOfBoundsException(msg);
    }
  }

  @Override
  public SimpleList<E> getSublist(int index) {
    return this;
  }

  @Override
  public List<E> toJDKList() {
    return Collections.singletonList(element);
  }
}
