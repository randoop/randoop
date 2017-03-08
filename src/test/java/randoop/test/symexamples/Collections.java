package randoop.test.symexamples;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public class Collections {
  private Collections() {}

  private static final int BINARYSEARCH_THRESHOLD = 5000;

  private static final int REVERSE_THRESHOLD = 18;

  private static final int SHUFFLE_THRESHOLD = 5;

  private static final int FILL_THRESHOLD = 25;

  private static final int ROTATE_THRESHOLD = 100;

  private static final int COPY_THRESHOLD = 10;

  private static final int REPLACEALL_THRESHOLD = 11;

  private static final int INDEXOFSUBLIST_THRESHOLD = 35;

  private static Object get(ListIterator i, int index) {
    Object obj;
    int pos = i.nextIndex();
    if ((((pos <= index) && ++randoopCoverageInfo.branchTrue[2] != 0)
        || ++randoopCoverageInfo.branchFalse[2] == 0)) {
      do {
        obj = i.next();
      } while ((((pos++ < index) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0));
    } else {
      do {
        obj = i.previous();
      } while ((((--pos > index) && ++randoopCoverageInfo.branchTrue[1] != 0)
          || ++randoopCoverageInfo.branchFalse[1] == 0));
    }
    return obj;
  }

  public static void swap(List list, int i, int j) {
    list.set(i, list.set(j, list.get(i)));
  }

  private static void swap(Object[] arr, int i, int j) {
    Object tmp = arr[i];
    arr[i] = arr[j];
    arr[j] = tmp;
  }

  public static Object min(Collection coll) {
    Iterator i = coll.iterator();
    Comparable candidate = (Comparable) (i.next());
    while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[4] != 0)
        || ++randoopCoverageInfo.branchFalse[4] == 0)) {
      Comparable next = (Comparable) (i.next());
      if ((((next.compareTo(candidate) < 0) && ++randoopCoverageInfo.branchTrue[3] != 0)
          || ++randoopCoverageInfo.branchFalse[3] == 0)) candidate = next;
    }
    return candidate;
  }

  public static Object min(Collection coll, Comparator comp) {
    if ((((comp == null) && ++randoopCoverageInfo.branchTrue[5] != 0)
        || ++randoopCoverageInfo.branchFalse[5] == 0)) return min(coll);
    Iterator i = coll.iterator();
    Object candidate = i.next();
    while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[7] != 0)
        || ++randoopCoverageInfo.branchFalse[7] == 0)) {
      Object next = i.next();
      if ((((comp.compare(next, candidate) < 0) && ++randoopCoverageInfo.branchTrue[6] != 0)
          || ++randoopCoverageInfo.branchFalse[6] == 0)) candidate = next;
    }
    return candidate;
  }

  public static Object max(Collection coll) {
    Iterator i = coll.iterator();
    Comparable candidate = (Comparable) (i.next());
    while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[9] != 0)
        || ++randoopCoverageInfo.branchFalse[9] == 0)) {
      Comparable next = (Comparable) (i.next());
      if ((((next.compareTo(candidate) > 0) && ++randoopCoverageInfo.branchTrue[8] != 0)
          || ++randoopCoverageInfo.branchFalse[8] == 0)) candidate = next;
    }
    return candidate;
  }

  public static Object max(Collection coll, Comparator comp) {
    if ((((comp == null) && ++randoopCoverageInfo.branchTrue[10] != 0)
        || ++randoopCoverageInfo.branchFalse[10] == 0)) return max(coll);
    Iterator i = coll.iterator();
    Object candidate = i.next();
    while ((((i.hasNext()) && ++randoopCoverageInfo.branchTrue[12] != 0)
        || ++randoopCoverageInfo.branchFalse[12] == 0)) {
      Object next = i.next();
      if ((((comp.compare(next, candidate) > 0) && ++randoopCoverageInfo.branchTrue[11] != 0)
          || ++randoopCoverageInfo.branchFalse[11] == 0)) candidate = next;
    }
    return candidate;
  }

  public static int indexOfSubList(List source, List target) {
    int sourceSize = source.size();
    int targetSize = target.size();
    int maxCandidate = sourceSize - targetSize;
    if ((((sourceSize < INDEXOFSUBLIST_THRESHOLD
                || (source instanceof RandomAccess && target instanceof RandomAccess))
            && ++randoopCoverageInfo.branchTrue[20] != 0)
        || ++randoopCoverageInfo.branchFalse[20] == 0)) {
      nextCand:
      for (int candidate = 0;
          (((candidate <= maxCandidate) && ++randoopCoverageInfo.branchTrue[15] != 0)
              || ++randoopCoverageInfo.branchFalse[15] == 0);
          candidate++) {
        for (int i = 0, j = candidate;
            (((i < targetSize) && ++randoopCoverageInfo.branchTrue[14] != 0)
                || ++randoopCoverageInfo.branchFalse[14] == 0);
            i++, j++)
          if ((((!eq(target.get(i), source.get(j))) && ++randoopCoverageInfo.branchTrue[13] != 0)
              || ++randoopCoverageInfo.branchFalse[13] == 0)) continue nextCand;
        return candidate;
      }
    } else {
      ListIterator si = source.listIterator();
      nextCand:
      for (int candidate = 0;
          (((candidate <= maxCandidate) && ++randoopCoverageInfo.branchTrue[19] != 0)
              || ++randoopCoverageInfo.branchFalse[19] == 0);
          candidate++) {
        ListIterator ti = target.listIterator();
        for (int i = 0;
            (((i < targetSize) && ++randoopCoverageInfo.branchTrue[18] != 0)
                || ++randoopCoverageInfo.branchFalse[18] == 0);
            i++) {
          if ((((!eq(ti.next(), si.next())) && ++randoopCoverageInfo.branchTrue[17] != 0)
              || ++randoopCoverageInfo.branchFalse[17] == 0)) {
            for (int j = 0;
                (((j < i) && ++randoopCoverageInfo.branchTrue[16] != 0)
                    || ++randoopCoverageInfo.branchFalse[16] == 0);
                j++) si.previous();
            continue nextCand;
          }
        }
        return candidate;
      }
    }
    return -1;
  }

  public static int lastIndexOfSubList(List source, List target) {
    int sourceSize = source.size();
    int targetSize = target.size();
    int maxCandidate = sourceSize - targetSize;
    if ((((sourceSize < INDEXOFSUBLIST_THRESHOLD || source instanceof RandomAccess)
            && ++randoopCoverageInfo.branchTrue[30] != 0)
        || ++randoopCoverageInfo.branchFalse[30] == 0)) {
      nextCand:
      for (int candidate = maxCandidate;
          (((candidate >= 0) && ++randoopCoverageInfo.branchTrue[23] != 0)
              || ++randoopCoverageInfo.branchFalse[23] == 0);
          candidate--) {
        for (int i = 0, j = candidate;
            (((i < targetSize) && ++randoopCoverageInfo.branchTrue[22] != 0)
                || ++randoopCoverageInfo.branchFalse[22] == 0);
            i++, j++)
          if ((((!eq(target.get(i), source.get(j))) && ++randoopCoverageInfo.branchTrue[21] != 0)
              || ++randoopCoverageInfo.branchFalse[21] == 0)) continue nextCand;
        return candidate;
      }
    } else {
      if ((((maxCandidate < 0) && ++randoopCoverageInfo.branchTrue[24] != 0)
          || ++randoopCoverageInfo.branchFalse[24] == 0)) return -1;
      ListIterator si = source.listIterator(maxCandidate);
      nextCand:
      for (int candidate = maxCandidate;
          (((candidate >= 0) && ++randoopCoverageInfo.branchTrue[29] != 0)
              || ++randoopCoverageInfo.branchFalse[29] == 0);
          candidate--) {
        ListIterator ti = target.listIterator();
        for (int i = 0;
            (((i < targetSize) && ++randoopCoverageInfo.branchTrue[28] != 0)
                || ++randoopCoverageInfo.branchFalse[28] == 0);
            i++) {
          if ((((!eq(ti.next(), si.next())) && ++randoopCoverageInfo.branchTrue[27] != 0)
              || ++randoopCoverageInfo.branchFalse[27] == 0)) {
            if ((((candidate != 0) && ++randoopCoverageInfo.branchTrue[26] != 0)
                || ++randoopCoverageInfo.branchFalse[26] == 0)) {
              for (int j = 0;
                  (((j <= i + 1) && ++randoopCoverageInfo.branchTrue[25] != 0)
                      || ++randoopCoverageInfo.branchFalse[25] == 0);
                  j++) si.previous();
            }
            continue nextCand;
          }
        }
        return candidate;
      }
    }
    return -1;
  }

  public static Collection unmodifiableCollection(Collection c) {
    return new UnmodifiableCollection(c);
  }

  static class UnmodifiableCollection implements Collection, Serializable {
    private static final long serialVersionUID = 1820017752578914078L;

    Collection c;

    UnmodifiableCollection(Collection c) {
      if ((((c == null) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0)) throw new NullPointerException();
      this.c = c;
    }

    public int size() {
      return c.size();
    }

    public boolean isEmpty() {
      return c.isEmpty();
    }

    public boolean contains(Object o) {
      return c.contains(o);
    }

    public Object[] toArray() {
      return c.toArray();
    }

    public Object[] toArray(Object[] a) {
      return c.toArray(a);
    }

    @Override
    public String toString() {
      return c.toString();
    }

    public Iterator iterator() {
      return new Iterator() {
        Iterator i = c.iterator();

        public boolean hasNext() {
          return i.hasNext();
        }

        public Object next() {
          return i.next();
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    public boolean add(Object o) {
      throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection coll) {
      return c.containsAll(coll);
    }

    public boolean addAll(Collection coll) {
      throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection coll) {
      throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection coll) {
      throw new UnsupportedOperationException();
    }

    public void clear() {
      throw new UnsupportedOperationException();
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(1, methodToIndices);
    }
  }

  public static Set unmodifiableSet(Set s) {
    return new UnmodifiableSet(s);
  }

  static class UnmodifiableSet extends UnmodifiableCollection implements Set, Serializable {
    private static final long serialVersionUID = 1L;

    UnmodifiableSet(Set s) {
      super(s);
    }

    @Override
    public boolean equals(Object o) {
      return c.equals(o);
    }

    @Override
    public int hashCode() {
      return c.hashCode();
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public static SortedSet unmodifiableSortedSet(SortedSet s) {
    return new UnmodifiableSortedSet(s);
  }

  static class UnmodifiableSortedSet extends UnmodifiableSet implements SortedSet, Serializable {
    private static final long serialVersionUID = 1L;

    private SortedSet ss;

    UnmodifiableSortedSet(SortedSet s) {
      super(s);
      ss = s;
    }

    public Comparator comparator() {
      return ss.comparator();
    }

    public SortedSet subSet(Object fromElement, Object toElement) {
      return new UnmodifiableSortedSet(ss.subSet(fromElement, toElement));
    }

    public SortedSet headSet(Object toElement) {
      return new UnmodifiableSortedSet(ss.headSet(toElement));
    }

    public SortedSet tailSet(Object fromElement) {
      return new UnmodifiableSortedSet(ss.tailSet(fromElement));
    }

    public Object first() {
      return ss.first();
    }

    public Object last() {
      return ss.last();
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public static List unmodifiableList(List list) {
    return (list instanceof RandomAccess
        ? new UnmodifiableRandomAccessList(list)
        : new UnmodifiableList(list));
  }

  static class UnmodifiableList extends UnmodifiableCollection implements List {
    static final long serialVersionUID = -283967356065247728L;

    List list;

    UnmodifiableList(List list) {
      super(list);
      this.list = list;
    }

    @Override
    public boolean equals(Object o) {
      return list.equals(o);
    }

    @Override
    public int hashCode() {
      return list.hashCode();
    }

    public Object get(int index) {
      return list.get(index);
    }

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
      return list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
      return list.lastIndexOf(o);
    }

    public boolean addAll(int index, Collection c) {
      throw new UnsupportedOperationException();
    }

    public ListIterator listIterator() {
      return listIterator(0);
    }

    public ListIterator listIterator(final int index) {
      return new ListIterator() {
        ListIterator i = list.listIterator(index);

        public boolean hasNext() {
          return i.hasNext();
        }

        public Object next() {
          return i.next();
        }

        public boolean hasPrevious() {
          return i.hasPrevious();
        }

        public Object previous() {
          return i.previous();
        }

        public int nextIndex() {
          return i.nextIndex();
        }

        public int previousIndex() {
          return i.previousIndex();
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }

        public void set(Object o) {
          throw new UnsupportedOperationException();
        }

        public void add(Object o) {
          throw new UnsupportedOperationException();
        }
      };
    }

    public List subList(int fromIndex, int toIndex) {
      return new UnmodifiableList(list.subList(fromIndex, toIndex));
    }

    private Object readResolve() {
      return (list instanceof RandomAccess ? new UnmodifiableRandomAccessList(list) : this);
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  static class UnmodifiableRandomAccessList extends UnmodifiableList implements RandomAccess {
    UnmodifiableRandomAccessList(List list) {
      super(list);
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
      return new UnmodifiableRandomAccessList(list.subList(fromIndex, toIndex));
    }

    private static final long serialVersionUID = -2542308836966382001L;

    private Object writeReplace() {
      return new UnmodifiableList(list);
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public static Map unmodifiableMap(Map m) {
    return new UnmodifiableMap(m);
  }

  private static class UnmodifiableMap implements Map, Serializable {
    private static final long serialVersionUID = -1034234728574286014L;

    private final Map m;

    UnmodifiableMap(Map m) {
      if ((((m == null) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0)) throw new NullPointerException();
      this.m = m;
    }

    public int size() {
      return m.size();
    }

    public boolean isEmpty() {
      return m.isEmpty();
    }

    public boolean containsKey(Object key) {
      return m.containsKey(key);
    }

    public boolean containsValue(Object val) {
      return m.containsValue(val);
    }

    public Object get(Object key) {
      return m.get(key);
    }

    public Object put(Object key, Object value) {
      throw new UnsupportedOperationException();
    }

    public Object remove(Object key) {
      throw new UnsupportedOperationException();
    }

    public void putAll(Map t) {
      throw new UnsupportedOperationException();
    }

    public void clear() {
      throw new UnsupportedOperationException();
    }

    private transient Set keySet = null;

    private transient Set entrySet = null;

    private transient Collection values = null;

    public Set keySet() {
      if ((((keySet == null) && ++randoopCoverageInfo.branchTrue[1] != 0)
          || ++randoopCoverageInfo.branchFalse[1] == 0)) keySet = unmodifiableSet(m.keySet());
      return keySet;
    }

    public Set entrySet() {
      if ((((entrySet == null) && ++randoopCoverageInfo.branchTrue[2] != 0)
          || ++randoopCoverageInfo.branchFalse[2] == 0))
        entrySet = new UnmodifiableEntrySet(m.entrySet());
      return entrySet;
    }

    public Collection values() {
      if ((((values == null) && ++randoopCoverageInfo.branchTrue[3] != 0)
          || ++randoopCoverageInfo.branchFalse[3] == 0))
        values = unmodifiableCollection(m.values());
      return values;
    }

    @Override
    public boolean equals(Object o) {
      return m.equals(o);
    }

    @Override
    public int hashCode() {
      return m.hashCode();
    }

    @Override
    public String toString() {
      return m.toString();
    }

    static class UnmodifiableEntrySet extends UnmodifiableSet {
      private static final long serialVersionUID = 1L;

      UnmodifiableEntrySet(Set s) {
        super(s);
      }

      @Override
      public Iterator iterator() {
        return new Iterator() {
          Iterator i = c.iterator();

          public boolean hasNext() {
            return i.hasNext();
          }

          public Object next() {
            return new UnmodifiableEntry((Map.Entry) i.next());
          }

          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }

      @Override
      public Object[] toArray() {
        Object[] a = c.toArray();
        for (int i = 0;
            (((i < a.length) && ++randoopCoverageInfo.branchTrue[0] != 0)
                || ++randoopCoverageInfo.branchFalse[0] == 0);
            i++) a[i] = new UnmodifiableEntry((Map.Entry) a[i]);
        return a;
      }

      @Override
      public Object[] toArray(final Object[] a) {
        final Object[] arr =
            c.toArray(
                a.length == 0
                    ? a
                    : (Object[])
                        java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), 0));
        for (int i = 0;
            (((i < arr.length) && ++randoopCoverageInfo.branchTrue[1] != 0)
                || ++randoopCoverageInfo.branchFalse[1] == 0);
            i++) arr[i] = new UnmodifiableEntry((Map.Entry) arr[i]);
        if ((((arr.length > a.length) && ++randoopCoverageInfo.branchTrue[2] != 0)
            || ++randoopCoverageInfo.branchFalse[2] == 0)) return arr;
        System.arraycopy(arr, 0, a, 0, arr.length);
        if ((((a.length > arr.length) && ++randoopCoverageInfo.branchTrue[3] != 0)
            || ++randoopCoverageInfo.branchFalse[3] == 0)) a[arr.length] = null;
        return a;
      }

      @Override
      public boolean contains(Object o) {
        if ((((!(o instanceof Map.Entry)) && ++randoopCoverageInfo.branchTrue[4] != 0)
            || ++randoopCoverageInfo.branchFalse[4] == 0)) return false;
        return c.contains(new UnmodifiableEntry((Map.Entry) o));
      }

      @Override
      public boolean containsAll(Collection coll) {
        Iterator e = coll.iterator();
        while ((((e.hasNext()) && ++randoopCoverageInfo.branchTrue[6] != 0)
            || ++randoopCoverageInfo.branchFalse[6] == 0))
          if ((((!contains(e.next())) && ++randoopCoverageInfo.branchTrue[5] != 0)
              || ++randoopCoverageInfo.branchFalse[5] == 0)) return false;
        return true;
      }

      @Override
      public boolean equals(Object o) {
        if ((((o == this) && ++randoopCoverageInfo.branchTrue[7] != 0)
            || ++randoopCoverageInfo.branchFalse[7] == 0)) return true;
        if ((((!(o instanceof Set)) && ++randoopCoverageInfo.branchTrue[8] != 0)
            || ++randoopCoverageInfo.branchFalse[8] == 0)) return false;
        Set s = (Set) o;
        if ((((s.size() != c.size()) && ++randoopCoverageInfo.branchTrue[9] != 0)
            || ++randoopCoverageInfo.branchFalse[9] == 0)) return false;
        return containsAll(s);
      }

      private static class UnmodifiableEntry implements Map.Entry {
        private Map.Entry e;

        UnmodifiableEntry(Map.Entry e) {
          this.e = e;
        }

        public Object getKey() {
          return e.getKey();
        }

        public Object getValue() {
          return e.getValue();
        }

        public Object setValue(Object value) {
          throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
          return e.hashCode();
        }

        @Override
        public boolean equals(Object o) {
          if ((((!(o instanceof Map.Entry)) && ++randoopCoverageInfo.branchTrue[0] != 0)
              || ++randoopCoverageInfo.branchFalse[0] == 0)) return false;
          Map.Entry t = (Map.Entry) o;
          return eq(e.getKey(), t.getKey()) && eq(e.getValue(), t.getValue());
        }

        @Override
        public String toString() {
          return e.toString();
        }

        private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

        static {
          java.util.Map<String, java.util.Set<Integer>> methodToIndices =
              new java.util.LinkedHashMap<>();
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(0);
            indexList.add(1);
            indexList.add(2);
            methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(3);
            indexList.add(4);
            methodToIndices.put(" Object min(Collection coll) ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(5);
            indexList.add(6);
            indexList.add(7);
            methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(8);
            indexList.add(9);
            methodToIndices.put(" Object max(Collection coll) ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(10);
            indexList.add(11);
            indexList.add(12);
            methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(13);
            indexList.add(14);
            indexList.add(15);
            indexList.add(16);
            indexList.add(17);
            indexList.add(18);
            indexList.add(19);
            indexList.add(20);
            methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(21);
            indexList.add(22);
            indexList.add(23);
            indexList.add(24);
            indexList.add(25);
            indexList.add(26);
            indexList.add(27);
            indexList.add(28);
            indexList.add(29);
            indexList.add(30);
            methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(0);
            methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(0);
            methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(1);
            methodToIndices.put(" Set keySet() ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(2);
            methodToIndices.put(" Set entrySet() ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(3);
            methodToIndices.put(" Collection values() ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(0);
            methodToIndices.put(" Object[] toArray() ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(1);
            indexList.add(2);
            indexList.add(3);
            methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(4);
            methodToIndices.put(" boolean contains(Object o) ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(5);
            indexList.add(6);
            methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
          }
          {
            java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
            indexList.add(7);
            indexList.add(8);
            indexList.add(9);
            indexList.add(0);
            methodToIndices.put(" boolean equals(Object o) ", indexList);
          }
          randoopCoverageInfo = new randoop.util.TestCoverageInfo(1, methodToIndices);
        }
      }

      private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

      static {
        java.util.Map<String, java.util.Set<Integer>> methodToIndices =
            new java.util.LinkedHashMap<>();
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(0);
          indexList.add(1);
          indexList.add(2);
          methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(3);
          indexList.add(4);
          methodToIndices.put(" Object min(Collection coll) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(5);
          indexList.add(6);
          indexList.add(7);
          methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(8);
          indexList.add(9);
          methodToIndices.put(" Object max(Collection coll) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(10);
          indexList.add(11);
          indexList.add(12);
          methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(13);
          indexList.add(14);
          indexList.add(15);
          indexList.add(16);
          indexList.add(17);
          indexList.add(18);
          indexList.add(19);
          indexList.add(20);
          methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(21);
          indexList.add(22);
          indexList.add(23);
          indexList.add(24);
          indexList.add(25);
          indexList.add(26);
          indexList.add(27);
          indexList.add(28);
          indexList.add(29);
          indexList.add(30);
          methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(0);
          methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(0);
          methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(1);
          methodToIndices.put(" Set keySet() ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(2);
          methodToIndices.put(" Set entrySet() ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(3);
          methodToIndices.put(" Collection values() ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(0);
          methodToIndices.put(" Object[] toArray() ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(1);
          indexList.add(2);
          indexList.add(3);
          methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(4);
          methodToIndices.put(" boolean contains(Object o) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(5);
          indexList.add(6);
          methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(7);
          indexList.add(8);
          indexList.add(9);
          indexList.add(0);
          methodToIndices.put(" boolean equals(Object o) ", indexList);
        }
        randoopCoverageInfo = new randoop.util.TestCoverageInfo(10, methodToIndices);
      }
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(4, methodToIndices);
    }
  }

  public static SortedMap unmodifiableSortedMap(SortedMap m) {
    return new UnmodifiableSortedMap(m);
  }

  static class UnmodifiableSortedMap extends UnmodifiableMap implements SortedMap, Serializable {
    private static final long serialVersionUID = 1L;

    private SortedMap sm;

    UnmodifiableSortedMap(SortedMap m) {
      super(m);
      sm = m;
    }

    public Comparator comparator() {
      return sm.comparator();
    }

    public SortedMap subMap(Object fromKey, Object toKey) {
      return new UnmodifiableSortedMap(sm.subMap(fromKey, toKey));
    }

    public SortedMap headMap(Object toKey) {
      return new UnmodifiableSortedMap(sm.headMap(toKey));
    }

    public SortedMap tailMap(Object fromKey) {
      return new UnmodifiableSortedMap(sm.tailMap(fromKey));
    }

    public Object firstKey() {
      return sm.firstKey();
    }

    public Object lastKey() {
      return sm.lastKey();
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public static Collection synchronizedCollection(Collection c) {
    return new SynchronizedCollection(c);
  }

  static Collection synchronizedCollection(Collection c, Object mutex) {
    return new SynchronizedCollection(c, mutex);
  }

  static class SynchronizedCollection implements Collection, Serializable {
    private static final long serialVersionUID = 3053995032091335093L;

    Collection c;

    Object mutex;

    SynchronizedCollection(Collection c) {
      if ((((c == null) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0)) throw new NullPointerException();
      this.c = c;
      mutex = this;
    }

    SynchronizedCollection(Collection c, Object mutex) {
      this.c = c;
      this.mutex = mutex;
    }

    public int size() {
      synchronized (mutex) {
        return c.size();
      }
    }

    public boolean isEmpty() {
      synchronized (mutex) {
        return c.isEmpty();
      }
    }

    public boolean contains(Object o) {
      synchronized (mutex) {
        return c.contains(o);
      }
    }

    public Object[] toArray() {
      synchronized (mutex) {
        return c.toArray();
      }
    }

    public Object[] toArray(Object[] a) {
      synchronized (mutex) {
        return c.toArray(a);
      }
    }

    public Iterator iterator() {
      return c.iterator();
    }

    public boolean add(Object o) {
      synchronized (mutex) {
        return c.add(o);
      }
    }

    public boolean remove(Object o) {
      synchronized (mutex) {
        return c.remove(o);
      }
    }

    public boolean containsAll(Collection coll) {
      synchronized (mutex) {
        return c.containsAll(coll);
      }
    }

    public boolean addAll(Collection coll) {
      synchronized (mutex) {
        return c.addAll(coll);
      }
    }

    public boolean removeAll(Collection coll) {
      synchronized (mutex) {
        return c.removeAll(coll);
      }
    }

    public boolean retainAll(Collection coll) {
      synchronized (mutex) {
        return c.retainAll(coll);
      }
    }

    public void clear() {
      synchronized (mutex) {
        c.clear();
      }
    }

    @Override
    public String toString() {
      synchronized (mutex) {
        return c.toString();
      }
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(1, methodToIndices);
    }
  }

  public static Set synchronizedSet(Set s) {
    return new SynchronizedSet(s);
  }

  static Set synchronizedSet(Set s, Object mutex) {
    return new SynchronizedSet(s, mutex);
  }

  static class SynchronizedSet extends SynchronizedCollection implements Set {
    private static final long serialVersionUID = 1L;

    SynchronizedSet(Set s) {
      super(s);
    }

    SynchronizedSet(Set s, Object mutex) {
      super(s, mutex);
    }

    @Override
    public boolean equals(Object o) {
      synchronized (mutex) {
        return c.equals(o);
      }
    }

    @Override
    public int hashCode() {
      synchronized (mutex) {
        return c.hashCode();
      }
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public static SortedSet synchronizedSortedSet(SortedSet s) {
    return new SynchronizedSortedSet(s);
  }

  static class SynchronizedSortedSet extends SynchronizedSet implements SortedSet {
    private static final long serialVersionUID = 1L;

    private SortedSet ss;

    SynchronizedSortedSet(SortedSet s) {
      super(s);
      ss = s;
    }

    SynchronizedSortedSet(SortedSet s, Object mutex) {
      super(s, mutex);
      ss = s;
    }

    public Comparator comparator() {
      synchronized (mutex) {
        return ss.comparator();
      }
    }

    public SortedSet subSet(Object fromElement, Object toElement) {
      synchronized (mutex) {
        return new SynchronizedSortedSet(ss.subSet(fromElement, toElement), mutex);
      }
    }

    public SortedSet headSet(Object toElement) {
      synchronized (mutex) {
        return new SynchronizedSortedSet(ss.headSet(toElement), mutex);
      }
    }

    public SortedSet tailSet(Object fromElement) {
      synchronized (mutex) {
        return new SynchronizedSortedSet(ss.tailSet(fromElement), mutex);
      }
    }

    public Object first() {
      synchronized (mutex) {
        return ss.first();
      }
    }

    public Object last() {
      synchronized (mutex) {
        return ss.last();
      }
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public static List synchronizedList(List list) {
    return (list instanceof RandomAccess
        ? new SynchronizedRandomAccessList(list)
        : new SynchronizedList(list));
  }

  static List synchronizedList(List list, Object mutex) {
    return (list instanceof RandomAccess
        ? new SynchronizedRandomAccessList(list, mutex)
        : new SynchronizedList(list, mutex));
  }

  static class SynchronizedList extends SynchronizedCollection implements List {
    static final long serialVersionUID = -7754090372962971524L;

    List list;

    SynchronizedList(List list) {
      super(list);
      this.list = list;
    }

    SynchronizedList(List list, Object mutex) {
      super(list, mutex);
      this.list = list;
    }

    @Override
    public boolean equals(Object o) {
      synchronized (mutex) {
        return list.equals(o);
      }
    }

    @Override
    public int hashCode() {
      synchronized (mutex) {
        return list.hashCode();
      }
    }

    public Object get(int index) {
      synchronized (mutex) {
        return list.get(index);
      }
    }

    public Object set(int index, Object element) {
      synchronized (mutex) {
        return list.set(index, element);
      }
    }

    public void add(int index, Object element) {
      synchronized (mutex) {
        list.add(index, element);
      }
    }

    public Object remove(int index) {
      synchronized (mutex) {
        return list.remove(index);
      }
    }

    public int indexOf(Object o) {
      synchronized (mutex) {
        return list.indexOf(o);
      }
    }

    public int lastIndexOf(Object o) {
      synchronized (mutex) {
        return list.lastIndexOf(o);
      }
    }

    public boolean addAll(int index, Collection c) {
      synchronized (mutex) {
        return list.addAll(index, c);
      }
    }

    public ListIterator listIterator() {
      return list.listIterator();
    }

    public ListIterator listIterator(int index) {
      return list.listIterator(index);
    }

    public List subList(int fromIndex, int toIndex) {
      synchronized (mutex) {
        return new SynchronizedList(list.subList(fromIndex, toIndex), mutex);
      }
    }

    private Object readResolve() {
      return (list instanceof RandomAccess ? new SynchronizedRandomAccessList(list) : this);
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  static class SynchronizedRandomAccessList extends SynchronizedList implements RandomAccess {
    SynchronizedRandomAccessList(List list) {
      super(list);
    }

    SynchronizedRandomAccessList(List list, Object mutex) {
      super(list, mutex);
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
      synchronized (mutex) {
        return new SynchronizedRandomAccessList(list.subList(fromIndex, toIndex), mutex);
      }
    }

    static final long serialVersionUID = 1530674583602358482L;

    private Object writeReplace() {
      return new SynchronizedList(list);
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public static Map synchronizedMap(Map m) {
    return new SynchronizedMap(m);
  }

  private static class SynchronizedMap implements Map, Serializable {
    private static final long serialVersionUID = 1978198479659022715L;

    private Map m;

    Object mutex;

    SynchronizedMap(Map m) {
      if ((((m == null) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0)) throw new NullPointerException();
      this.m = m;
      mutex = this;
    }

    SynchronizedMap(Map m, Object mutex) {
      this.m = m;
      this.mutex = mutex;
    }

    public int size() {
      synchronized (mutex) {
        return m.size();
      }
    }

    public boolean isEmpty() {
      synchronized (mutex) {
        return m.isEmpty();
      }
    }

    public boolean containsKey(Object key) {
      synchronized (mutex) {
        return m.containsKey(key);
      }
    }

    public boolean containsValue(Object value) {
      synchronized (mutex) {
        return m.containsValue(value);
      }
    }

    public Object get(Object key) {
      synchronized (mutex) {
        return m.get(key);
      }
    }

    public Object put(Object key, Object value) {
      synchronized (mutex) {
        return m.put(key, value);
      }
    }

    public Object remove(Object key) {
      synchronized (mutex) {
        return m.remove(key);
      }
    }

    public void putAll(Map map) {
      synchronized (mutex) {
        m.putAll(map);
      }
    }

    public void clear() {
      synchronized (mutex) {
        m.clear();
      }
    }

    private transient Set keySet = null;

    private transient Set entrySet = null;

    private transient Collection values = null;

    public Set keySet() {
      synchronized (mutex) {
        if ((((keySet == null) && ++randoopCoverageInfo.branchTrue[1] != 0)
            || ++randoopCoverageInfo.branchFalse[1] == 0))
          keySet = new SynchronizedSet(m.keySet(), mutex);
        return keySet;
      }
    }

    public Set entrySet() {
      synchronized (mutex) {
        if ((((entrySet == null) && ++randoopCoverageInfo.branchTrue[2] != 0)
            || ++randoopCoverageInfo.branchFalse[2] == 0))
          entrySet = new SynchronizedSet(m.entrySet(), mutex);
        return entrySet;
      }
    }

    public Collection values() {
      synchronized (mutex) {
        if ((((values == null) && ++randoopCoverageInfo.branchTrue[3] != 0)
            || ++randoopCoverageInfo.branchFalse[3] == 0))
          values = new SynchronizedCollection(m.values(), mutex);
        return values;
      }
    }

    @Override
    public boolean equals(Object o) {
      synchronized (mutex) {
        return m.equals(o);
      }
    }

    @Override
    public int hashCode() {
      synchronized (mutex) {
        return m.hashCode();
      }
    }

    @Override
    public String toString() {
      synchronized (mutex) {
        return m.toString();
      }
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedMap (Map m) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(4, methodToIndices);
    }
  }

  public static SortedMap synchronizedSortedMap(SortedMap m) {
    return new SynchronizedSortedMap(m);
  }

  static class SynchronizedSortedMap extends SynchronizedMap implements SortedMap {
    private static final long serialVersionUID = 1L;

    private SortedMap sm;

    SynchronizedSortedMap(SortedMap m) {
      super(m);
      sm = m;
    }

    SynchronizedSortedMap(SortedMap m, Object mutex) {
      super(m, mutex);
      sm = m;
    }

    public Comparator comparator() {
      synchronized (mutex) {
        return sm.comparator();
      }
    }

    public SortedMap subMap(Object fromKey, Object toKey) {
      synchronized (mutex) {
        return new SynchronizedSortedMap(sm.subMap(fromKey, toKey), mutex);
      }
    }

    public SortedMap headMap(Object toKey) {
      synchronized (mutex) {
        return new SynchronizedSortedMap(sm.headMap(toKey), mutex);
      }
    }

    public SortedMap tailMap(Object fromKey) {
      synchronized (mutex) {
        return new SynchronizedSortedMap(sm.tailMap(fromKey), mutex);
      }
    }

    public Object firstKey() {
      synchronized (mutex) {
        return sm.firstKey();
      }
    }

    public Object lastKey() {
      synchronized (mutex) {
        return sm.lastKey();
      }
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedMap (Map m) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public static final Set EMPTY_SET = new EmptySet();

  private static class EmptySet extends AbstractSet implements Serializable {
    private static final long serialVersionUID = 1582296315990362920L;

    @Override
    public Iterator iterator() {
      return new Iterator() {
        public boolean hasNext() {
          return false;
        }

        public Object next() {
          throw new NoSuchElementException();
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean contains(Object obj) {
      return false;
    }

    private Object readResolve() {
      return EMPTY_SET;
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedMap (Map m) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public static final List EMPTY_LIST = new EmptyList();

  private static class EmptyList extends AbstractList implements RandomAccess, Serializable {
    private static final long serialVersionUID = 8842843931221139166L;

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean contains(Object obj) {
      return false;
    }

    @Override
    public Object get(int index) {
      throw new IndexOutOfBoundsException("Index: " + index);
    }

    private Object readResolve() {
      return EMPTY_LIST;
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedMap (Map m) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public static final Map EMPTY_MAP = new EmptyMap();

  private static class EmptyMap extends AbstractMap implements Serializable {
    private static final long serialVersionUID = 6428348081105594320L;

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public boolean containsKey(Object key) {
      return false;
    }

    @Override
    public boolean containsValue(Object value) {
      return false;
    }

    @Override
    public Object get(Object key) {
      return null;
    }

    @Override
    public Set keySet() {
      return EMPTY_SET;
    }

    @Override
    public Collection values() {
      return EMPTY_SET;
    }

    @Override
    public Set entrySet() {
      return EMPTY_SET;
    }

    @Override
    public boolean equals(Object o) {
      return (o instanceof Map) && ((Map) o).size() == 0;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    private Object readResolve() {
      return EMPTY_MAP;
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedMap (Map m) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public static Set singleton(Object o) {
    return new SingletonSet(o);
  }

  private static class SingletonSet extends AbstractSet implements Serializable {
    private static final long serialVersionUID = 3193687207550431679L;

    private Object element;

    SingletonSet(Object o) {
      element = o;
    }

    @Override
    public Iterator iterator() {
      return new Iterator() {
        private boolean hasNext = true;

        public boolean hasNext() {
          return hasNext;
        }

        public Object next() {
          if (hasNext) {
            hasNext = false;
            return element;
          }
          throw new NoSuchElementException();
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public int size() {
      return 1;
    }

    @Override
    public boolean contains(Object o) {
      return eq(o, element);
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedMap (Map m) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public static List singletonList(Object o) {
    return new SingletonList(o);
  }

  private static class SingletonList extends AbstractList implements RandomAccess, Serializable {
    static final long serialVersionUID = 3093736618740652951L;

    private final Object element;

    SingletonList(Object obj) {
      element = obj;
    }

    @Override
    public int size() {
      return 1;
    }

    @Override
    public boolean contains(Object obj) {
      return eq(obj, element);
    }

    @Override
    public Object get(int index) {
      if ((((index != 0) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0))
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
      return element;
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object get(int index) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(1, methodToIndices);
    }
  }

  public static Map singletonMap(Object key, Object value) {
    return new SingletonMap(key, value);
  }

  private static class SingletonMap extends AbstractMap implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Object k, v;

    SingletonMap(Object key, Object value) {
      k = key;
      v = value;
    }

    @Override
    public int size() {
      return 1;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public boolean containsKey(Object key) {
      return eq(key, k);
    }

    @Override
    public boolean containsValue(Object value) {
      return eq(value, v);
    }

    @Override
    public Object get(Object key) {
      return (eq(key, k) ? v : null);
    }

    private transient Set keySet = null;

    private transient Set entrySet = null;

    private transient Collection values = null;

    @Override
    public Set keySet() {
      if ((((keySet == null) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0)) keySet = singleton(k);
      return keySet;
    }

    @Override
    public Set entrySet() {
      if ((((entrySet == null) && ++randoopCoverageInfo.branchTrue[1] != 0)
          || ++randoopCoverageInfo.branchFalse[1] == 0))
        entrySet = singleton(new ImmutableEntry(k, v));
      return entrySet;
    }

    @Override
    public Collection values() {
      if ((((values == null) && ++randoopCoverageInfo.branchTrue[2] != 0)
          || ++randoopCoverageInfo.branchFalse[2] == 0)) values = singleton(v);
      return values;
    }

    private static class ImmutableEntry implements Map.Entry {
      final Object k;

      final Object v;

      ImmutableEntry(Object key, Object value) {
        k = key;
        v = value;
      }

      public Object getKey() {
        return k;
      }

      public Object getValue() {
        return v;
      }

      public Object setValue(Object value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean equals(Object o) {
        if ((((!(o instanceof Map.Entry)) && ++randoopCoverageInfo.branchTrue[0] != 0)
            || ++randoopCoverageInfo.branchFalse[0] == 0)) return false;
        Map.Entry e = (Map.Entry) o;
        return eq(e.getKey(), k) && eq(e.getValue(), v);
      }

      @Override
      public int hashCode() {
        return ((k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode()));
      }

      @Override
      public String toString() {
        return k + "=" + v;
      }

      private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

      static {
        java.util.Map<String, java.util.Set<Integer>> methodToIndices =
            new java.util.LinkedHashMap<>();
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(0);
          indexList.add(1);
          indexList.add(2);
          methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(3);
          indexList.add(4);
          methodToIndices.put(" Object min(Collection coll) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(5);
          indexList.add(6);
          indexList.add(7);
          methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(8);
          indexList.add(9);
          methodToIndices.put(" Object max(Collection coll) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(10);
          indexList.add(11);
          indexList.add(12);
          methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(13);
          indexList.add(14);
          indexList.add(15);
          indexList.add(16);
          indexList.add(17);
          indexList.add(18);
          indexList.add(19);
          indexList.add(20);
          methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(21);
          indexList.add(22);
          indexList.add(23);
          indexList.add(24);
          indexList.add(25);
          indexList.add(26);
          indexList.add(27);
          indexList.add(28);
          indexList.add(29);
          indexList.add(30);
          methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(0);
          methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(0);
          methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(1);
          indexList.add(0);
          methodToIndices.put(" Set keySet() ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(2);
          indexList.add(1);
          methodToIndices.put(" Set entrySet() ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(3);
          indexList.add(2);
          methodToIndices.put(" Collection values() ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(0);
          methodToIndices.put(" Object[] toArray() ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(1);
          indexList.add(2);
          indexList.add(3);
          methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(4);
          methodToIndices.put(" boolean contains(Object o) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(5);
          indexList.add(6);
          methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(7);
          indexList.add(8);
          indexList.add(9);
          indexList.add(0);
          methodToIndices.put(" boolean equals(Object o) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(0);
          methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(0);
          methodToIndices.put(" SynchronizedMap (Map m) ", indexList);
        }
        {
          java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
          indexList.add(0);
          methodToIndices.put(" Object get(int index) ", indexList);
        }
        randoopCoverageInfo = new randoop.util.TestCoverageInfo(1, methodToIndices);
      }
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(0);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        indexList.add(1);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(2);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object get(int index) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(3, methodToIndices);
    }
  }

  public static List nCopies(int n, Object o) {
    return new CopiesList(n, o);
  }

  private static class CopiesList extends AbstractList implements RandomAccess, Serializable {
    static final long serialVersionUID = 2739099268398711800L;

    int n;

    Object element;

    CopiesList(int n, Object o) {
      if ((((n < 0) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0))
        throw new IllegalArgumentException("List length = " + n);
      this.n = n;
      element = o;
    }

    @Override
    public int size() {
      return n;
    }

    @Override
    public boolean contains(Object obj) {
      return n != 0 && eq(obj, element);
    }

    @Override
    public Object get(int index) {
      if ((((index < 0 || index >= n) && ++randoopCoverageInfo.branchTrue[1] != 0)
          || ++randoopCoverageInfo.branchFalse[1] == 0))
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + n);
      return element;
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        methodToIndices.put(" Object min(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        indexList.add(9);
        methodToIndices.put(" Object max(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(10);
        indexList.add(11);
        indexList.add(12);
        methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(13);
        indexList.add(14);
        indexList.add(15);
        indexList.add(16);
        indexList.add(17);
        indexList.add(18);
        indexList.add(19);
        indexList.add(20);
        methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(21);
        indexList.add(22);
        indexList.add(23);
        indexList.add(24);
        indexList.add(25);
        indexList.add(26);
        indexList.add(27);
        indexList.add(28);
        indexList.add(29);
        indexList.add(30);
        methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(0);
        methodToIndices.put(" Set keySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(2);
        indexList.add(1);
        methodToIndices.put(" Set entrySet() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(2);
        methodToIndices.put(" Collection values() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" Object[] toArray() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        indexList.add(3);
        methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(4);
        methodToIndices.put(" boolean contains(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(7);
        indexList.add(8);
        indexList.add(9);
        indexList.add(0);
        methodToIndices.put(" boolean equals(Object o) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" SynchronizedMap (Map m) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        methodToIndices.put(" Object get(int index) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" CopiesList (int n, Object o) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(2, methodToIndices);
    }
  }

  public static Enumeration enumeration(final Collection c) {
    return new Enumeration() {
      Iterator i = c.iterator();

      public boolean hasMoreElements() {
        return i.hasNext();
      }

      public Object nextElement() {
        return i.next();
      }
    };
  }

  public static ArrayList list(Enumeration e) {
    ArrayList l = new ArrayList();
    while ((((e.hasMoreElements()) && ++randoopCoverageInfo.branchTrue[31] != 0)
        || ++randoopCoverageInfo.branchFalse[31] == 0)) l.add(e.nextElement());
    return l;
  }

  private static boolean eq(Object o1, Object o2) {
    return (o1 == null ? o2 == null : o1.equals(o2));
  }

  private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

  static {
    java.util.Map<String, java.util.Set<Integer>> methodToIndices = new java.util.LinkedHashMap<>();
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      indexList.add(1);
      indexList.add(2);
      methodToIndices.put(" Object get(ListIterator i, int index) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(3);
      indexList.add(4);
      methodToIndices.put(" Object min(Collection coll) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(5);
      indexList.add(6);
      indexList.add(7);
      methodToIndices.put(" Object min(Collection coll, Comparator comp) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(8);
      indexList.add(9);
      methodToIndices.put(" Object max(Collection coll) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(10);
      indexList.add(11);
      indexList.add(12);
      methodToIndices.put(" Object max(Collection coll, Comparator comp) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(13);
      indexList.add(14);
      indexList.add(15);
      indexList.add(16);
      indexList.add(17);
      indexList.add(18);
      indexList.add(19);
      indexList.add(20);
      methodToIndices.put(" int indexOfSubList(List source, List target) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(21);
      indexList.add(22);
      indexList.add(23);
      indexList.add(24);
      indexList.add(25);
      indexList.add(26);
      indexList.add(27);
      indexList.add(28);
      indexList.add(29);
      indexList.add(30);
      methodToIndices.put(" int lastIndexOfSubList(List source, List target) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      methodToIndices.put(" UnmodifiableCollection (Collection c) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      methodToIndices.put(" UnmodifiableMap (Map m) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(1);
      indexList.add(0);
      methodToIndices.put(" Set keySet() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(2);
      indexList.add(1);
      methodToIndices.put(" Set entrySet() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(3);
      indexList.add(2);
      methodToIndices.put(" Collection values() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      methodToIndices.put(" Object[] toArray() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(1);
      indexList.add(2);
      indexList.add(3);
      methodToIndices.put(" Object[] toArray(Object a[]) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(4);
      methodToIndices.put(" boolean contains(Object o) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(5);
      indexList.add(6);
      methodToIndices.put(" boolean containsAll(Collection coll) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(7);
      indexList.add(8);
      indexList.add(9);
      indexList.add(0);
      methodToIndices.put(" boolean equals(Object o) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      methodToIndices.put(" SynchronizedCollection (Collection c) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      methodToIndices.put(" SynchronizedMap (Map m) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      indexList.add(1);
      methodToIndices.put(" Object get(int index) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      methodToIndices.put(" CopiesList (int n, Object o) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(31);
      methodToIndices.put(" ArrayList list(Enumeration e) ", indexList);
    }
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(32, methodToIndices);
  }
}
