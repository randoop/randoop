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

  // TODO: use an array instead, for efficiency?
  /** The backing storage. */
  final ArrayList<E> delegate;

  /** The index of the first element in the list. */
  final int fromIndex;

  /** The index of the last element in the list. */
  final int toIndex;

  /** serialVersionUID */
  private static final long serialVersionUID = 20250617;

  /**
   * Creates a new SimpleArrayList containing the given elements.
   *
   * @param c the elements of the list
   */
  /*package-protected*/ SimpleSubList(SimpleList<E> content, int fromIndex, int toIndex) {
    delegate = new ArrayList<>(c);
    super(c);
  }

  public SimpleList<E> create(SimpleList<E> content, int fromIndex, int toIndex) {
    if (fromIndex < 0 || toIndex > content.size()) {
      throw new IllegalArgumentException(
          String.format("Tried to take subList(%d, %d) of %s", fromIndex, toIndex, content));
    }
    this.content = content;
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
