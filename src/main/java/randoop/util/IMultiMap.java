package randoop.util;

import java.util.Set;

/**
 * A multimap, which maps each key to a set of values.
 *
 * @param <K> the type of keys
 * @param <V> the type of elements of values
 */
public interface IMultiMap<K, V> {

  /**
   * Precondition: the mapping key&rarr;value is not already in the map.
   *
   * @param key cannot be null
   * @param value cannot be null
   * @return true if the call modifies this object
   */
  boolean add(K key, V value);

  /**
   * Precondition: the mapping key&rarr;value is in the map.
   *
   * @param key cannot be null
   * @param value cannot be null
   * @return true if the call modifies this object
   */
  boolean remove(K key, V value);

  /**
   * Returns the values that the given key maps to.
   *
   * <p>For efficiency, this is permitted to be (but is not guaranteed to be) part of the internal
   * representation. Clients must not modify the return value (but may make a copy and modify the
   * copy).
   *
   * @param key cannot be null
   * @return the set of values for the given key
   */
  Set<V> getValues(K key);

  /**
   * Returns the set of keys in this map (the domain).
   *
   * @return the set of keys in this map
   */
  Set<K> keySet();

  /**
   * Returns the size of this map: the number of mappings.
   *
   * @return the number of entries in this map
   */
  int size();

  /**
   * Returns a String representation of this map.
   *
   * @return a String representation of this map
   */
  @Override
  String toString();
}
