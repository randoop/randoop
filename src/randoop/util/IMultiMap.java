package randoop.util;

import java.util.Set;

/**
 * Represents a relation from a set of T1s (the keys) to
 * another set of T2s (the values).
 * In partucular, each key maps to a set of values.
 */
public interface IMultiMap<T1, T2> {

  /**
   * Precondition: the mapping key->value is not already in the map.
   * 
   * @param key cannot be null.
   * @param value cannot be null.
   */
   void add(T1 key, T2 value);

  /**
   * Precondition: the mapping key->value is in the map.
   * 
   * @param key cannot be null.
   * @param value cannot be null.
   */
   void remove(T1 key, T2 value);

  /**
   * Returns the values that the given key maps to. 
   * @param key cannot be null.
   */
   Set<T2> getValues(T1 key);

  /**
   * Returns the set of keys in this map (the domain).
   */
   Set<T1> keySet();

  /**
   * Returns the size of this map: the number of mappings.
   */
   int size();

  /**
   * Returns a String representation of this map.
   */
   String toString();

}
