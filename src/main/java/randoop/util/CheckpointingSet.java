package randoop.util;

import java.util.Set;

/**
 * A Set that supports settingcheckpoints (also called "marks") and restoring the data structure's
 * state to them.
 */
public class CheckpointingSet<E> implements ISimpleSet<E> {

  public final CheckpointingMultiMap<E, Boolean> map;

  public CheckpointingSet() {
    this.map = new CheckpointingMultiMap<>();
  }

  @Override
  public void add(E elt) {
    if (elt == null) throw new IllegalArgumentException("arg cannot be null.");
    if (contains(elt)) throw new IllegalArgumentException("set already contains elt " + elt);
    map.add(elt, true);
  }

  @Override
  public boolean contains(E elt) {
    if (elt == null) throw new IllegalArgumentException("arg cannot be null.");
    return map.keySet().contains(elt);
  }

  @Override
  public Set<E> getElements() {
    return map.keySet();
  }

  @Override
  public void remove(E elt) {
    if (elt == null) {
      throw new IllegalArgumentException("arg cannot be null.");
    }

    if (!contains(elt)) {
      throw new IllegalArgumentException("set does not contain elt " + elt);
    }

    map.remove(elt, true);
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
}
