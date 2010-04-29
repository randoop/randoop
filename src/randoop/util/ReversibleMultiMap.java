package randoop.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plume.Triple;

public class ReversibleMultiMap<T1, T2> implements IMultiMap<T1, T2> {
  
  public static boolean verbose_log = false;
  
  private final Map<T1, Set<T2>> map;
  
  public final List<Integer> marks;
  
  private enum Ops { ADD, REMOVE };
  
  private final List<Triple<Ops,T1,T2>> ops;

  private int steps;

  public ReversibleMultiMap() {
    map = new LinkedHashMap<T1, Set<T2>>();
    marks = new ArrayList<Integer>();
    ops = new ArrayList<Triple<Ops,T1,T2>>();
    steps = 0;
  }

  /* (non-Javadoc)
   * @see randoop.util.IMultiMap#add(T1, T2)
   */
  public void add(T1 key, T2 value) {
    if (verbose_log && Log.isLoggingOn()) Log.logLine("ADD " + key + " ->" + value);
    add_bare(key, value);
    ops.add(new Triple<Ops, T1, T2>(Ops.ADD, key, value));
    steps++;
  }

  private void add_bare(T1 key, T2 value) {
    if (key == null || value == null)
      throw new IllegalArgumentException("args cannot be null.");
    
    Set<T2> values = map.get(key);
    if(values == null) {
      values = new LinkedHashSet<T2>(1);  
      map.put(key, values);
    }
    if (values.contains(value)) {
      throw new IllegalArgumentException("Mapping already present: " + key + " -> " + value);
    }
    values.add(value);
  }

  /* (non-Javadoc)
   * @see randoop.util.IMultiMap#remove(T1, T2)
   */
  public void remove(T1 key, T2 value) {
    if (verbose_log && Log.isLoggingOn()) Log.logLine("REMOVE " + key + " ->" + value);
    remove_bare(key, value);
    ops.add(new Triple<Ops, T1, T2>(Ops.REMOVE, key, value));
    steps++;
  }
  
  private void remove_bare(T1 key, T2 value) {
    if (key == null || value == null)
      throw new IllegalArgumentException("args cannot be null.");

    Set<T2> values = map.get(key);
    if(values == null) {
      throw new IllegalArgumentException("Mapping not present: " + key + " -> " + value);
    } 
    values.remove(value);

    // If no more mapping from key, remove key from map.
    if (values.isEmpty()) {
      map.remove(key);
    }
  }
  
  public void mark() {
    marks.add(steps);
    steps = 0;
  }
  
  public void undoToLastMark() {
    if (marks.isEmpty()) {
      throw new IllegalArgumentException("No marks.");
    }
    if (Log.isLoggingOn()) Log.logLine("marks: " + marks);
    for (int i = 0 ; i < steps ; i++) {
      undoLastOp();
    }
    steps = marks.remove(marks.size() - 1);
  }
  
  private void undoLastOp() {
    if (ops.isEmpty())
      throw new IllegalStateException("ops empty.");
    Triple<Ops,T1,T2> last = ops.remove(ops.size() - 1);
    
    if (last.a == Ops.ADD) {
      // Remove the mapping.
      if (Log.isLoggingOn()) Log.logLine("REMOVE " + last.b + " ->" + last.c);
      remove_bare(last.b, last.c);
    } else if (last.a == Ops.REMOVE) {
      // Add the mapping.
      if (Log.isLoggingOn()) Log.logLine("ADD " + last.b + " ->" + last.c);
      add_bare(last.b, last.c);
    } else {
      // Really, we should never get here.
      throw new IllegalStateException("Unhandled op: " + last.a);
    }
  }

  /* (non-Javadoc)
   * @see randoop.util.IMultiMap#getValues(T1)
   */
  public Set<T2> getValues(T1 key) {
    if (key == null)
      throw new IllegalArgumentException("arg cannot be null.");
    Set<T2> values = map.get(key);
    if(values == null) return Collections.emptySet();
    return values;
  }

  /* (non-Javadoc)
   * @see randoop.util.IMultiMap#keySet()
   */
  public Set<T1> keySet() {
    return map.keySet();
  }

  /* (non-Javadoc)
   * @see randoop.util.IMultiMap#size()
   */
  public int size() {
    return map.size();
  }

  /* (non-Javadoc)
   * @see randoop.util.IMultiMap#toString()
   */
  @Override
  public String toString() {
    return map.toString();
  }

}
