package randoop.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Implements an IMultiMap with a java.util.LinkedHashMap. */
public class MultiMap<K, V> implements IMultiMap<K, V> {

  private final Map<K, Set<V>> map;

  public MultiMap() {
    map = new LinkedHashMap<>();
  }

  public MultiMap(int initialCapacity) {
    map = new LinkedHashMap<>(initialCapacity);
  }

  public void put(K key, Collection<? extends V> values) {
    if (contains(key)) remove(key);
    map.put(key, new LinkedHashSet<V>(values));
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

  public void addAll(MultiMap<K, V> mmap) {
    for (Map.Entry<K, Set<V>> entry : mmap.map.entrySet()) {
      addAll(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public boolean add(K key, V value) {
    Set<V> values = map.computeIfAbsent(key, __ -> new LinkedHashSet<>(1));
    return values.add(value);
  }

  @Override
  public boolean remove(K key, V value) {
    Set<V> values = map.get(key);
    if (values == null) {
      throw new IllegalStateException(
          "No values were found when trying to remove from multiset. Key: "
              + key
              + " Variable: "
              + value);
    }
    return values.remove(value);
  }

  public boolean remove(K key) {
    Set<V> values = map.get(key);
    if (values == null) {
      throw new IllegalStateException(
          "No values were found when trying to remove from multiset. Key: " + key);
    }
    return map.remove(key) != null;
  }

  @Override
  public Set<V> getValues(K key) {
    return map.getOrDefault(key, Collections.emptySet());
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  public boolean contains(K obj) {
    return map.containsKey(obj);
  }

  public void clear() {
    map.clear();
  }

  @Override
  public int size() {
    return map.size();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public String toString() {
    return map.toString();
  }
}
