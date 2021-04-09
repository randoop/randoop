package randoop.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class KeyToMultiSet<K, V> {

  private final Map<K, MultiSet<V>> map;

  public KeyToMultiSet() {
    map = new LinkedHashMap<>();
  }

  public void addAll(Map<? extends K, ? extends V> m) {
    for (K t1 : m.keySet()) {
      add(t1, m.get(t1));
    }
  }

  public void addAll(K key, Collection<? extends V> values) {
    for (V t2 : values) {
      add(key, t2);
    }
  }

  public void add(K key, V value) {
    MultiSet<V> values = map.computeIfAbsent(key, __ -> new MultiSet<>());
    map.put(key, values);
  }

  public void remove(K key, V value) {
    MultiSet<V> values = map.get(key);
    if (values == null) {
      throw new IllegalStateException(
          "No values where found when trying to remove from multiset. Key: "
              + key
              + " Variable: "
              + value);
    }
    values.remove(value);
  }

  public void remove(K key) {
    MultiSet<V> values = map.get(key);
    if (values == null) {
      throw new IllegalStateException(
          "No values where found when trying to remove from multiset. Key: " + key);
    }
    map.remove(key);
  }

  public Set<V> getVariables(K key) {
    MultiSet<V> values = map.get(key);
    if (values == null) {
      return Collections.emptySet();
    }
    return values.getElements();
  }

  public Set<K> keySet() {
    return map.keySet();
  }

  public boolean contains(K obj) {
    return map.containsKey(obj);
  }

  // Removes all keys with an empty set
  public void clean() {
    for (Iterator<Map.Entry<K, MultiSet<V>>> iter = map.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry<K, MultiSet<V>> element = iter.next();
      if (element.getValue().isEmpty()) {
        iter.remove();
      }
    }
  }

  public void removeAllInstances(Set<V> values) {
    for (MultiSet<V> multiSet : map.values()) {
      multiSet.removeAllInstances(values);
    }
  }

  public void clear() {
    map.clear();
  }

  public int size() {
    return map.size();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }
}
