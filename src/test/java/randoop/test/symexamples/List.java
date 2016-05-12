package randoop.test.symexamples;

import java.util.Collection;
import java.util.Iterator;

public interface List extends Collection {
  int size();

  boolean isEmpty();

  boolean contains(Object o);

  Iterator iterator();

  Object[] toArray();

  Object[] toArray(Object[] a);

  boolean add(Object o);

  boolean remove(Object o);

  boolean containsAll(Collection c);

  boolean addAll(Collection c);

  boolean addAll(int index, Collection c);

  boolean removeAll(Collection c);

  boolean retainAll(Collection c);

  void clear();

  boolean equals(Object o);

  int hashCode();

  Object get(int index);

  Object set(int index, Object element);

  void add(int index, Object element);

  Object remove(int index);

  int indexOf(Object o);

  int lastIndexOf(Object o);

  ListIterator listIterator();

  ListIterator listIterator(int index);

  List subList(int fromIndex, int toIndex);
}
