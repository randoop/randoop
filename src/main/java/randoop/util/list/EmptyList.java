package randoop.util.list;

import java.util.Collections;
import java.util.List;

/**
 * An immutable empty list.
 *
 * @param <E> the type of the list elements
 */
public class EmptyList<E> extends SimpleList<E> {

  /** serialVersionUID */
  private static final long serialVersionUID = 20250719;

  /** Creates an empty list. */
  public EmptyList() {}

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
    throw new IndexOutOfBoundsException("index " + index + " for EmptyList");
  }

  @Override
  public SimpleList<E> getSublistContaining(int index) {
    throw new IndexOutOfBoundsException("index " + index + " for EmptyList");
  }

  @Override
  public List<E> toJDKList() {
    return Collections.emptyList();
  }
}
