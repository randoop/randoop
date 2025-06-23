package randoop.util.list;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
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
    this.lists = lists.toArray[lists.size()];
    this.cumulativeSize = new int[this.lists.length];
    this.size = 0;
    for (int i = 0; i < this.lists.length; i++) {
      SimpleList<E> l = this.lists[i];
      this.size += l.size();
      this.cumulativeSize[i] = this.size;
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
        return lists.get(i).get(index - previousListSize);
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
        return lists.get(i).getSublistContaining(index - previousListSize);
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
