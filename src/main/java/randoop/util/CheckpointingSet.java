package randoop.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * A Set that supports settingcheckpoints (also called "marks") and restoring the data structure's
 * state to them.
 */
public class CheckpointingSet<E> implements Set<E> {

  // This uses a MultiMap just because that is an existing checkpointing data structure.
  // The value is always true in this mapping, never false.
  public final CheckpointingMultiMap<E, Boolean> map;

  public CheckpointingSet() {
    this.map = new CheckpointingMultiMap<>();
  }

  @Override
  public boolean add(E elt) {
    if (elt == null) throw new IllegalArgumentException("arg cannot be null.");
    if (contains(elt)) throw new IllegalArgumentException("set already contains elt " + elt);
    return map.add(elt, Boolean.TRUE);
  }

  @Override
  public boolean contains(Object elt) {
    if (elt == null) throw new IllegalArgumentException("arg cannot be null.");
    return map.containsKey(elt);
  }

  @Override
  public boolean remove(Object elt) {
    if (elt == null) {
      throw new IllegalArgumentException("arg cannot be null.");
    }

    @SuppressWarnings("unchecked")
    E eltCasted = (E) elt;
    return map.remove(eltCasted, Boolean.TRUE);
  }

  @Override
  public int size() {
    return map.size();
  }

  /** Checkpoint the state of the data structure, for use by {@link #undoToLastMark()}. */
  public void mark() {
    map.mark();
  }

  /** Undo changes since the last call to {@link #mark()}. */
  public void undoToLastMark() {
    map.undoToLastMark();
  }

  @Override
  public String toString() {
    return map.keySet().toString();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public <T> T[] toArray(T[] a) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public Object[] toArray() {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public Iterator<E> iterator() {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public boolean isEmpty() {
    // return map.isEmpty();
    throw new UnsupportedOperationException("not yet implemented");
  }
}
