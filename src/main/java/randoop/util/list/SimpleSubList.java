package randoop.util.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A view of part of a SimpleList.
 *
 * @param <E> the type of elements of the list
 */
public class SimpleSubList<E> extends SimpleList<E> implements Serializable {

  /** serialVersionUID */
  private static final long serialVersionUID = 20250617;

  // TODO: use an array instead, for efficiency?
  /** The backing storage. */
  private final ArrayList<E> delegate;

  /** The index in `delegate` of the first element in this list. */
  private final int fromIndex;

  /** The index in `delegate` of one past the last element in this list. */
  private final int toIndex;

  /**
   * Creates a new SimpleArrayList containing the given elements.
   *
   * @param c the elements of the list
   * @param fromIndex the index in `delegate` of the first element in this list
   * @param toIndex the index in `delegate` of one past the last element in this list.
   */
  /*package-protected*/ SimpleSubList(SimpleList<E> delegate, int fromIndex, int toIndex) {
    if (fromIndex < 0 || toIndex > delegate.size()) {
      throw new IllegalArgumentException(
          String.format("Tried to take subList(%d, %d) of %s", fromIndex, toIndex, delegate));
    }
    this.delegate = delegate;
    this.fromIndex = fromIndex;
    this.toIndex = toIndex;
  }

  @Override
  // Return the entire list.
  public SimpleList<E> getSublistContaining(int index) {
    return this;
  }

  @Override
  public List<E> toJDKList() {
    return new ArrayList<>(this);
  }
}
