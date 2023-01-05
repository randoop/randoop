package randoop.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MultiSet<E> {

  private final Map<E, Integer> frequencyMap;

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
