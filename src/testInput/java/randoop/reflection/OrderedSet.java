package randoop.reflection;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * Input class for testing type instantiation. SortedSet classes have the property that the order on
 * the element type may be defined as the "natural order" or by a {@code Comparator} object.
 */
public class OrderedSet<E> implements SortedSet<E> {

  public OrderedSet() {} // natural order

  public OrderedSet(Comparator<E> comparator) {} // explicit comparator

  public OrderedSet(Collection<E> collection) {} // natural order

  public OrderedSet(SortedSet<E> set) {} // same order as set

  @Override
  public Comparator<? super E> comparator() {
    return null;
  }

  @Override
  public SortedSet<E> subSet(E fromElement, E toElement) {
    return new OrderedSet<>();
  }

  @Override
  public SortedSet<E> headSet(E toElement) {
    return new OrderedSet<>();
  }

  @Override
  public SortedSet<E> tailSet(E fromElement) {
    return new OrderedSet<>();
  }

  @Override
  public E first() {
    return null;
  }

  @Override
  public E last() {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean contains(Object o) {
    return false;
  }

  @Override
  public Iterator<E> iterator() {
    return new OrderedIterator<>();
  }

  @Override
  public Object[] toArray() {
    return new Object[0];
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return (T[]) new Object[0];
  }

  @Override
  public boolean add(E e) {
    return false;
  }

  @Override
  public boolean remove(Object o) {
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    return false;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return false;
  }

  @Override
  public void clear() {}

  private class OrderedIterator<T> implements Iterator<T> {

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public T next() {
      return null;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("not implemented");
    }
  }
}
