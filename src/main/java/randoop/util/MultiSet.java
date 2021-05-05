package randoop.util;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Set;

public class MultiSet<E> {

  private final Object2IntMap<E> frequencyMap;

  public MultiSet() {
    frequencyMap = new Object2IntLinkedOpenHashMap<>();
  }

  public void add(E obj) {
    int i = frequencyMap.getOrDefault(obj, 0);
    frequencyMap.put(obj, i + 1);
  }

  public void remove(E obj) {
    int i = frequencyMap.getOrDefault(obj, -1);
    if (i < 1) {
      throw new IllegalStateException(
          "Variable not found when trying to remove from multiset. Variable: " + obj);
    }
    if (i == 1) {
      frequencyMap.removeInt(obj);
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
      frequencyMap.removeInt(value);
    }
  }
}
