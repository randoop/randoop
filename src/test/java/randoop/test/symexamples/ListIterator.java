package randoop.test.symexamples;

import java.util.Iterator;

public interface ListIterator extends Iterator {
  boolean hasNext();

  Object next();

  boolean hasPrevious();

  Object previous();

  int nextIndex();

  int previousIndex();

  void remove();

  void set(Object o);

  void add(Object o);
}
