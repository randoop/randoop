package randoop.test.symexamples;

public class IntStack {
  private Integer[] store;

  private int size;

  private static final int INITIAL_CAPACITY = 5;

  public IntStack() {
    this.store = new Integer[INITIAL_CAPACITY];
    this.size = 0;
  }

  public void push(Integer value) {
    if ((((this.size == this.store.length) && ++randoopCoverageInfo.branchTrue[0] != 0)
        || ++randoopCoverageInfo.branchFalse[0] == 0)) {
      Integer[] store = new Integer[this.store.length * 2];
      System.arraycopy(this.store, 0, store, 0, this.size);
      this.store = store;
    }
    this.store[this.size] = value;
    this.size++;
  }

  public Integer pop() {
    Integer result = this.store[this.size - 1];
    this.size--;
    if ((((this.store.length > INITIAL_CAPACITY && this.size * 2 < this.store.length)
            && ++randoopCoverageInfo.branchTrue[1] != 0)
        || ++randoopCoverageInfo.branchFalse[1] == 0)) {
      Integer[] store = new Integer[this.store.length / 2];
      System.arraycopy(this.store, 0, store, 0, this.size);
      this.store = store;
    }
    return result;
  }

  private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

  static {
    java.util.Map<String, java.util.Set<Integer>> methodToIndices = new java.util.LinkedHashMap<>();
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      methodToIndices.put(" void push(Integer value) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(1);
      methodToIndices.put(" Integer pop() ", indexList);
    }
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(2, methodToIndices);
  }
}
