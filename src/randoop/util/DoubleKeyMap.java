package randoop.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Map indexed with 2 keys.
 */
public final class DoubleKeyMap<K1, K2, V> {

  private final Map<K1, Map<K2, V>> fMap;

  public DoubleKeyMap() {
    fMap= new LinkedHashMap<K1, Map<K2, V>>();
  }

  public Set<K1> getK1Set() {
    return Collections.unmodifiableSet(fMap.keySet());
  }

  public Set<K2> getK2Set(K1 k1) {
    return Collections.unmodifiableSet(getSecondLevel(k1).keySet());
  }

  public Set<V> getVSet(K1 k1) {
    return Collections.unmodifiableSet(new LinkedHashSet<V>(getSecondLevel(k1).values()));
  }

  private Map<K2, V> getSecondLevel(K1 k1) {
    if (fMap.containsKey(k1))
      return fMap.get(k1);
    else
      return Collections.emptyMap();
  }

  public boolean containsKeys(K1 k1, K2 k2) {
    Map<K2, V> secondLevel= fMap.get(k1);// get directly, for speed
    if (secondLevel == null)
      return false;
    else
      return secondLevel.containsKey(k2);
  }

  public V get(K1 k1, K2 k2) {
    Map<K2,V> secondLevel= fMap.get(k1);// get directly, for speed
    if (secondLevel == null)
      return null;
    return secondLevel.get(k2);
  }

  public V put(K1 k1, K2 k2, V v) {
    Map<K2, V> secLvl= fMap.get(k1);// get directly, for speed
    if (secLvl == null) {
      secLvl= new LinkedHashMap<K2,V>(2);
      fMap.put(k1, secLvl);
    }
    V oldV= secLvl.put(k2, v);
    return oldV;
  }

  @Override
  public String toString() {
    return fMap.toString();
  }

  public void clear() {
    fMap.clear();
  }

  public Set<V> getAllVs() {
    Set<V> result= new LinkedHashSet<V>();
    Set<K1> k1Set= getK1Set();
    for (K1 k1 : k1Set) {
      result.addAll(getVSet(k1));
    }
    return Collections.unmodifiableSet(result);
  }
}
