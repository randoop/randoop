package randoop.util;

import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A multi-map using key identity rather than equality.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public class IdentityMultiMap<K, V> {

  /** The underlying map. */
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
  public @Nullable Set<V> get(K key) {
    return map.get(key);
  }
}
