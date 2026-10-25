package randoop.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.checkerframework.checker.mustcall.qual.MustCallUnknown;
import org.checkerframework.checker.signedness.qual.PolySigned;
import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * A Set that supports settingcheckpoints (also called "marks") and restoring the data structure's
 * state to them.
 *
 * @param <E> the type of elements
 */
public class CheckpointingSet<E extends @Signed Object> implements Set<E> {

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
    return map.add(elt, true);
  }

  @Override
  public boolean contains(@MustCallUnknown @UnknownSignedness Object elt) {
    if (elt == null) throw new IllegalArgumentException("arg cannot be null.");
    return map.containsKey(elt);
  }

  @Override
  public boolean remove(@MustCallUnknown @UnknownSignedness Object elt) {
    if (elt == null) {
      throw new IllegalArgumentException("arg cannot be null.");
    }

    @SuppressWarnings({
      "unchecked",
      "signedness:cast.unsafe" // unchecked cast
    })
    E eltCasted = (E) elt;
    return map.remove(eltCasted, true);
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
  @SideEffectFree
  public <T> T[] toArray(T[] a) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public @PolySigned Object[] toArray() {
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
