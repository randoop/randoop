package randoop.util;

import java.util.*;

// TODO I actually don't think I need this in the earlier process, however I'll leave it here for now
public class FrequencyMultiMap<T1, T2> extends MultiMap<T1, T2> {

  private final Map<T1, MultiSet<T2>> map;

  public FrequencyMultiMap() {
    map = new LinkedHashMap<>();
  }

  public FrequencyMultiMap(int i) {
    map = new LinkedHashMap<>(i);
  }

  public void put(T1 key, Collection<? extends T2> values) {
    // DO NOTHING HERE, TODO
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

  @Override
  public void add(T1 key, T2 value) {
    MultiSet<T2> values = map.get(key);
    if (values == null) {
      values = new MultiSet<T2>();
      map.put(key, values);
    }
    values.add(value);
  }

  @Override
  public void remove(T1 key, T2 value) {
    MultiSet<T2> values = map.get(key);
    if (values == null) {
      throw new IllegalStateException(
          "No values where found when trying to remove from multiset. Key: "
              + key
              + " Variable: "
              + value);
    }
    values.remove(value);
  }

  public void remove(T1 key) {
    MultiSet<T2> values = map.get(key);
    if (values == null) {
      throw new IllegalStateException(
          "No values where found when trying to remove from multiset. Key: " + key);
    }
    map.remove(key);
  }

  @Override
  public Set<T2> getValues(T1 key) {
    MultiSet<T2> values = map.get(key);
    if (values == null) return Collections.emptySet();
    return values.getElements();
  }

  @Override
  public Set<T1> keySet() {
    return map.keySet();
  }

  public boolean contains(T1 obj) {
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
