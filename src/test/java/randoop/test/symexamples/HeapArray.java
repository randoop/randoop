package randoop.test.symexamples;

public class HeapArray {
  private int size;

  private Comparable[] array;

  public Object extractMax() {
    if ((((size == 0) && ++randoopCoverageInfo.branchTrue[0] != 0)
        || ++randoopCoverageInfo.branchFalse[0] == 0)) throw new IllegalArgumentException();
    Object o = array[0];
    array[0] = array[--size];
    array[size] = null;
    heapifyDown(0);
    return o;
  }

  private void heapifyDown(int index) {
    int son;
    Comparable elm = array[index];
    son = index * 2 + 1;
    if (((((son + 1 < size) && (array[son].compareTo(array[son + 1]) < 0))
            && ++randoopCoverageInfo.branchTrue[1] != 0)
        || ++randoopCoverageInfo.branchFalse[1] == 0)) son = son + 1;
    while (((((son < size) && (elm.compareTo(array[son]) < 0))
            && ++randoopCoverageInfo.branchTrue[4] != 0)
        || ++randoopCoverageInfo.branchFalse[4] == 0)) {
      array[index] = array[son];
      index = son;
      son = son * 2 + 1;
      if (((((son + 1 < size) && (array[son].compareTo(array[son + 1]) < 0))
              && ++randoopCoverageInfo.branchTrue[2] != 0)
          || ++randoopCoverageInfo.branchFalse[2] == 0)) son = son + 1;
      if ((((son >= size) && ++randoopCoverageInfo.branchTrue[3] != 0)
          || ++randoopCoverageInfo.branchFalse[3] == 0)) break;
    }
    array[index] = elm;
  }

  public boolean insert(Comparable element) {
    if ((((size >= array.length) && ++randoopCoverageInfo.branchTrue[6] != 0)
        || ++randoopCoverageInfo.branchFalse[6] == 0)) {
      Comparable[] temp = new Comparable[2 * array.length + 1];
      for (int i = 0;
          (((i < size) && ++randoopCoverageInfo.branchTrue[5] != 0)
              || ++randoopCoverageInfo.branchFalse[5] == 0);
          i++) temp[i] = array[i];
      array = temp;
    }
    array[size] = element;
    heapifyUp(size);
    size++;
    return true;
  }

  private void heapifyUp(int index) {
    while ((((index > 0 && array[(index - 1) / 2].compareTo(array[index]) < 0)
            && ++randoopCoverageInfo.branchTrue[7] != 0)
        || ++randoopCoverageInfo.branchFalse[7] == 0)) {
      Comparable t = array[index];
      array[index] = array[(index - 1) / 2];
      array[(index - 1) / 2] = t;
      index = (index - 1) / 2;
    }
  }

  public HeapArray() {
    array = new Integer[5];
  }

  private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

  static {
    java.util.Map<String, java.util.Set<Integer>> methodToIndices = new java.util.LinkedHashMap<>();
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      methodToIndices.put(" Object extractMax() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(1);
      indexList.add(2);
      indexList.add(3);
      indexList.add(4);
      methodToIndices.put(" void heapifyDown(int index) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(5);
      indexList.add(6);
      methodToIndices.put(" boolean insert(Comparable element) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(7);
      methodToIndices.put(" void heapifyUp(int index) ", indexList);
    }
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(8, methodToIndices);
  }
}
