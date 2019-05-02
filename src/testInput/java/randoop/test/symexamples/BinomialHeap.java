package randoop.test.symexamples;

public class BinomialHeap {
  public static class BinomialHeapNode {
    private Integer key;

    private int degree;

    private BinomialHeapNode parent;

    private BinomialHeapNode sibling;

    private BinomialHeapNode child;

    public BinomialHeapNode(Integer k) {
      key = k;
      degree = 0;
      parent = null;
      sibling = null;
      child = null;
    }

    public Integer getKey() {
      return key;
    }

    private void setKey(Integer value) {
      key = value;
    }

    public int getDegree() {
      return degree;
    }

    private void setDegree(int deg) {
      degree = deg;
    }

    public BinomialHeapNode getParent() {
      return parent;
    }

    private void setParent(BinomialHeapNode par) {
      parent = par;
    }

    public BinomialHeapNode getSibling() {
      return sibling;
    }

    private void setSibling(BinomialHeapNode nextBr) {
      sibling = nextBr;
    }

    public BinomialHeapNode getChild() {
      return child;
    }

    private void setChild(BinomialHeapNode firstCh) {
      child = firstCh;
    }

    public int getSize() {
      return (1
          + ((child == null) ? 0 : child.getSize())
          + ((sibling == null) ? 0 : sibling.getSize()));
    }

    private BinomialHeapNode reverse(BinomialHeapNode sibl) {
      BinomialHeapNode ret;
      if ((((sibling != null) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0)) ret = sibling.reverse(this);
      else ret = this;
      sibling = sibl;
      return ret;
    }

    private BinomialHeapNode findMinNode() {
      BinomialHeapNode x = this, y = this;
      Integer min = x.key;
      while ((((x != null) && ++randoopCoverageInfo.branchTrue[2] != 0)
          || ++randoopCoverageInfo.branchFalse[2] == 0)) {
        if ((((x.key.compareTo(min) < 0) && ++randoopCoverageInfo.branchTrue[1] != 0)
            || ++randoopCoverageInfo.branchFalse[1] == 0)) {
          y = x;
          min = x.key;
        }
        x = x.sibling;
      }
      return y;
    }

    private BinomialHeapNode findANodeWithKey(Integer value) {
      BinomialHeapNode temp = this, node = null;
      while ((((temp != null) && ++randoopCoverageInfo.branchTrue[6] != 0)
          || ++randoopCoverageInfo.branchFalse[6] == 0)) {
        if ((((temp.key.compareTo(value) == 0) && ++randoopCoverageInfo.branchTrue[3] != 0)
            || ++randoopCoverageInfo.branchFalse[3] == 0)) {
          node = temp;
          break;
        }
        if ((((temp.child == null) && ++randoopCoverageInfo.branchTrue[5] != 0)
            || ++randoopCoverageInfo.branchFalse[5] == 0)) temp = temp.sibling;
        else {
          node = temp.child.findANodeWithKey(value);
          if ((((node == null) && ++randoopCoverageInfo.branchTrue[4] != 0)
              || ++randoopCoverageInfo.branchFalse[4] == 0)) temp = temp.sibling;
          else break;
        }
      }
      return node;
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        methodToIndices.put(" BinomialHeapNode reverse(BinomialHeapNode sibl) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" BinomialHeapNode findMinNode() ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        indexList.add(5);
        indexList.add(6);
        methodToIndices.put(" BinomialHeapNode findANodeWithKey(Integer value) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(7, methodToIndices);
    }
  }

  private BinomialHeapNode Nodes;

  private int size;

  public BinomialHeap() {
    Nodes = null;
    size = 0;
  }

  private Integer findMinimum() {
    return Nodes.findMinNode().key;
  }

  private void merge(BinomialHeapNode binHeap) {
    BinomialHeapNode temp1 = Nodes, temp2 = binHeap;
    while (((((temp1 != null) && (temp2 != null)) && ++randoopCoverageInfo.branchTrue[4] != 0)
        || ++randoopCoverageInfo.branchFalse[4] == 0)) {
      if ((((temp1.degree == temp2.degree) && ++randoopCoverageInfo.branchTrue[3] != 0)
          || ++randoopCoverageInfo.branchFalse[3] == 0)) {
        BinomialHeapNode tmp = temp2;
        temp2 = temp2.sibling;
        tmp.sibling = temp1.sibling;
        temp1.sibling = tmp;
        temp1 = tmp.sibling;
      } else {
        if ((((temp1.degree < temp2.degree) && ++randoopCoverageInfo.branchTrue[2] != 0)
            || ++randoopCoverageInfo.branchFalse[2] == 0)) {
          if (((((temp1.sibling == null) || (temp1.sibling.degree > temp2.degree))
                  && ++randoopCoverageInfo.branchTrue[0] != 0)
              || ++randoopCoverageInfo.branchFalse[0] == 0)) {
            BinomialHeapNode tmp = temp2;
            temp2 = temp2.sibling;
            tmp.sibling = temp1.sibling;
            temp1.sibling = tmp;
            temp1 = tmp.sibling;
          } else temp1 = temp1.sibling;
        } else {
          BinomialHeapNode tmp = temp1;
          temp1 = temp2;
          temp2 = temp2.sibling;
          temp1.sibling = tmp;
          if ((((tmp == Nodes) && ++randoopCoverageInfo.branchTrue[1] != 0)
              || ++randoopCoverageInfo.branchFalse[1] == 0)) Nodes = temp1;
        }
      }
    }
    if ((((temp1 == null) && ++randoopCoverageInfo.branchTrue[6] != 0)
        || ++randoopCoverageInfo.branchFalse[6] == 0)) {
      temp1 = Nodes;
      while ((((temp1.sibling != null) && ++randoopCoverageInfo.branchTrue[5] != 0)
          || ++randoopCoverageInfo.branchFalse[5] == 0)) temp1 = temp1.sibling;
      temp1.sibling = temp2;
    }
  }

  private void unionNodes(BinomialHeapNode binHeap) {
    merge(binHeap);
    BinomialHeapNode prevTemp = null, temp = Nodes, nextTemp = Nodes.sibling;
    while ((((nextTemp != null) && ++randoopCoverageInfo.branchTrue[10] != 0)
        || ++randoopCoverageInfo.branchFalse[10] == 0)) {
      if (((((temp.degree != nextTemp.degree)
                  || ((nextTemp.sibling != null) && (nextTemp.sibling.degree == temp.degree)))
              && ++randoopCoverageInfo.branchTrue[9] != 0)
          || ++randoopCoverageInfo.branchFalse[9] == 0)) {
        prevTemp = temp;
        temp = nextTemp;
      } else {
        if ((((temp.key.compareTo(nextTemp.key) <= 0) && ++randoopCoverageInfo.branchTrue[8] != 0)
            || ++randoopCoverageInfo.branchFalse[8] == 0)) {
          temp.sibling = nextTemp.sibling;
          nextTemp.parent = temp;
          nextTemp.sibling = temp.child;
          temp.child = nextTemp;
          temp.degree++;
        } else {
          if ((((prevTemp == null) && ++randoopCoverageInfo.branchTrue[7] != 0)
              || ++randoopCoverageInfo.branchFalse[7] == 0)) Nodes = nextTemp;
          else prevTemp.sibling = nextTemp;
          temp.parent = nextTemp;
          temp.sibling = nextTemp.child;
          nextTemp.child = temp;
          nextTemp.degree++;
          temp = nextTemp;
        }
      }
      nextTemp = temp.sibling;
    }
  }

  public void insert(Integer value) {
    if ((((value.compareTo(new Integer(0)) > 0) && ++randoopCoverageInfo.branchTrue[12] != 0)
        || ++randoopCoverageInfo.branchFalse[12] == 0)) {
      BinomialHeapNode temp = new BinomialHeapNode(value);
      if ((((Nodes == null) && ++randoopCoverageInfo.branchTrue[11] != 0)
          || ++randoopCoverageInfo.branchFalse[11] == 0)) {
        Nodes = temp;
        size = 1;
      } else {
        unionNodes(temp);
        size++;
      }
    }
  }

  public Integer extractMin() {
    if ((((Nodes == null) && ++randoopCoverageInfo.branchTrue[13] != 0)
        || ++randoopCoverageInfo.branchFalse[13] == 0)) return new Integer(-1);
    BinomialHeapNode temp = Nodes, prevTemp = null;
    BinomialHeapNode minNode = Nodes.findMinNode();
    while ((((temp.key.compareTo(minNode.key) != 0) && ++randoopCoverageInfo.branchTrue[14] != 0)
        || ++randoopCoverageInfo.branchFalse[14] == 0)) {
      prevTemp = temp;
      temp = temp.sibling;
    }
    if ((((prevTemp == null) && ++randoopCoverageInfo.branchTrue[15] != 0)
        || ++randoopCoverageInfo.branchFalse[15] == 0)) Nodes = temp.sibling;
    else prevTemp.sibling = temp.sibling;
    temp = temp.child;
    BinomialHeapNode fakeNode = temp;
    while ((((temp != null) && ++randoopCoverageInfo.branchTrue[16] != 0)
        || ++randoopCoverageInfo.branchFalse[16] == 0)) {
      temp.parent = null;
      temp = temp.sibling;
    }
    if (((((Nodes == null) && (fakeNode == null)) && ++randoopCoverageInfo.branchTrue[19] != 0)
        || ++randoopCoverageInfo.branchFalse[19] == 0)) size = 0;
    else {
      if (((((Nodes == null) && (fakeNode != null)) && ++randoopCoverageInfo.branchTrue[18] != 0)
          || ++randoopCoverageInfo.branchFalse[18] == 0)) {
        Nodes = fakeNode.reverse(null);
        size = Nodes.getSize();
      } else {
        if (((((Nodes != null) && (fakeNode == null)) && ++randoopCoverageInfo.branchTrue[17] != 0)
            || ++randoopCoverageInfo.branchFalse[17] == 0)) size = Nodes.getSize();
        else {
          unionNodes(fakeNode.reverse(null));
          size = Nodes.getSize();
        }
      }
    }
    return minNode.key;
  }

  private void decreaseKeyVariable(Integer old_value, Integer new_value) {
    BinomialHeapNode temp = Nodes.findANodeWithKey(old_value);
    temp.key = new_value;
    BinomialHeapNode tempParent = temp.parent;
    while (((((tempParent != null) && (temp.key.compareTo(tempParent.key) < 0))
            && ++randoopCoverageInfo.branchTrue[20] != 0)
        || ++randoopCoverageInfo.branchFalse[20] == 0)) {
      Integer z = temp.key;
      temp.key = tempParent.key;
      tempParent.key = z;
      temp = tempParent;
      tempParent = tempParent.parent;
    }
  }

  public void delete(Integer value) {
    if (((((Nodes != null) && (Nodes.findANodeWithKey(value) != null))
            && ++randoopCoverageInfo.branchTrue[21] != 0)
        || ++randoopCoverageInfo.branchFalse[21] == 0)) {
      decreaseKeyVariable(value, new Integer(findMinimum().intValue() - 1));
      extractMin();
    }
  }

  private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

  static {
    java.util.Map<String, java.util.Set<Integer>> methodToIndices = new java.util.LinkedHashMap<>();
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      methodToIndices.put(" BinomialHeapNode reverse(BinomialHeapNode sibl) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(1);
      indexList.add(2);
      methodToIndices.put(" BinomialHeapNode findMinNode() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(3);
      indexList.add(4);
      indexList.add(5);
      indexList.add(6);
      methodToIndices.put(" BinomialHeapNode findANodeWithKey(Integer value) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      indexList.add(1);
      indexList.add(2);
      indexList.add(3);
      indexList.add(4);
      indexList.add(5);
      indexList.add(6);
      methodToIndices.put(" void merge(BinomialHeapNode binHeap) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(7);
      indexList.add(8);
      indexList.add(9);
      indexList.add(10);
      methodToIndices.put(" void unionNodes(BinomialHeapNode binHeap) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(11);
      indexList.add(12);
      methodToIndices.put(" void insert(Integer value) ", indexList);
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
      methodToIndices.put(" Integer extractMin() ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(20);
      methodToIndices.put(
          " void decreaseKeyVariable(Integer old_value, Integer new_value) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(21);
      methodToIndices.put(" void delete(Integer value) ", indexList);
    }
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(22, methodToIndices);
  }
}
