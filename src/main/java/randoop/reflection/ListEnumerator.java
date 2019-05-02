package randoop.reflection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Enumerates the set of lists formed by selecting values sequentially from a list of candidates,
 * such that each generlist has a value from each candidate list. the length of the lists . For
 * instance, given {@code [["a1", "a2"], ["b1"], ["c1", "c2", "c3"]]} enumerates
 *
 * <pre>
 *   ["a1", "b1", "c1"]
 *   ["a1", "b1", "c2"]
 *   ["a1", "b1", "c3"]
 *   ["a2", "b1", "c1"]
 *   ["a2", "b1", "c2"]
 *   ["a2", "b1", "c3"]
 * </pre>
 *
 * by successive calls to {@link #next()}.
 */
class ListEnumerator<T> implements Iterator<List<T>> {

  /** Lists of candidate values for each position in generated lists. */
  private final List<List<T>> candidates;
  /** Iterators for each list of candidate values. */
  private final List<Iterator<T>> iterators;
  /** The partially constructed next value. */
  private final List<T> currentTypes;
  /** The current position for which to select a value. */
  private int nextList;

  /**
   * Creates a {@link ListEnumerator} for lists constructed from the given candidates. Each
   * generated list will be the same length as the given list.
   *
   * @param candidates lists of candidate values for each position in generated lists
   */
  ListEnumerator(List<List<T>> candidates) {
    this.candidates = candidates;
    this.iterators = new ArrayList<>(candidates.size());
    this.currentTypes = new ArrayList<>(candidates.size());
    for (List<T> list : candidates) {
      iterators.add(list.iterator());
      currentTypes.add(null);
    }
    this.nextList = 0;
  }

  @Override
  public boolean hasNext() {
    while (nextList >= 0 && !iterators.get(nextList).hasNext()) {
      iterators.set(nextList, candidates.get(nextList).iterator());
      nextList--;
    }
    if (nextList >= 0) {
      return true;
    }
    nextList = 0;
    return false;
  }

  @Override
  public List<T> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    while (nextList < candidates.size() && iterators.get(nextList).hasNext()) {
      currentTypes.set(nextList, iterators.get(nextList).next());
      nextList++;
    }
    if (nextList < candidates.size()) {
      throw new NoSuchElementException();
    }
    nextList--;
    return new ArrayList<>(currentTypes);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException(
        "Remove not implemented for randoop.reflection.SubstitutionEnumerator");
  }
}
