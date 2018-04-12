package collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** A class that violates the collection.toArray().length == collection.size() contract. */
public class BadCollection<E> implements Collection<E> {
  private final List<E> backingList;

  public BadCollection() {
    backingList = new ArrayList<>();
  }

  @Override
  public void clear() {
    this.backingList.clear();
  }

  @Override
  public int size() {
    return this.backingList.size() + 1;
  }

  @Override
  public boolean isEmpty() {
    return this.backingList.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return this.backingList.contains(o);
  }

  @Override
  public Iterator iterator() {
    return this.backingList.iterator();
  }

  @Override
  public Object[] toArray() {
    return this.backingList.toArray();
  }

  @Override
  public Object[] toArray(Object[] ts) {
    return this.backingList.toArray(ts);
  }

  @Override
  public boolean add(Object e) {
    return this.backingList.add((E) e);
  }

  @Override
  public boolean remove(Object o) {
    return this.backingList.remove(o);
  }

  @Override
  public boolean containsAll(Collection clctn) {
    return this.backingList.containsAll(clctn);
  }

  @Override
  public boolean addAll(Collection clctn) {
    return this.backingList.addAll(clctn);
  }

  @Override
  public boolean removeAll(Collection clctn) {
    return this.backingList.removeAll(clctn);
  }

  @Override
  public boolean retainAll(Collection clctn) {
    return this.backingList.retainAll(clctn);
  }
}
