package randoop.util.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.plumelib.util.CollectionsPlume;
import randoop.main.RandoopBug;

/**
 * Given a list of lists, defines methods that can access all the elements as if they were part of a
 * single list, without actually merging the lists.
 *
 * <p>This class is used for performance reasons. We want the ability to select elements collected
 * across several lists, but we observed that creating a brand new list (i.e. via a sequence of
 * List.addAll(..) operations can be very expensive, because it happened in a hot spot (method
 * SequenceCollection.getSequencesThatYield).
 *
 * @param <E> the type of elements of the list
 */
/*package-private*/ class ListOfLists<E> implements SimpleList<E>, Serializable {

  /** serialVersionUID */
  private static final long serialVersionUID = -3307714585442970263L;

  /** The lists themselves. */
  @SuppressWarnings("serial") // TODO: use a serializable type.
  public final List<SimpleList<E>> lists;

  /** The i-th value is the number of elements in the sublists up to the i-th one, inclusive. */
  private int[] cumulativeSize;

  /** The size of this collection. */
  private int totalelements;

  /**
   * Create a ListOfLists from a list of SimpleLists.
   *
   * @param lists the lists that will compose the newly-created ListOfLists
   */
  private ListOfLists(List<SimpleList<E>> lists) {
    this.lists = lists;
    this.cumulativeSize = new int[lists.size()];
    this.totalelements = 0;
    for (int i = 0; i < lists.size(); i++) {
      SimpleList<E> l = lists.get(i);
      this.totalelements += l.size();
      this.cumulativeSize[i] = this.totalelements;
    }
  }

  /**
   * Create a SimpleList from an array of SimpleLists.
   *
   * @param <E2> the type of elements of the list
   * @param lists the lists that will compose the newly-created ListOfLists
   * @return the concatenated lists
   */
  @SuppressWarnings({"unchecked"}) // heap pollution warning
  public static <E2> SimpleList<E2> create(SimpleList<E2>... lists) {
    return create(Arrays.asList(lists));
  }

  /**
   * Create a SimpleList from a list of SimpleLists.
   *
   * @param <E2> the type of elements of the list
   * @param lists the lists that will compose the newly-created ListOfLists
   * @return the concatenated lists
   */
  public static <E2> SimpleList<E2> create(List<SimpleList<E2>> lists) {
    if (lists == null) throw new IllegalArgumentException("param cannot be null");
    if (CollectionsPlume.anyMatch(lists, SimpleList::isEmpty)) {
      // Don't side-effect the parameter `lists`; instead, re-assign it.
      lists = new ArrayList<>(lists);
      lists.removeIf((SimpleList<E2> sl) -> sl.isEmpty());
    }
    int size = lists.size();
    if (size == 0) {
      return new EmptyList<>();
    } else if (size == 1) {
      // I suspect that returning `lists.get(0)` is causing a problem:  aliasing causes undesired
      // side effects.
      // return lists.get(0);
      return new ListOfLists<>(lists);
    } else if (size == 2 && lists.get(1).size() == 1) {
      return new OneMoreElementList<>(lists.get(0), lists.get(1).get(0));
    } else {
      return new ListOfLists<>(lists);
    }
  }

  @Override
  public int size() {
    return this.totalelements;
  }

  @Override
  public boolean isEmpty() {
    return this.totalelements == 0;
  }

  @Override
  public E get(int index) {
    if (index < 0 || index > this.totalelements - 1) {
      throw new IllegalArgumentException("index must be between 0 and size()-1");
    }
    int previousListSize = 0;
    for (int i = 0; i < this.cumulativeSize.length; i++) {
      if (index < this.cumulativeSize[i]) {
        return this.lists.get(i).get(index - previousListSize);
      }
      previousListSize = this.cumulativeSize[i];
    }
    throw new RandoopBug("Indexing error in ListOfLists");
  }

  @Override
  public SimpleList<E> getContainingSublist(int index) {
    if (index < 0 || index > this.totalelements - 1) {
      throw new IllegalArgumentException("index must be between 0 and size()-1");
    }
    int previousListSize = 0;
    for (int i = 0; i < this.cumulativeSize.length; i++) {
      if (index < this.cumulativeSize[i]) {
        // Recurse.
        return lists.get(i).getContainingSublist(index - previousListSize);
      }
      previousListSize = cumulativeSize[i];
    }
    throw new RandoopBug("indexing error in ListOfLists");
  }

  @Override
  public List<E> toJDKList() {
    List<E> result = new ArrayList<>();
    for (SimpleList<E> l : lists) {
      result.addAll(l.toJDKList());
    }
    return result;
  }

  @Override
  public String toString() {
    return toJDKList().toString();
  }
}
