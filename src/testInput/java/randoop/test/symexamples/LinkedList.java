package randoop.test.symexamples;

import java.util.NoSuchElementException;

public class LinkedList extends AbstractSequentialList {
  public transient Entry header = new Entry(null, null, null);

  private transient int size = 0;

  public LinkedList() {
    header.next = header.previous = header;
  }

  public Integer removeLast() {
    Integer last = header.previous.element;
    remove(header.previous);
    return last;
  }

  public boolean add(Integer o) {
    addBefore(o, header);
    return true;
  }

  public boolean remove(Integer o) {
    for (Entry e = header.next;
        (((e != header) && ++randoopCoverageInfo.branchTrue[1] != 0)
            || ++randoopCoverageInfo.branchFalse[1] == 0);
        e = e.next) {
      if ((((o.equals(e.element)) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0)) {
        remove(e);
        return true;
      }
    }
    return false;
  }

  public class ListItr implements ListIterator {
    private Entry lastReturned = header;

    private Entry next;

    private int nextIndex;

    ListItr(int index) {
      if (index < 0 || index > size) {
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
      }
      if (index < size / 2) {
        next = header.next;
        for (nextIndex = 0; nextIndex < index; nextIndex++) next = next.next;
      } else {
        next = header;
        for (nextIndex = size; nextIndex > index; nextIndex--) next = next.previous;
      }
    }

    public boolean hasNext() {
      return nextIndex != size;
    }

    public Object next() {
      checkForComodification();
      if (nextIndex == size) throw new NoSuchElementException();
      lastReturned = next;
      next = next.next;
      nextIndex++;
      return lastReturned.element;
    }

    public boolean hasPrevious() {
      return nextIndex != 0;
    }

    public Object previous() {
      if (nextIndex == 0) throw new NoSuchElementException();
      lastReturned = next = next.previous;
      nextIndex--;
      checkForComodification();
      return lastReturned.element;
    }

    public int nextIndex() {
      return nextIndex;
    }

    public int previousIndex() {
      return nextIndex - 1;
    }

    public void remove() {
      LinkedList.this.remove(lastReturned);
      if (next == lastReturned) next = lastReturned.next;
      else nextIndex--;
      lastReturned = header;
    }

    public void set(Object o) {
      if (lastReturned == header) throw new IllegalStateException();
      checkForComodification();
      lastReturned.element = (Integer) o;
    }

    public void add(Object o) {
      checkForComodification();
      lastReturned = header;
      addBefore((Integer) o, next);
      nextIndex++;
    }

    final void checkForComodification() {}
  }

  public static class Entry {
    Integer element;

    Entry next;

    Entry previous;

    Entry(Integer element, Entry next, Entry previous) {
      this.element = element;
      this.next = next;
      this.previous = previous;
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        methodToIndices.put(" boolean remove(Integer o) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  private Entry addBefore(Integer o, Entry e) {
    Entry newEntry = new Entry(o, e, e.previous);
    newEntry.previous.next = newEntry;
    newEntry.next.previous = newEntry;
    size++;
    return newEntry;
  }

  private void remove(Entry e) {
    if ((((e == header) && ++randoopCoverageInfo.branchTrue[2] != 0)
        || ++randoopCoverageInfo.branchFalse[2] == 0)) throw new NoSuchElementException();
    e.previous.next = e.next;
    e.next.previous = e.previous;
    size--;
  }

  private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

  static {
    java.util.Map<String, java.util.Set<Integer>> methodToIndices = new java.util.LinkedHashMap<>();
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      indexList.add(1);
      methodToIndices.put(" boolean remove(Integer o) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(2);
      methodToIndices.put(" void remove(Entry e) ", indexList);
    }
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(3, methodToIndices);
  }
}
