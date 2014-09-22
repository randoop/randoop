package randoop.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import randoop.Globals;
import plume.UtilMDE;

/**
 * Keeps track of how many objects of different kinds are inserted into it.
 */
public final class Histogram<T> {
  public final Map<T, Integer> fMap= new LinkedHashMap<T, Integer>();
  private String fName;

  public Histogram(String name) {
    fName= name;
  }

  public Histogram() {
    this(null);
  }

  public void addToCount(T t, int i) {
    if (i <= 0)
      throw new IllegalArgumentException("negative argument:" + i);
    if (! fMap.containsKey(t))
      fMap.put(t, i);
    else
      fMap.put(t, getCount(t) + i);
  }

  public void addOneToCount(T t) {
    addToCount(t, 1);
  }

  @Override
  public String toString() {
    return toStringSortedByNumbers();
  }

  public String toStringSortedByNumbers() {
    return entriesToString(true, new Comparator<Map.Entry<T, Integer>>() {
      public int compare(Map.Entry<T, Integer> e1, Map.Entry<T, Integer> e2) {
        Integer value1= e1.getValue();
        Integer value2= e2.getValue();
        return value2.intValue() - value1.intValue();
      }
    });
  }

  /**
   * This works only when keys are comparable.
   * (In general, the keys are not required to be comparable.)
   */
  @SuppressWarnings("rawtypes")
  public String toStringSortedByKey() {
    return entriesToString(false, new Comparator<Map.Entry<T, Integer>>() {
      @SuppressWarnings("unchecked")
      public int compare(Map.Entry<T, Integer> e1, Map.Entry<T, Integer> e2) {
        Comparable key1= (Comparable) e1.getKey();
        Comparable key2= (Comparable) e2.getKey();
        return key2.compareTo(key1);
      }
    });
  }

  private String entriesToString(boolean append_percent, Comparator<Map.Entry<T, Integer>> c) {
    List<Map.Entry<T, Integer>> entries= new ArrayList<Map.Entry<T, Integer>>(fMap.entrySet());
    Collections.sort(entries, c);
    StringBuilder sb= new StringBuilder();
    if (fName != null)
      sb.append("Histogram:").append(fName).append(Globals.lineSep);
    int size = getSize();
    sb.append("Total size:" + size).append(Globals.lineSep);
    for(Iterator<Map.Entry<T, Integer>> iter= entries.iterator(); iter.hasNext();) {
      Map.Entry<T, Integer> e= iter.next();
      sb.append(UtilMDE.rpad(e.getValue(), 9));
      if (append_percent)
        sb.append(UtilMDE.rpad(createPercentString(size, e), 8));
      sb.append(e.getKey() +Globals.lineSep);
    }
    return sb.toString();
  }

  private String createPercentString(int size, Map.Entry<T, Integer> e) {
    float size_f = size;// this this assignment for conversion
    float count = getCount(e.getKey());// this assignment for conversion
    int percent = Math.round((count/size_f)*100);
    return " ["+ percent +"%]";
  }

  private int getSize() {
    int result= 0;
    for(Iterator<T> iter= fMap.keySet().iterator(); iter.hasNext();) {
      result += getCount(iter.next());
    }
    return result;
  }

  public int getCount(T t) {
    if (fMap.containsKey(t))
      return fMap.get(t);
    else return 0;

  }
  public void clear() {
    fMap.clear();
  }
}
