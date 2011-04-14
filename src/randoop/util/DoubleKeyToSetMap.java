package randoop.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Map indexed with 2 keys.
 */
public final class DoubleKeyToSetMap<K1, K2, V> {
  private final DoubleKeyMap<K1, K2, Set<V>> dkMap;

  public DoubleKeyToSetMap() {
    dkMap = new DoubleKeyMap<K1, K2, Set<V>>();
  }

  public Set<K1> getK1Set() {
    return dkMap.getK1Set();
  }

  public Set<K2> getK2Set(K1 k1) {
    return dkMap.getK2Set(k1);
  }

  public boolean containsKeys(K1 k1, K2 k2) {
    return dkMap.containsKeys(k1, k2);
  }

  public Set<V> get(K1 k1, K2 k2) {
    Set<V> ret = dkMap.get(k1, k2);
    if (ret == null) {
      return Collections.emptySet();
    } else {
      return Collections.unmodifiableSet(ret);
    }
  }

  private void ensureSetExists(K1 k1, K2 k2) {
    if (!dkMap.containsKeys(k1, k2)) {
      dkMap.put(k1, k2, new LinkedHashSet<V>());
    }
  }

  /**
   * Returns true if the doubly-nested set did not contain v.
   */
   public boolean add(K1 k1, K2 k2, V v) {
     ensureSetExists(k1, k2);
     return dkMap.get(k1, k2).add(v);
   }

   /**
    * Returns true if the doubly-nested set contained v.
    */
   public boolean remove(K1 k1, K2 k2, V v) {
     ensureSetExists(k1, k2);
     return dkMap.get(k1, k2).remove(v);
   }

   /**
    * Returns true if the doubly-nested set did not contain all of
    * the vs.
    */
   public boolean addAll(K1 k1, K2 k2, Set<V> vs) {
     ensureSetExists(k1, k2);
     return dkMap.get(k1, k2).addAll(vs);
   }

   @Override
   public String toString() {
     return dkMap.toString();
   }

   public void clear() {
     dkMap.clear();
   }

   public Set<V> getAllVs() {
     Set<V> result= new LinkedHashSet<V>();
     for (K1 k1 : getK1Set()) {
       for (K2 k2 : getK2Set(k1)) {
         result.addAll(get(k1, k2));
       }
     }
     return Collections.unmodifiableSet(result);
   }
}
