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

  public void add(E obj) {
    Integer i = frequencyMap.getOrDefault(obj, 0);
    frequencyMap.put(obj, i + 1);
  }

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

  public Set<E> getElements() {
    return frequencyMap.keySet();
  }

  public boolean isEmpty() {
    return frequencyMap.isEmpty();
  }

  public void removeAllInstances(Set<E> values) {
    for (E value : values) {
      frequencyMap.remove(value);
    }
  }
}
