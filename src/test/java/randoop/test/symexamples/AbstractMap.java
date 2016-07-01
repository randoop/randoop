package randoop.test.symexamples;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public abstract class AbstractMap implements Map {
  protected AbstractMap() {}

  public int size() {
    return entrySet().size();
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public boolean containsValue(Object value) {
    Iterator i = entrySet().iterator();
    if ((((value == null) && ++randoopCoverageInfo.branchTrue[4] != 0)
        || ++randoopCoverageInfo.branchFalse[4] == 0)) {
      while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[1] != 0)
          || ++randoopCoverageInfo.branchFalse[1] == 0)) {
        Entry e = (Entry) i.next();
        if ((((e.getValue() == null) && ++randoopCoverageInfo.branchTrue[0] != 0)
            || ++randoopCoverageInfo.branchFalse[0] == 0)) return true;
      }
    } else {
      while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[3] != 0)
          || ++randoopCoverageInfo.branchFalse[3] == 0)) {
        Entry e = (Entry) i.next();
        if ((((value.equals(e.getValue())) && ++randoopCoverageInfo.branchTrue[2] != 0)
            || ++randoopCoverageInfo.branchFalse[2] == 0)) return true;
      }
    }
    return false;
  }

  public boolean containsKey(Object key) {
    Iterator i = entrySet().iterator();
    if ((((key == null) && ++randoopCoverageInfo.branchTrue[9] != 0)
        || ++randoopCoverageInfo.branchFalse[9] == 0)) {
      while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[6] != 0)
          || ++randoopCoverageInfo.branchFalse[6] == 0)) {
        Entry e = (Entry) i.next();
        if ((((e.getKey() == null) && ++randoopCoverageInfo.branchTrue[5] != 0)
            || ++randoopCoverageInfo.branchFalse[5] == 0)) return true;
      }
    } else {
      while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[8] != 0)
          || ++randoopCoverageInfo.branchFalse[8] == 0)) {
        Entry e = (Entry) i.next();
        if ((((key.equals(e.getKey())) && ++randoopCoverageInfo.branchTrue[7] != 0)
            || ++randoopCoverageInfo.branchFalse[7] == 0)) return true;
      }
    }
    return false;
  }

  public Object get(Object key) {
    Iterator i = entrySet().iterator();
    if ((((key == null) && ++randoopCoverageInfo.branchTrue[14] != 0)
        || ++randoopCoverageInfo.branchFalse[14] == 0)) {
      while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[11] != 0)
          || ++randoopCoverageInfo.branchFalse[11] == 0)) {
        Entry e = (Entry) i.next();
        if ((((e.getKey() == null) && ++randoopCoverageInfo.branchTrue[10] != 0)
            || ++randoopCoverageInfo.branchFalse[10] == 0)) return e.getValue();
      }
    } else {
      while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[13] != 0)
          || ++randoopCoverageInfo.branchFalse[13] == 0)) {
        Entry e = (Entry) i.next();
        if ((((key.equals(e.getKey())) && ++randoopCoverageInfo.branchTrue[12] != 0)
            || ++randoopCoverageInfo.branchFalse[12] == 0)) return e.getValue();
      }
    }
    return null;
  }

  public Object put(Object key, Object value) {
    throw new UnsupportedOperationException();
  }

  public Object remove(Object key) {
    Iterator i = entrySet().iterator();
    Entry correctEntry = null;
    if ((((key == null) && ++randoopCoverageInfo.branchTrue[19] != 0)
        || ++randoopCoverageInfo.branchFalse[19] == 0)) {
      while ((((correctEntry == null && i.hasNext()) && ++randoopCoverageInfo.branchTrue[16] != 0)
          || ++randoopCoverageInfo.branchFalse[16] == 0)) {
        Entry e = (Entry) i.next();
        if ((((e.getKey() == null) && ++randoopCoverageInfo.branchTrue[15] != 0)
            || ++randoopCoverageInfo.branchFalse[15] == 0)) correctEntry = e;
      }
    } else {
      while ((((correctEntry == null && i.hasNext()) && ++randoopCoverageInfo.branchTrue[18] != 0)
          || ++randoopCoverageInfo.branchFalse[18] == 0)) {
        Entry e = (Entry) i.next();
        if ((((key.equals(e.getKey())) && ++randoopCoverageInfo.branchTrue[17] != 0)
            || ++randoopCoverageInfo.branchFalse[17] == 0)) correctEntry = e;
      }
    }
    Object oldVariable = null;
    if ((((correctEntry != null) && ++randoopCoverageInfo.branchTrue[20] != 0)
        || ++randoopCoverageInfo.branchFalse[20] == 0)) {
      oldVariable = correctEntry.getValue();
      i.remove();
    }
    return oldVariable;
  }

  public void putAll(Map t) {
    Iterator i = t.entrySet().iterator();
    while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[21] != 0)
        || ++randoopCoverageInfo.branchFalse[21] == 0)) {
      Entry e = (Entry) i.next();
      put(e.getKey(), e.getValue());
    }
  }

  public void clear() {
    entrySet().clear();
  }

  transient volatile Set keySet = null;

  transient volatile Collection values = null;

  public Set keySet() {
    if ((((keySet == null) && ++randoopCoverageInfo.branchTrue[22] != 0)
        || ++randoopCoverageInfo.branchFalse[22] == 0)) {
      keySet =
          new AbstractSet() {
            @Override
            public Iterator iterator() {
              return new Iterator() {
                private Iterator i = entrySet().iterator();

                public boolean hasNext() {
                  return i.hasNext();
                }

                public Object next() {
                  return ((Entry) i.next()).getKey();
                }

                public void remove() {
                  i.remove();
                }
              };
            }

            @Override
            public int size() {
              return AbstractMap.this.size();
            }

            @Override
            public boolean contains(Object k) {
              return AbstractMap.this.containsKey(k);
            }
          };
    }
    return keySet;
  }

  public Collection values() {
    if ((((values == null) && ++randoopCoverageInfo.branchTrue[23] != 0)
        || ++randoopCoverageInfo.branchFalse[23] == 0)) {
      values =
          new AbstractCollection() {
            @Override
            public Iterator iterator() {
              return new Iterator() {
                private Iterator i = entrySet().iterator();

                public boolean hasNext() {
                  return i.hasNext();
                }

                public Object next() {
                  return ((Entry) i.next()).getValue();
                }

                public void remove() {
                  i.remove();
                }
              };
            }

            @Override
            public int size() {
              return AbstractMap.this.size();
            }

            @Override
            public boolean contains(Object v) {
              return AbstractMap.this.containsValue(v);
            }
          };
    }
    return values;
  }

  @Override
  public boolean equals(Object o) {
    if ((((o == this) && ++randoopCoverageInfo.branchTrue[24] != 0)
        || ++randoopCoverageInfo.branchFalse[24] == 0)) return true;
    if ((((!(o instanceof Map)) && ++randoopCoverageInfo.branchTrue[25] != 0)
        || ++randoopCoverageInfo.branchFalse[25] == 0)) return false;
    Map t = (Map) o;
    if ((((t.size() != size()) && ++randoopCoverageInfo.branchTrue[26] != 0)
        || ++randoopCoverageInfo.branchFalse[26] == 0)) return false;
    try {
      Iterator i = entrySet().iterator();
      while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[30] != 0)
          || ++randoopCoverageInfo.branchFalse[30] == 0)) {
        Entry e = (Entry) i.next();
        Object key = e.getKey();
        Object value = e.getValue();
        if ((((value == null) && ++randoopCoverageInfo.branchTrue[29] != 0)
            || ++randoopCoverageInfo.branchFalse[29] == 0)) {
          if ((((!(t.get(key) == null && t.containsKey(key)))
                  && ++randoopCoverageInfo.branchTrue[27] != 0)
              || ++randoopCoverageInfo.branchFalse[27] == 0)) return false;
        } else {
          if ((((!value.equals(t.get(key))) && ++randoopCoverageInfo.branchTrue[28] != 0)
              || ++randoopCoverageInfo.branchFalse[28] == 0)) return false;
        }
      }
    } catch (ClassCastException unused) {
      return false;
    } catch (NullPointerException unused) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int h = 0;
    Iterator i = entrySet().iterator();
    while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[31] != 0)
        || ++randoopCoverageInfo.branchFalse[31] == 0)) h += i.next().hashCode();
    return h;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("{");
    Iterator i = entrySet().iterator();
    boolean hasNext = i.hasNext();
    while ((((hasNext) && ++randoopCoverageInfo.branchTrue[33] != 0)
        || ++randoopCoverageInfo.branchFalse[33] == 0)) {
      Entry e = (Entry) (i.next());
      Object key = e.getKey();
      Object value = e.getValue();
      buf.append((key == this ? "(this Map)" : key) + "=" + (value == this ? "(this Map)" : value));
      hasNext = i.hasNext();
      if ((((hasNext) && ++randoopCoverageInfo.branchTrue[32] != 0)
          || ++randoopCoverageInfo.branchFalse[32] == 0)) buf.append(", ");
    }
    buf.append("}");
    return buf.toString();
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    AbstractMap result = (AbstractMap) super.clone();
    result.keySet = null;
    result.values = null;
    return result;
  }

  static class SimpleEntry implements Entry {
    Object key;

    Object value;

    public SimpleEntry(Object key, Object value) {
      this.key = key;
      this.value = value;
    }

    public SimpleEntry(Map.Entry e) {
      this.key = e.getKey();
      this.value = e.getValue();
    }

    public Object getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }

    public Object setValue(Object value) {
      Object oldVariable = this.value;
      this.value = value;
      return oldVariable;
    }

    @Override
    public boolean equals(Object o) {
      if ((((!(o instanceof Map.Entry)) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0)) return false;
      Map.Entry e = (Map.Entry) o;
      return eq(key, e.getKey()) && eq(value, e.getValue());
    }

    @Override
    public int hashCode() {
      Object v;
      return ((key == null) ? 0 : key.hashCode()) ^ ((value == null) ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
      return key + "=" + value;
    }

    private static boolean eq(Object o1, Object o2) {
      return (o1 == null ? o2 == null : o1.equals(o2));
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" boolean containsValue(Object value) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" boolean containsKey(Object key) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        indexList.add(13);
        indexList.add(14);
        methodToIndices.put(" Object get(Object key) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" Object remove(Object key) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        methodToIndices.put(" void putAll(Map t) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(22);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(23);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(31);
        methodToIndices.put(" int hashCode() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(32);
        indexList.add(33);
        methodToIndices.put(" String toString() ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(1, methodToIndices);
    }
  }

  public SortedMap tailMap(Object fromKey) {
    return null;
  }

  public Comparator comparator() {
    return null;
  }

  public Object firstKey() {
    return null;
  }

  public Object lastKey() {
    return null;
  }

  public Set entrySet() {
    return null;
  }

  public SortedMap subMap(Object fromKey, Object toKey) {
    return null;
  }

  public SortedMap headMap(Object toKey) {
    return null;
  }

  private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

  static {
    java.util.Map<String, java.util.Set<Integer>> methodToIndices = new java.util.LinkedHashMap<>();
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      indexList.add(1);
      indexList.add(2);
      indexList.add(3);
      indexList.add(4);
      methodToIndices.put(" boolean containsValue(Object value) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(5);
      indexList.add(6);
      indexList.add(7);
      indexList.add(8);
      indexList.add(9);
      methodToIndices.put(" boolean containsKey(Object key) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(10);
      indexList.add(11);
      indexList.add(12);
      indexList.add(13);
      indexList.add(14);
      methodToIndices.put(" Object get(Object key) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(15);
      indexList.add(16);
      indexList.add(17);
      indexList.add(18);
      indexList.add(19);
      indexList.add(20);
      methodToIndices.put(" Object remove(Object key) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(21);
      methodToIndices.put(" void putAll(Map t) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(22);
      methodToIndices.put(" Set keySet() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(23);
      methodToIndices.put(" Collection values() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(24);
      indexList.add(25);
      indexList.add(26);
      indexList.add(27);
      indexList.add(28);
      indexList.add(29);
      indexList.add(30);
      indexList.add(0);
      methodToIndices.put(" boolean equals(Object o) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(31);
      methodToIndices.put(" int hashCode() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(32);
      indexList.add(33);
      methodToIndices.put(" String toString() ", indexList);
    }
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(34, methodToIndices);
  }
}
