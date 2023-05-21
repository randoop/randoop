package randoop.util;

import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/** A multi-map using key identity rather than equality. */
public class IdentityMultiMap<K, V> {

  /** the underlying map */
  private IdentityHashMap<K, Set<V>> map;

  /** Creates an empty multi-map. */
  public IdentityMultiMap() {
    map = new IdentityHashMap<>();
  }

  /**
   * Adds a key-value pair to the multimap.
   *
   * @param key the key
   * @param value the value
   */
  public void put(K key, V value) {
    Set<V> set = map.computeIfAbsent(key, __ -> new LinkedHashSet<>());
    set.add(value);
  }

  /**
   * Returns the set of values that correspond to the given key in the map.
   *
   * @param key the key value
   * @return the set of values that correspond to the key, null if none
   */
  public Set<V> get(K key) {
    return map.get(key);
  }
}
