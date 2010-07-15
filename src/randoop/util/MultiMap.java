package randoop.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implements an IMultiMap with a java.util.LinkedHashMap.
 */
public class MultiMap<T1, T2> implements IMultiMap<T1, T2> {

  private final Map<T1, Set<T2>> map;

  public MultiMap() {
    map = new LinkedHashMap<T1, Set<T2>>();
  }

  public MultiMap(int i) {
    map = new LinkedHashMap<T1, Set<T2>>(i);
  }

  public void put(T1 key, Collection<? extends T2> values) {
    if (contains(key))
      remove(key);
    map.put(key, new LinkedHashSet<T2>(values));
  }

  public void addAll(Map<? extends T1, ? extends T2> m) {
    for (T1 t1 : m.keySet()) {
      add(t1, m.get(t1));
    }
  }

  public void addAll(T1 key, Collection<? extends T2> values) {
    for (T2 t2 : values) {
      add(key, t2);
    }
  }

  public void add(T1 key, T2 value) {
    Set<T2> values = map.get(key);
    if(values == null) {
      values = new LinkedHashSet<T2>(1);
      map.put(key, values);
    }
    values.add(value);
  }

  public void remove(T1 key, T2 value) {
    Set<T2> values = map.get(key);
    if(values == null) {
      throw new IllegalStateException("No values where found when trying to remove from multiset. Key: " + key + " Variable: " + value);
    }
    values.remove(value);
  }

  public void remove(T1 key) {
    Set<T2> values = map.get(key);
    if(values == null) {
      throw new IllegalStateException("No values where found when trying to remove from multiset. Key: " + key);
    }
    map.remove(key);
  }

  public Set<T2> getValues(T1 key) {
    Set<T2> values = map.get(key);
    if(values == null) return Collections.emptySet();
    return values;
  }

  public Set<T1> keySet() {
    return map.keySet();
  }

  public boolean contains(T1 obj) {
    return map.containsKey(obj);
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

  @Override
  public String toString() {
    return map.toString();
  }
}
