package randoop.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.signedness.qual.Signed;

/**
 * A map from a key to a multi-set.
 *
 * @param <K> the type of the keys
 * @param <V> the types of the elements of the value multi-sets
 */
// @Signed so that the values can be printed.
public class KeyToMultiSet<K extends @Signed Object, V extends @Signed Object> {

  /** The backing map. */
  private final Map<K, MultiSet<V>> map;

  /** Creates a new, empty KeyToMultiSet. */
  public KeyToMultiSet() {
    map = new LinkedHashMap<>();
  }

  /**
   * For each entry in the given map, adds its value to the multiset for its key.
   *
   * @param m the key-value pairs to add
   */
  public void addAll(Map<? extends K, ? extends V> m) {
    for (K t1 : m.keySet()) {
      add(t1, m.get(t1));
    }
  }

  /**
   * Adds each of the given values to the multiset for the given key.
   *
   * @param key the key
   * @param values the values to add
   */
  public void addAll(K key, Collection<? extends V> values) {
    for (V t2 : values) {
      add(key, t2);
    }
  }

  /**
   * Map the key to the value (along with any other keys it already maps to).
   *
   * @param key the key
   * @param value the value
   */
  public void add(K key, V value) {
    MultiSet<V> values = map.computeIfAbsent(key, __ -> new MultiSet<>());
    values.add(value);
  }

  /**
   * Removes one occurrence of the given value from the multiset for the given key.
   *
   * @param key the key
   * @param value the value to remove
   * @throws IllegalStateException if the key is not in this map, or its multiset does not contain
   *     the value
   */
  public void remove(K key, V value) {
    MultiSet<V> values = map.get(key);
    if (values == null) {
      throw new IllegalStateException(
          "No values were found when trying to remove from multiset. Key: "
              + key
              + " Variable: "
              + value);
    }
    values.remove(value);
  }

  /**
   * Removes the given key, and its multiset, from this map.
   *
   * @param key the key to remove
   * @throws IllegalStateException if the key is not in this map
   */
  public void remove(K key) {
    MultiSet<V> values = map.get(key);
    if (values == null) {
      throw new IllegalStateException(
          "No values were found when trying to remove from multiset. Key: " + key);
    }
    map.remove(key);
  }

  /**
   * Returns the distinct values in the multiset for the given key.
   *
   * @param key the key
   * @return the distinct values that the key maps to; empty if the key is not in this map
   */
  public Set<V> getVariables(K key) {
    MultiSet<V> values = map.get(key);
    if (values == null) {
      return Collections.emptySet();
    }
    return values.getElements();
  }

  /**
   * Returns the keys of this map.
   *
   * @return the keys of this map
   */
  public Set<K> keySet() {
    return map.keySet();
  }

  /**
   * Returns true if this map contains the given key.
   *
   * @param obj a key
   * @return true if this map contains the given key
   */
  public boolean contains(K obj) {
    return map.containsKey(obj);
  }

  /** Removes all keys that map to an empty set. */
  public void clean() {
    for (Iterator<Map.Entry<K, MultiSet<V>>> iter = map.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry<K, MultiSet<V>> element = iter.next();
      if (element.getValue().isEmpty()) {
        iter.remove();
      }
    }
  }

  /**
   * Removes all occurrences of each of the given values from the multiset of every key.
   *
   * @param values the values to remove
   */
  public void removeAllInstances(Set<V> values) {
    for (MultiSet<V> multiSet : map.values()) {
      multiSet.removeAllInstances(values);
    }
  }

  /** Removes all keys from this map. */
  public void clear() {
    map.clear();
  }

  /**
   * Returns the number of keys in this map.
   *
   * @return the number of keys in this map
   */
  public int size() {
    return map.size();
  }

  /**
   * Returns true if this map contains no keys.
   *
   * @return true if this map contains no keys
   */
  public boolean isEmpty() {
    return map.isEmpty();
  }
}
