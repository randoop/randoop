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

  /**
   * Returns an immutable, empty multimap.
   *
   * @return an immutable, empty multimap
   */
  @SuppressWarnings({"unchecked"})
  public static <K, V> MultiMap<K, V> empty() {
    return EmptyMultiMap.instance;
  }

  /**
   * Adds a key-values mapping to this multimap
   *
   * @param key the key
   * @param values the values
   */
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

  /** An immutable, empty multimap. */
  private static class EmptyMultiMap<K, V> extends MultiMap<K, V> {

    /** The canonical EmptyMultiMap. */
    @SuppressWarnings({"rawtypes"})
    public static EmptyMultiMap instance = new EmptyMultiMap();

    /**
     * Creates an immutable, empty multimap. Should only be called once, because all EmptyMultiMaps
     * are the same.
     */
    private EmptyMultiMap() {
      super(0);
    }

    @Override
    public void put(K key, Collection<? extends V> values) {
      throw new UnsupportedOperationException("EmptyMultiMap.put");
    }

    @Override
    public void addAll(Map<? extends K, ? extends V> m) {
      throw new UnsupportedOperationException("EmptyMultiMap.addAll");
    }

    @Override
    public void addAll(K key, Collection<? extends V> values) {
      throw new UnsupportedOperationException("EmptyMultiMap.addAll");
    }

    @Override
    public void addAll(MultiMap<K, V> mmap) {
      throw new UnsupportedOperationException("EmptyMultiMap.addAll");
    }

    @Override
    public boolean add(K key, V value) {
      throw new UnsupportedOperationException("EmptyMultiMap.add");
    }

    @Override
    public boolean remove(K key, V value) {
      throw new UnsupportedOperationException("EmptyMultiMap.remove");
    }

    @Override
    public boolean remove(K key) {
      throw new UnsupportedOperationException("EmptyMultiMap.remove");
    }

    @Override
    public Set<V> getValues(K key) {
      return Collections.emptySet();
    }

    @Override
    public Set<K> keySet() {
      return Collections.emptySet();
    }

    @Override
    public boolean contains(K obj) {
      return false;
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException("EmptyMultiMap.clear");
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public String toString() {
      return "{}";
    }
  }
}
