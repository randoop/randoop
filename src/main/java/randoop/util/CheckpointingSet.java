package randoop.util;

import java.util.AbstractSet;
import java.util.Iterator;
import org.checkerframework.checker.mustcall.qual.MustCallUnknown;
import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;

/**
 * A Set that supports setting checkpoints (also called "marks") and restoring the data structure's
 * state to them. The set does not support null elements.
 *
 * @param <E> the type of elements
 */
public class CheckpointingSet<E extends @Signed Object> extends AbstractSet<E> {

  // This uses a MultiMap just because that is an existing checkpointing data structure.
  // The value is always true in this mapping, never false.
  public final CheckpointingMultiMap<E, Boolean> map;

  public CheckpointingSet() {
    this.map = new CheckpointingMultiMap<>();
  }

  @Override
  public boolean add(E elt) {
    if (elt == null) {
      throw new IllegalArgumentException("arg cannot be null.");
    }
    if (contains(elt)) {
      throw new IllegalArgumentException("set already contains elt " + elt);
    }
    return map.add(elt, true);
  }

  @Override
  public boolean contains(@MustCallUnknown @UnknownSignedness Object elt) {
    if (elt == null) {
      throw new IllegalArgumentException("arg cannot be null.");
    }
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

  @Override
  public Iterator<E> iterator() {
    return new CheckpointingSetIterator(map.keySet().iterator());
  }

  /** An iterator over a {@link CheckpointingSet} whose {@code remove()} preserves checkpointing. */
  private class CheckpointingSetIterator implements Iterator<E> {
    /** The iterator over the underlying map's keys. */
    private final Iterator<E> underlying;

    /** The element most recently returned by {@link #next()}, or null if none. */
    private E current;

    /**
     * Creates a {@link CheckpointingSetIterator}.
     *
     * @param underlying the iterator over the underlying map's keys
     */
    CheckpointingSetIterator(Iterator<E> underlying) {
      this.underlying = underlying;
    }

    @Override
    public boolean hasNext() {
      return underlying.hasNext();
    }

    @Override
    public E next() {
      current = underlying.next();
      return current;
    }

    @Override
    public void remove() {
      // Delegate to CheckpointingSet.remove() to preserve checkpointing
      if (current == null) {
        throw new IllegalStateException();
      }
      CheckpointingSet.this.remove(current);
      current = null;
    }
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
}
