package randoop.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A MultiMap that supports checkpointing and restoring to a checkpoint (that is, undoing all
 * operations up to a checkpoint, also called a "mark").
 */
public class CheckpointingMultiMap<T1, T2> implements IMultiMap<T1, T2> {

  public static boolean verbose_log = false;

  private final Map<T1, Set<T2>> map;

  public final List<Integer> marks;

  private enum Ops {
    ADD,
    REMOVE
  }

  private final List<OpKeyVal> ops;

  private int steps;

  // A triple of an operation, a key, and a value
  private class OpKeyVal {
    final Ops op;
    final T1 key;
    final T2 val;

    OpKeyVal(final Ops op, final T1 key, final T2 val) {
      this.op = op;
      this.key = key;
      this.val = val;
    }
  }

  public CheckpointingMultiMap() {
    map = new LinkedHashMap<>();
    marks = new ArrayList<>();
    ops = new ArrayList<>();
    steps = 0;
  }

  /*
   * (non-Javadoc)
   *
   * @see randoop.util.IMultiMap#add(T1, T2)
   */
  @Override
  public void add(T1 key, T2 value) {
    if (verbose_log) {
      Log.logPrintf("ADD %s -> %s%n", key, value);
    }
    add_bare(key, value);
    ops.add(new OpKeyVal(Ops.ADD, key, value));
    steps++;
  }

  private void add_bare(T1 key, T2 value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException("args cannot be null.");
    }

    Set<T2> values = map.get(key);
    if (values == null) {
      values = new LinkedHashSet<>(1);
      map.put(key, values);
    }
    if (values.contains(value)) {
      throw new IllegalArgumentException("Mapping already present: " + key + " -> " + value);
    }
    values.add(value);
  }

  /*
   * (non-Javadoc)
   *
   * @see randoop.util.IMultiMap#remove(T1, T2)
   */
  @Override
  public void remove(T1 key, T2 value) {
    if (verbose_log) {
      Log.logPrintf("REMOVE %s -> %s%n", key, value);
    }
    remove_bare(key, value);
    ops.add(new OpKeyVal(Ops.REMOVE, key, value));
    steps++;
  }

  private void remove_bare(T1 key, T2 value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException("args cannot be null.");
    }

    Set<T2> values = map.get(key);
    if (values == null) {
      throw new IllegalArgumentException("Mapping not present: " + key + " -> " + value);
    }
    values.remove(value);

    // If no more mapping from key, remove key from map.
    if (values.isEmpty()) {
      map.remove(key);
    }
  }

  /** Checkpoint the state of the data structure, for use by {@link #undoToLastMark()}. */
  public void mark() {
    marks.add(steps);
    steps = 0;
  }

  /** Undo changes since the last call to {@link #mark()}. */
  public void undoToLastMark() {
    if (marks.isEmpty()) {
      throw new IllegalArgumentException("No marks.");
    }
    Log.logPrintf("marks: %s%n", marks);
    for (int i = 0; i < steps; i++) {
      undoLastOp();
    }
    steps = marks.remove(marks.size() - 1);
  }

  private void undoLastOp() {
    if (ops.isEmpty()) throw new IllegalStateException("ops empty.");
    OpKeyVal last = ops.remove(ops.size() - 1);
    Ops op = last.op;
    T1 key = last.key;
    T2 val = last.val;

    if (op == Ops.ADD) {
      // Remove the mapping.
      Log.logPrintf("REMOVE %s%n", key + " ->" + val);
      remove_bare(key, val);
    } else if (op == Ops.REMOVE) {
      // Add the mapping.
      Log.logPrintf("ADD %s -> %s%n", key, val);
      add_bare(key, val);
    } else {
      // Really, we should never get here.
      throw new IllegalStateException("Unhandled op: " + op);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see randoop.util.IMultiMap#getValues(T1)
   */
  @Override
  public Set<T2> getValues(T1 key) {
    if (key == null) throw new IllegalArgumentException("arg cannot be null.");
    Set<T2> values = map.get(key);
    if (values == null) {
      return Collections.emptySet();
    }
    return values;
  }

  /*
   * (non-Javadoc)
   *
   * @see randoop.util.IMultiMap#keySet()
   */
  @Override
  public Set<T1> keySet() {
    return map.keySet();
  }

  /*
   * (non-Javadoc)
   *
   * @see randoop.util.IMultiMap#size()
   */
  @Override
  public int size() {
    return map.size();
  }

  /*
   * (non-Javadoc)
   *
   * @see randoop.util.IMultiMap#toString()
   */
  @Override
  public String toString() {
    return map.toString();
  }
}
