package randoop.util;

import java.util.LinkedHashSet;
import java.util.Set;

public class SimpleSet<T> implements ISimpleSet<T> {

  private final LinkedHashSet<T> set;

  public SimpleSet() {
    set = new LinkedHashSet<>();
  }

  @Override
  public void add(T elt) {
    set.add(elt);
  }

  @Override
  public boolean contains(T elt) {
    return set.contains(elt);
  }

  @Override
  public Set<T> getElements() {
    return set;
  }

  @Override
  public void remove(T elt) {
    set.remove(elt);
  }

  @Override
  public int size() {
    return set.size();
  }
}
