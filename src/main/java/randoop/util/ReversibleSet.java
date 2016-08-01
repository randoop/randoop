package randoop.util;

import java.util.Set;

public class ReversibleSet<T> implements ISimpleSet<T> {

  public final ReversibleMultiMap<T, Boolean> map;

  public ReversibleSet() {
    this.map = new ReversibleMultiMap<>();
  }

  @Override
  public void add(T elt) {
    if (elt == null) throw new IllegalArgumentException("arg cannot be null.");
    if (contains(elt)) throw new IllegalArgumentException("set already contains elt " + elt);
    map.add(elt, true);
  }

  @Override
  public boolean contains(T elt) {
    if (elt == null) throw new IllegalArgumentException("arg cannot be null.");
    return map.keySet().contains(elt);
  }

  @Override
  public Set<T> getElements() {
    return map.keySet();
  }

  @Override
  public void remove(T elt) {
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

  public void mark() {
    map.mark();
  }

  public void undoToLastMark() {
    map.undoToLastMark();
  }

  @Override
  public String toString() {
    return map.keySet().toString();
  }
}
