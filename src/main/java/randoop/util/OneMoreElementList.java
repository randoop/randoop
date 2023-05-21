package randoop.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class OneMoreElementList<E> implements SimpleList<E>, Serializable {

  private static final long serialVersionUID = 1332963552183905833L;

  /** The last element in this. */
  @SuppressWarnings("serial")
  public final E lastElement;

  /** All but the last element in this. */
  @SuppressWarnings("serial") // TODO: use a serializable type.
  public final SimpleList<E> list;

  /** The size of this. */
  public final int size;

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
    if (index < size - 1) {
      return list.get(index);
    }
    if (index == size - 1) {
      return lastElement;
    }
    throw new IndexOutOfBoundsException("No such element: " + index);
  }

  @Override
  public SimpleList<E> getSublist(int index) {
    if (index == size - 1) { // is lastElement
      return this;
    }
    // Not the last element, so recurse.
    if (index < size - 1) {
      return list.getSublist(index);
    }
    throw new IndexOutOfBoundsException("No such index: " + index);
  }

  @Override
  public List<E> toJDKList() {
    List<E> result = new ArrayList<>();
    result.addAll(list.toJDKList());
    result.add(lastElement);
    return result;
  }

  @Override
  public String toString() {
    return toJDKList().toString();
  }
}
