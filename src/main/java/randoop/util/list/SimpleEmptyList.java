package randoop.util.list;

import java.io.Serializable;
import java.util.List;

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

  /**
   * Creates a new SimpleArrayList containing the given elements.
   *
   * @param c the elements of the list
   */
  private SimpleEmptyList() {}

  @Override
  public SimpleList<E> subList(int startIndex, int toIndex) {
    if (startIndex < 0 || toIndex > size()) {
      throw new IllegalArgumentException(
          String.format("Tried to take subList(%d, %d) of %s", startIndex, toIndex, this));
    }
    if (startIndex == 0 && toIndex == size()) {
      return this;
    }
    return new SimpleList(this.content, this.startIndex + startIndex, this.startIndex + toIndex);
  }

  @Override
  // Return the entire list.
  public SimpleList<E> getSublistContaining(int index) {
    return this;
  }

  @Override
  public List<E> toJDKList() {
    return Collections.emptyList();
  }
}
