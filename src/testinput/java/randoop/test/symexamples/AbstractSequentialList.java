package randoop.test.symexamples;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractSequentialList extends AbstractList {
  protected AbstractSequentialList() {}

  @Override
  public Object get(int index) {
    ListIterator e = listIterator(index);
    try {
      return (e.next());
    } catch (NoSuchElementException exc) {
      throw (new IndexOutOfBoundsException("Index: " + index));
    }
  }

  @Override
  public Object set(int index, Object element) {
    ListIterator e = listIterator(index);
    try {
      Object oldVal = e.next();
      e.set(element);
      return oldVal;
    } catch (NoSuchElementException exc) {
      throw (new IndexOutOfBoundsException("Index: " + index));
    }
  }

  @Override
  public void add(int index, Object element) {
    ListIterator e = listIterator(index);
    e.add(element);
  }

  @Override
  public Object remove(int index) {
    ListIterator e = listIterator(index);
    Object outCast;
    try {
      outCast = e.next();
    } catch (NoSuchElementException exc) {
      throw (new IndexOutOfBoundsException("Index: " + index));
    }
    e.remove();
    return (outCast);
  }

  @Override
  public boolean addAll(int index, Collection c) {
    boolean modified = false;
    ListIterator e1 = listIterator(index);
    Iterator e2 = c.iterator();
    while ((((e2.hasNext()) && ++randoopCoverageInfo.branchTrue[0] != 0)
        || ++randoopCoverageInfo.branchFalse[0] == 0)) {
      e1.add(e2.next());
      modified = true;
    }
    return modified;
  }

  @Override
  public Iterator iterator() {
    return listIterator();
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public ListIterator listIterator(int index) {
    return null;
  }

  private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

  static {
    java.util.Map<String, java.util.Set<Integer>> methodToIndices = new java.util.LinkedHashMap<>();
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      methodToIndices.put(" boolean addAll(int index, Collection c) ", indexList);
    }
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(1, methodToIndices);
  }
}
