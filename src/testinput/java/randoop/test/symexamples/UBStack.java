package randoop.test.symexamples;

public class UBStack {
  private int[] elems;

  private int numberOfElements;

  private int max;

  public UBStack() {
    numberOfElements = 0;
    max = 5;
    elems = new int[max];
  }

  public void push(int k) {
    int index;
    boolean alreadyMember;
    alreadyMember = false;
    for (index = 0;
        (((index < numberOfElements) && ++randoopCoverageInfo.branchTrue[1] != 0)
            || ++randoopCoverageInfo.branchFalse[1] == 0);
        index++) {
      if ((((k == elems[index]) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0)) {
        alreadyMember = true;
        break;
      }
    }
    if ((((alreadyMember) && ++randoopCoverageInfo.branchTrue[4] != 0)
        || ++randoopCoverageInfo.branchFalse[4] == 0)) {
      for (int j = index;
          (((j < numberOfElements - 1) && ++randoopCoverageInfo.branchTrue[2] != 0)
              || ++randoopCoverageInfo.branchFalse[2] == 0);
          j++) {
        elems[j] = elems[j + 1];
      }
      elems[numberOfElements - 1] = k;
    } else {
      if ((((numberOfElements < max) && ++randoopCoverageInfo.branchTrue[3] != 0)
          || ++randoopCoverageInfo.branchFalse[3] == 0)) {
        elems[numberOfElements] = k;
        numberOfElements++;
        return;
      } else {
        return;
      }
    }
  }

  public void pop() {
    numberOfElements--;
  }

  public int top() {
    if ((((numberOfElements < 1) && ++randoopCoverageInfo.branchTrue[5] != 0)
        || ++randoopCoverageInfo.branchFalse[5] == 0)) {
      return -1;
    } else return elems[numberOfElements - 1];
  }

  public boolean isEmpty() {
    if ((((numberOfElements == 0) && ++randoopCoverageInfo.branchTrue[6] != 0)
        || ++randoopCoverageInfo.branchFalse[6] == 0)) return true;
    else return false;
  }

  public int maxSize() {
    return max;
  }

  public boolean isMember(int k) {
    for (int index = 0;
        (((index < numberOfElements) && ++randoopCoverageInfo.branchTrue[8] != 0)
            || ++randoopCoverageInfo.branchFalse[8] == 0);
        index++)
      if ((((k == elems[index]) && ++randoopCoverageInfo.branchTrue[7] != 0)
          || ++randoopCoverageInfo.branchFalse[7] == 0)) return true;
    return false;
  }

  public boolean equals(UBStack s) {
    if ((((s.maxSize() != max) && ++randoopCoverageInfo.branchTrue[9] != 0)
        || ++randoopCoverageInfo.branchFalse[9] == 0)) return false;
    if ((((s.getNumberOfElements() != numberOfElements)
            && ++randoopCoverageInfo.branchTrue[10] != 0)
        || ++randoopCoverageInfo.branchFalse[10] == 0)) return false;
    int[] sElems = s.getArray();
    for (int j = 0;
        (((j < numberOfElements) && ++randoopCoverageInfo.branchTrue[12] != 0)
            || ++randoopCoverageInfo.branchFalse[12] == 0);
        j++) {
      if ((((elems[j] != sElems[j]) && ++randoopCoverageInfo.branchTrue[11] != 0)
          || ++randoopCoverageInfo.branchFalse[11] == 0)) return false;
    }
    return true;
  }

  public int[] getArray() {
    int[] a;
    a = new int[max];
    for (int j = 0;
        (((j < numberOfElements) && ++randoopCoverageInfo.branchTrue[13] != 0)
            || ++randoopCoverageInfo.branchFalse[13] == 0);
        j++) a[j] = elems[j];
    return a;
  }

  public int getNumberOfElements() {
    return numberOfElements;
  }

  public boolean isFull() {
    return numberOfElements == max;
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
      methodToIndices.put(" void push(int k) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(5);
      methodToIndices.put(" int top() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(6);
      methodToIndices.put(" boolean isEmpty() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(7);
      indexList.add(8);
      methodToIndices.put(" boolean isMember(int k) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(9);
      indexList.add(10);
      indexList.add(11);
      indexList.add(12);
      methodToIndices.put(" boolean equals(UBStack s) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(13);
      methodToIndices.put(" int[] getArray() ", indexList);
    }
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(14, methodToIndices);
  }
}
