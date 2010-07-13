package randoop.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MultiSet<T> {

  private final Map<T, Integer> frequencyMap; 

  public MultiSet() {
    frequencyMap = new LinkedHashMap<T, Integer>();
  }

  public void add(T obj) {
    Integer i = frequencyMap.get(obj);
    if(i == null) {
      i = 0;
    }
    frequencyMap.put(obj, i + 1);     
  }

  public void remove(T obj) {
    Integer i = frequencyMap.get(obj);
    if(i == null || i < 1) {
      throw new IllegalStateException("Variable not found when trying to remove from multiset. Variable: " + obj);
    } 
    if(i==1) frequencyMap.remove(obj);
    else frequencyMap.put(obj, i - 1);     
  }

  public Set<T> getElements() {
    return frequencyMap.keySet();
  }

  public boolean isEmpty() {
    return frequencyMap.isEmpty();
  }

  public void removeAllInstances(Set<T> values) {
    for(T value:values) {
      frequencyMap.remove(value);
    }
  }
}
