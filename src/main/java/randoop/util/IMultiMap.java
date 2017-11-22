package randoop.util;

import java.util.Set;

/** A multimap, which maps each key to a set of values. */
public interface IMultiMap<T1, T2> {

  /**
   * Precondition: the mapping key&rarr;value is not already in the map.
   *
   * @param key cannot be null
   * @param value cannot be null
   */
  void add(T1 key, T2 value);

  /**
   * Precondition: the mapping key&rarr;value is in the map.
   *
   * @param key cannot be null
   * @param value cannot be null
   */
  void remove(T1 key, T2 value);

  /**
   * Returns the values that the given key maps to.
   *
   * @param key cannot be null
   * @return the set of values for the given key
   */
  Set<T2> getValues(T1 key);

  /**
   * Returns the set of keys in this map (the domain).
   *
   * @return the set of keys in this map
   */
  Set<T1> keySet();

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
