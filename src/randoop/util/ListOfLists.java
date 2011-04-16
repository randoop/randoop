package randoop.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Given a list of lists, defines methods that can access all the elements as if
 * they were part of a single list, without actually merging the lists.
 * 
 * This class is used for performance reasons. We want the ability to select
 * elements collected across several lists, but we observed that creating a
 * brand new list (i.e. via a sequence of List.addAll(..) operations can be very
 * expensive, because it happened in a hot spot (method
 * SequenceCollection.getSequencesThatYield).
 */
public class ListOfLists<T> extends SimpleList<T> implements Serializable {

  private static final long serialVersionUID = -3307714585442970263L;

  public final List<SimpleList<T>> lists;

  private int[] accumulatedSize;

  private int totalelements;

  @SuppressWarnings({"varargs","unchecked"}) // heap pollution warning
  public ListOfLists(SimpleList<T>... lists) {
    this.lists = new ArrayList<SimpleList<T>>(lists.length);
    for (SimpleList<T> sl : lists) {
      this.lists.add(sl);
    }
    this.accumulatedSize = new int[lists.length];
    this.totalelements = 0;
    for (int i = 0; i < lists.length ; i++) {
      SimpleList<T> l = lists[i];
      if (l == null)
        throw new IllegalArgumentException("All lists should be non-null");
      this.totalelements += l.size();
      this.accumulatedSize[i] = this.totalelements;
    }
  }
  
  public ListOfLists(List<SimpleList<T>> lists) {
    if (lists == null)
      throw new IllegalArgumentException("param cannot be null");
    this.lists = lists;
    this.accumulatedSize = new int[lists.size()];
    this.totalelements = 0;
    for (int i = 0; i < lists.size(); i++) {
      SimpleList<T> l = lists.get(i);
      if (l == null)
        throw new IllegalArgumentException("All lists should be non-null");
      this.totalelements += l.size();
      this.accumulatedSize[i] = this.totalelements;
    }
  }

  /* (non-Javadoc)
   * @see randoop.util.SimpleList#size()
   */
  @Override
  public int size() {
    return this.totalelements;
  }

  /* (non-Javadoc)
   * @see randoop.util.SimpleList#get(int)
   */
  @Override
  public T get(int index) {
    if (index < 0 || index > this.totalelements - 1)
      throw new IllegalArgumentException(
          "index must be between 0 and size()-1");
    int previousListSize = 0;
    for (int i = 0; i < this.accumulatedSize.length; i++) {
      if (index < this.accumulatedSize[i])
        return this.lists.get(i).get(index - previousListSize);
      previousListSize = this.accumulatedSize[i];
    }
    throw new RuntimeException(
        "This point shouldn't be reached (bug in randoop)");
  }

  @Override
  public List<T> toJDKList() {
    List<T> result= new ArrayList<T>();
    for (SimpleList<T> l : lists) {
      result.addAll(l.toJDKList());
    }
    return result;
  }
  
  @Override
  public String toString() {
    return toJDKList().toString();
  }
}
