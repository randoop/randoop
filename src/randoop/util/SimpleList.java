package randoop.util;

import java.util.List;

/**
 * List implementation used by Randoop to store the sequence of
 * <code>Statement</code>s making up a Sequence.
 * <p>
 *
 * IMPLEMENTATION NOTE
 * <p>
 *
 * Randoop's main generator ({@link randoop.ForwardGenerator} creates new
 * sequences by concatenation existing sequences and appending a
 * statement at the end. A naive implementation of concatenation
 * copies the elements of the concatenated sub-sequences into a new
 * list. The first implementation of Sequence concatenation took this
 * approach.
 *
 * <p> When profiling Randoop, we observed that naive concatenation
 * took up a large portion of the tool's running time, and the
 * component set (i.e. the set of stored sequences used to create
 * more sequences) quickly exhausted the memory available.
 *
 * <p> To improve memory and time efficiency, we now do concatenation
 * differently.  We store the list of statements in a Sequence in a
 * SimpleList, an abstract class that has three subclasses:
 *
 * <p>
 * <ul>
 * <li> {@link ArrayListSimpleList}: a typical list is stored as an array list.
 * <li> {@link ListOfLists}: a list that only stores pointers to its constituent
 * sub-lists.
 * <li> {@link OneMoreElementList}: stores a SimpleList plus one additional final
 * element.
 * </ul>
 * <p>
 * When concantenating N Sequences to create a new sequence, we store the
 * concatenated sequence statements in a ListofLists, which takes space (and
 * creation time) proportional to N, not to the length of the new sequence.
 * <p>
 * When extending a Sequence with a new statement, we store the old sequence's
 * statements plus the new statement in a OneMoreElementList, which takes up
 * only 2 references in memory (and constant creation time).
 * <p>
 */
public abstract class SimpleList<T> {

  public abstract int size();

  public abstract T get(int index);

  public final boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns a java.util.List version of this list.
   */
  public abstract List<T> toJDKList();
}
