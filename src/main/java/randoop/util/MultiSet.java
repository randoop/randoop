package randoop.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.signedness.qual.Signed;

/**
 * A multiset: each value may appear multiple times.
 *
 * @param <E> the type of elements
 */
public class MultiSet<E extends @Signed Object> {

  /** How often each element appears in this multiset. */
  private final Map<E, Integer> frequencyMap;

  /** Creates a new, empty MultiSet. */
  public MultiSet() {
    frequencyMap = new LinkedHashMap<>();
  }

  /**
   * Adds one occurrence of the given element to this multiset.
   *
   * @param obj the element to add
   */
  public void add(E obj) {
    Integer i = frequencyMap.getOrDefault(obj, 0);
    frequencyMap.put(obj, i + 1);
  }

  /**
   * Removes one occurrence of the given element from this multiset.
   *
   * @param obj the element to remove
   * @throws IllegalStateException if the element does not appear in this multiset
   */
  public void remove(E obj) {
    Integer i = frequencyMap.get(obj);
    if (i == null || i < 1) {
      throw new IllegalStateException(
          "Variable not found when trying to remove from multiset. Variable: " + obj);
    }
    if (i == 1) {
      frequencyMap.remove(obj);
    } else {
      frequencyMap.put(obj, i - 1);
    }
  }

  /**
   * Returns the distinct elements of this multiset. The result is a view backed by this multiset.
   *
   * @return the distinct elements of this multiset
   */
  public Set<E> getElements() {
    return frequencyMap.keySet();
  }

  /**
   * Returns true if this multiset contains no elements.
   *
   * @return true if this multiset contains no elements
   */
  public boolean isEmpty() {
    return frequencyMap.isEmpty();
  }

  /**
   * Removes all occurrences of each of the given elements from this multiset.
   *
   * @param values the elements to remove
   */
  public void removeAllInstances(Set<E> values) {
    for (E value : values) {
      frequencyMap.remove(value);
    }
  }
}
