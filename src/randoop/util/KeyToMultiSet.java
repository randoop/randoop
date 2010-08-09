package randoop.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class KeyToMultiSet<T1, T2> {

  private final Map<T1, MultiSet<T2>> map;

  public KeyToMultiSet() {
    map = new LinkedHashMap<T1, MultiSet<T2>>();
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
    MultiSet<T2> values = map.get(key);
    if(values == null) {
      values = new MultiSet<T2>();  
    } 
    values.add(value);
    map.put(key, values);
  }

  public void remove(T1 key, T2 value) {
    MultiSet<T2> values = map.get(key);
    if(values == null) {
      throw new IllegalStateException("No values where found when trying to remove from multiset. Key: " + key + " Variable: " + value);
    } 
    values.remove(value);
  }

  public void remove(T1 key) {
    MultiSet<T2> values = map.get(key);
    if(values == null) {
      throw new IllegalStateException("No values where found when trying to remove from multiset. Key: " + key);
    } 
    map.remove(key);
  }

  public Set<T2> getVariables(T1 key) {
    MultiSet<T2> values = map.get(key);
    if(values == null) return Collections.emptySet();
    return values.getElements();
  }

  public Set<T1> keySet() {
    return map.keySet();
  }

  public boolean contains(T1 obj) {
    return map.containsKey(obj);
  }

  // Removes all keys with an empty set
  public void clean() {
    for (Iterator<Entry<T1, MultiSet<T2>>> iter = map.entrySet().iterator(); iter.hasNext(); ) {
      Entry<T1, MultiSet<T2>> element = iter.next();
      if (element.getValue().isEmpty()) {
        iter.remove();
      }
    }
  }

  public void removeAllInstances(Set<T2> values) {
    for(MultiSet<T2> multiSet:map.values()) {
      multiSet.removeAllInstances(values);
    }
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


}
