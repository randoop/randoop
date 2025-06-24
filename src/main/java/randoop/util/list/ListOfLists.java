package randoop.util.list;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.plumelib.util.CollectionsPlume;

/**
 * Given a list of lists, defines methods that can access all the elements as if they were part of a
 * single list, without copying any list contents.
 *
 * @param <E> the type of elements of the list
 */
/*package-private*/ class ListOfLists<E> extends SimpleList<E> implements Serializable {

  /** serialVersionUID */
  private static final long serialVersionUID = -3307714585442970263L;

  /** The lists themselves. */
  @SuppressWarnings("serial") // TODO: use a serializable type.
  // TODO: use an array for efficiency, just as `cumulativeSize` is.
  private final SimpleList<E>[] lists;

  /** The i-th value is the number of elements in the sublists up to the i-th one, inclusive. */
  private int[] cumulativeSize;

  /** The size of this collection. */
  private int size;

  /**
   * Create a ListOfLists from a list of SimpleLists.
   *
   * @param lists the lists that will compose the newly-created ListOfLists
   */
  /*package-private*/ ListOfLists(List<SimpleList<E>> lists) {
    // TODO: have a variant that doesn't make a copy?
    @SuppressWarnings({
      "unchecked",
      "nullness:assignment",
      "nullness:toarray.nullable.elements.not.newarray" // bug in CF: doesn't permit cast
    })
    @NonNull SimpleList<E>[] tmpLists = lists.toArray((SimpleList<E>[]) new Object[lists.size()]);
    this.lists = tmpLists;
    this.cumulativeSize = new int[this.lists.length];
    this.size = 0;
    for (int i = 0; i < this.lists.length; i++) {
      SimpleList<E> l = this.lists[i];
      size += l.size();
      cumulativeSize[i] = size;
    }
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
    int previousListSize = 0;
    for (int i = 0; i < cumulativeSize.length; i++) {
      if (index < cumulativeSize[i]) {
        return lists[i].get(index - previousListSize);
      }
      previousListSize = cumulativeSize[i];
    }
    throw new Error("This can't happen.");
  }

  @Override
  public SimpleList<E> getSublistContaining(int index) {
    checkIndex(index);
    int previousListSize = 0;
    for (int i = 0; i < cumulativeSize.length; i++) {
      if (index < cumulativeSize[i]) {
        // Recurse.
        return lists[i].getSublistContaining(index - previousListSize);
      }
      previousListSize = cumulativeSize[i];
    }
    throw new Error("This can't happen.");
  }

  @Override
  public Iterator<E> iterator() {
    List<Iterator<E>> itors = CollectionsPlume.mapList(SimpleList::iterator, lists);
    return new CollectionsPlume.MergedIterator<>(itors.iterator());
  }
}
