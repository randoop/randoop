package randoop.test.symexamples;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

public abstract class AbstractList extends AbstractCollection implements List {
  protected AbstractList() {}

  @Override
  public boolean add(Object o) {
    add(size(), o);
    return true;
  }

  public abstract Object get(int index);

  public Object set(int index, Object element) {
    throw new UnsupportedOperationException();
  }

  public void add(int index, Object element) {
    throw new UnsupportedOperationException();
  }

  public Object remove(int index) {
    throw new UnsupportedOperationException();
  }

  public int indexOf(Object o) {
    ListIterator e = listIterator();
    if ((((o == null) && ++randoopCoverageInfo.branchTrue[4] != 0)
        || ++randoopCoverageInfo.branchFalse[4] == 0)) {
      while ((((e.hasNext()) && ++randoopCoverageInfo.branchTrue[1] != 0)
          || ++randoopCoverageInfo.branchFalse[1] == 0))
        if ((((e.next() == null) && ++randoopCoverageInfo.branchTrue[0] != 0)
            || ++randoopCoverageInfo.branchFalse[0] == 0)) return e.previousIndex();
    } else {
      while ((((e.hasNext()) && ++randoopCoverageInfo.branchTrue[3] != 0)
          || ++randoopCoverageInfo.branchFalse[3] == 0))
        if ((((o.equals(e.next())) && ++randoopCoverageInfo.branchTrue[2] != 0)
            || ++randoopCoverageInfo.branchFalse[2] == 0)) return e.previousIndex();
    }
    return -1;
  }

  public int lastIndexOf(Object o) {
    ListIterator e = listIterator(size());
    if ((((o == null) && ++randoopCoverageInfo.branchTrue[9] != 0)
        || ++randoopCoverageInfo.branchFalse[9] == 0)) {
      while ((((e.hasPrevious()) && ++randoopCoverageInfo.branchTrue[6] != 0)
          || ++randoopCoverageInfo.branchFalse[6] == 0))
        if ((((e.previous() == null) && ++randoopCoverageInfo.branchTrue[5] != 0)
            || ++randoopCoverageInfo.branchFalse[5] == 0)) return e.nextIndex();
    } else {
      while ((((e.hasPrevious()) && ++randoopCoverageInfo.branchTrue[8] != 0)
          || ++randoopCoverageInfo.branchFalse[8] == 0))
        if ((((o.equals(e.previous())) && ++randoopCoverageInfo.branchTrue[7] != 0)
            || ++randoopCoverageInfo.branchFalse[7] == 0)) return e.nextIndex();
    }
    return -1;
  }

  @Override
  public void clear() {
    removeRange(0, size());
  }

  public boolean addAll(int index, Collection c) {
    boolean modified = false;
    Iterator e = c.iterator();
    while ((((e.hasNext()) && ++randoopCoverageInfo.branchTrue[10] != 0)
        || ++randoopCoverageInfo.branchFalse[10] == 0)) {
      add(index++, e.next());
      modified = true;
    }
    return modified;
  }

  @Override
  public Iterator iterator() {
    return new Itr();
  }

  public ListIterator listIterator() {
    return listIterator(0);
  }

  public ListIterator listIterator(final int index) {
    if ((((index < 0 || index > size()) && ++randoopCoverageInfo.branchTrue[11] != 0)
        || ++randoopCoverageInfo.branchFalse[11] == 0))
      throw new IndexOutOfBoundsException("Index: " + index);
    return new ListItr(index);
  }

  private class Itr implements Iterator {
    int cursor = 0;

    int lastRet = -1;

    public boolean hasNext() {
      return cursor != size();
    }

    public Object next() {
      checkForComodification();
      try {
        Object next = get(cursor);
        lastRet = cursor++;
        return next;
      } catch (IndexOutOfBoundsException e) {
        checkForComodification();
        throw new NoSuchElementException();
      }
    }

    public void remove() {
      if (lastRet == -1) throw new IllegalStateException();
      checkForComodification();
      try {
        AbstractList.this.remove(lastRet);
        if (lastRet < cursor) cursor--;
        lastRet = -1;
      } catch (IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException();
      }
    }

    final void checkForComodification() {}
  }

  private class ListItr extends Itr implements ListIterator {
    ListItr(int index) {
      cursor = index;
    }

    public boolean hasPrevious() {
      return cursor != 0;
    }

    public Object previous() {
      checkForComodification();
      try {
        int i = cursor - 1;
        Object previous = get(i);
        lastRet = cursor = i;
        return previous;
      } catch (IndexOutOfBoundsException e) {
        checkForComodification();
        throw new NoSuchElementException();
      }
    }

    public int nextIndex() {
      return cursor;
    }

    public int previousIndex() {
      return cursor - 1;
    }

    public void set(Object o) {
      if (lastRet == -1) throw new IllegalStateException();
      checkForComodification();
      try {
        AbstractList.this.set(lastRet, o);
      } catch (IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException();
      }
    }

    public void add(Object o) {
      checkForComodification();
      try {
        AbstractList.this.add(cursor++, o);
        lastRet = -1;
      } catch (IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException();
      }
    }
  }

  public List subList(int fromIndex, int toIndex) {
    return (this instanceof RandomAccess
        ? new RandomAccessSubList(this, fromIndex, toIndex)
        : new SubList(this, fromIndex, toIndex));
  }

  @Override
  public boolean equals(Object o) {
    if ((((o == this) && ++randoopCoverageInfo.branchTrue[12] != 0)
        || ++randoopCoverageInfo.branchFalse[12] == 0)) return true;
    if ((((!(o instanceof List)) && ++randoopCoverageInfo.branchTrue[13] != 0)
        || ++randoopCoverageInfo.branchFalse[13] == 0)) return false;
    ListIterator e1 = listIterator();
    ListIterator e2 = ((List) o).listIterator();
    while ((((e1.hasNext() && e2.hasNext()) && ++randoopCoverageInfo.branchTrue[15] != 0)
        || ++randoopCoverageInfo.branchFalse[15] == 0)) {
      Object o1 = e1.next();
      Object o2 = e2.next();
      if ((((!(o1 == null ? o2 == null : o1.equals(o2)))
              && ++randoopCoverageInfo.branchTrue[14] != 0)
          || ++randoopCoverageInfo.branchFalse[14] == 0)) return false;
    }
    return !(e1.hasNext() || e2.hasNext());
  }

  @Override
  public int hashCode() {
    int hashCode = 1;
    Iterator i = iterator();
    while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[16] != 0)
        || ++randoopCoverageInfo.branchFalse[16] == 0)) {
      Object obj = i.next();
      hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
    }
    return hashCode;
  }

  protected void removeRange(int fromIndex, int toIndex) {
    ListIterator it = listIterator(fromIndex);
    for (int i = 0, n = toIndex - fromIndex;
        (((i < n) && ++randoopCoverageInfo.branchTrue[17] != 0)
            || ++randoopCoverageInfo.branchFalse[17] == 0);
        i++) {
      it.next();
      it.remove();
    }
  }

  private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

  static {
    java.util.Map<String, java.util.Set<Integer>> methodToIndices = new java.util.LinkedHashMap<>();
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      indexList.add(1);
      indexList.add(2);
      indexList.add(3);
      indexList.add(4);
      methodToIndices.put(" int indexOf(Object o) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(5);
      indexList.add(6);
      indexList.add(7);
      indexList.add(8);
      indexList.add(9);
      methodToIndices.put(" int lastIndexOf(Object o) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(10);
      methodToIndices.put(" boolean addAll(int index, Collection c) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(11);
      methodToIndices.put(" ListIterator listIterator(final int index) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(12);
      indexList.add(13);
      indexList.add(14);
      indexList.add(15);
      methodToIndices.put(" boolean equals(Object o) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(16);
      methodToIndices.put(" int hashCode() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(17);
      methodToIndices.put(" void removeRange(int fromIndex, int toIndex) ", indexList);
    }
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(18, methodToIndices);
  }
}

class SubList extends AbstractList {
  private AbstractList l;

  private int offset;

  private int size;

  SubList(AbstractList list, int fromIndex, int toIndex) {
    if ((((fromIndex < 0) && ++randoopCoverageInfo.branchTrue[0] != 0)
        || ++randoopCoverageInfo.branchFalse[0] == 0))
      throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
    if ((((toIndex > list.size()) && ++randoopCoverageInfo.branchTrue[1] != 0)
        || ++randoopCoverageInfo.branchFalse[1] == 0))
      throw new IndexOutOfBoundsException("toIndex = " + toIndex);
    if ((((fromIndex > toIndex) && ++randoopCoverageInfo.branchTrue[2] != 0)
        || ++randoopCoverageInfo.branchFalse[2] == 0))
      throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
    l = list;
    offset = fromIndex;
    size = toIndex - fromIndex;
  }

  @Override
  public Object set(int index, Object element) {
    rangeCheck(index);
    checkForComodification();
    return l.set(index + offset, element);
  }

  @Override
  public Object get(int index) {
    rangeCheck(index);
    checkForComodification();
    return l.get(index + offset);
  }

  @Override
  public int size() {
    checkForComodification();
    return size;
  }

  @Override
  public void add(int index, Object element) {
    if ((((index < 0 || index > size) && ++randoopCoverageInfo.branchTrue[3] != 0)
        || ++randoopCoverageInfo.branchFalse[3] == 0)) throw new IndexOutOfBoundsException();
    checkForComodification();
    l.add(index + offset, element);
    size++;
  }

  @Override
  public Object remove(int index) {
    rangeCheck(index);
    checkForComodification();
    Object result = l.remove(index + offset);
    size--;
    return result;
  }

  @Override
  protected void removeRange(int fromIndex, int toIndex) {
    checkForComodification();
    l.removeRange(fromIndex + offset, toIndex + offset);
    size -= (toIndex - fromIndex);
  }

  @Override
  public boolean addAll(Collection c) {
    return addAll(size, c);
  }

  @Override
  public boolean addAll(int index, Collection c) {
    if ((((index < 0 || index > size) && ++randoopCoverageInfo.branchTrue[4] != 0)
        || ++randoopCoverageInfo.branchFalse[4] == 0))
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    int cSize = c.size();
    if ((((cSize == 0) && ++randoopCoverageInfo.branchTrue[5] != 0)
        || ++randoopCoverageInfo.branchFalse[5] == 0)) return false;
    checkForComodification();
    l.addAll(offset + index, c);
    size += cSize;
    return true;
  }

  @Override
  public Iterator iterator() {
    return listIterator();
  }

  @Override
  public ListIterator listIterator(final int index) {
    checkForComodification();
    if ((((index < 0 || index > size) && ++randoopCoverageInfo.branchTrue[6] != 0)
        || ++randoopCoverageInfo.branchFalse[6] == 0))
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    return new ListIterator() {
      private ListIterator i = l.listIterator(index + offset);

      public boolean hasNext() {
        return nextIndex() < size;
      }

      public Object next() {
        if (hasNext()) return i.next();
        else throw new NoSuchElementException();
      }

      public boolean hasPrevious() {
        return previousIndex() >= 0;
      }

      public Object previous() {
        if (hasPrevious()) return i.previous();
        else throw new NoSuchElementException();
      }

      public int nextIndex() {
        return i.nextIndex() - offset;
      }

      public int previousIndex() {
        return i.previousIndex() - offset;
      }

      public void remove() {
        i.remove();
        size--;
      }

      public void set(Object o) {
        i.set(o);
      }

      public void add(Object o) {
        i.add(o);
        size++;
      }
    };
  }

  @Override
  public List subList(int fromIndex, int toIndex) {
    return new SubList(this, fromIndex, toIndex);
  }

  private void rangeCheck(int index) {
    if ((((index < 0 || index >= size) && ++randoopCoverageInfo.branchTrue[7] != 0)
        || ++randoopCoverageInfo.branchFalse[7] == 0))
      throw new IndexOutOfBoundsException("Index: " + index + ",Size: " + size);
  }

  private void checkForComodification() {}

  private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

  static {
    java.util.Map<String, java.util.Set<Integer>> methodToIndices = new java.util.LinkedHashMap<>();
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      indexList.add(1);
      indexList.add(2);
      methodToIndices.put(" SubList (AbstractList list, int fromIndex, int toIndex) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(3);
      methodToIndices.put(" void add(int index, Object element) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(4);
      indexList.add(5);
      methodToIndices.put(" boolean addAll(int index, Collection c) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(6);
      methodToIndices.put(" ListIterator listIterator(final int index) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(7);
      methodToIndices.put(" void rangeCheck(int index) ", indexList);
    }
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(8, methodToIndices);
  }
}

class RandomAccessSubList extends SubList implements RandomAccess {
  RandomAccessSubList(AbstractList list, int fromIndex, int toIndex) {
    super(list, fromIndex, toIndex);
  }

  @Override
  public List subList(int fromIndex, int toIndex) {
    return new RandomAccessSubList(this, fromIndex, toIndex);
  }

  private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

  static {
    java.util.Map<String, java.util.Set<Integer>> methodToIndices = new java.util.LinkedHashMap<>();
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
  }
}
