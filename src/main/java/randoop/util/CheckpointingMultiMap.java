package randoop.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;

/**
 * A MultiMap that supports checkpointing and restoring to a checkpoint (that is, undoing all
 * operations up to a checkpoint, also called a "mark").
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
// @Signed so the values can be printed
public class CheckpointingMultiMap<K extends @Signed Object, V extends @Signed Object>
    implements IMultiMap<K, V> {

  /** True if this class should do logging. */
  public static boolean verbose_log = false;

  /** The backing map. */
  private final Map<K, Set<V>> map;

  /** The marks/checkpoints that have been set so far, to permit restoring to a previous state. */
  public final List<Integer> marks;

  /** The operations on the map. */
  private enum Ops {
    /** Adding an element to the map. */
    ADD,
    /** Removing an element from the map. */
    REMOVE
  }

  /** The operations that have been performed on this map. */
  private final List<OpKeyVal> ops;

  /** The number of operations that have been performed on this map. */
  private int steps;

  /** A triple of an operation, a key, and a value. */
  private class OpKeyVal {
    /** An operation. */
    final Ops op;

    /** A key. */
    final K key;

    /** A value. */
    final V val;

    /**
     * Creates a new OpKeyVal.
     *
     * @param op an operation
     * @param key a key
     * @param val a value
     */
    OpKeyVal(final Ops op, final K key, final V val) {
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

  @Override
  public boolean add(K key, V value) {
    if (verbose_log) {
      Log.logPrintf("ADD %s -> %s%n", key, value);
    }
    add_bare(key, value);
    ops.add(new OpKeyVal(Ops.ADD, key, value));
    steps++;
    return true;
  }

  private void add_bare(K key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException("args cannot be null.");
    }

    Set<V> values = map.computeIfAbsent(key, __ -> new LinkedHashSet<>(1));
    if (values.contains(value)) {
      throw new IllegalArgumentException("Mapping already present: " + key + " -> " + value);
    }
    values.add(value);
  }

  @Override
  public boolean remove(K key, V value) {
    if (verbose_log) {
      Log.logPrintf("REMOVE %s -> %s%n", key, value);
    }
    remove_bare(key, value);
    ops.add(new OpKeyVal(Ops.REMOVE, key, value));
    steps++;
    return true;
  }

  private void remove_bare(K key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException("args cannot be null.");
    }

    Set<V> values = map.get(key);
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
    K key = last.key;
    V val = last.val;

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

  @Override
  public Set<V> getValues(K key) {
    if (key == null) throw new IllegalArgumentException("arg cannot be null.");
    return map.getOrDefault(key, Collections.emptySet());
  }

  /**
   * Returns true if this map contains the given key.
   *
   * @param key the key to look for
   * @return true if this map contains the given key
   */
  public boolean containsKey(@UnknownSignedness Object key) {
    if (key == null) throw new IllegalArgumentException("arg cannot be null.");
    return map.containsKey(key);
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public String toString() {
    return map.toString();
  }
}
