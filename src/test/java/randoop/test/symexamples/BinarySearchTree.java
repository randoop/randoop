package randoop.test.symexamples;

public class BinarySearchTree {
  private Node root;

  private int size;

  public static class Node {
    private Node left;

    private Node right;

    private Comparable info;

    Node(Node left, Node right, Comparable info) {
      this.left = left;
      this.right = right;
      this.info = info;
    }

    Node(Comparable info) {
      this.info = info;
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  public boolean remove(Comparable info) {
    Node parent = null;
    Node current = root;
    while ((((current != null) && ++randoopCoverageInfo.branchTrue[2] != 0)
        || ++randoopCoverageInfo.branchFalse[2] == 0)) {
      int cmp = info.compareTo(current.info);
      if ((((cmp < 0) && ++randoopCoverageInfo.branchTrue[1] != 0)
          || ++randoopCoverageInfo.branchFalse[1] == 0)) {
        parent = current;
        current = current.left;
      } else if ((((cmp > 0) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0)) {
        parent = current;
        current = current.right;
      } else {
        break;
      }
    }
    if ((((current == null) && ++randoopCoverageInfo.branchTrue[3] != 0)
        || ++randoopCoverageInfo.branchFalse[3] == 0)) return false;
    Node change = removeNode(current);
    if ((((parent == null) && ++randoopCoverageInfo.branchTrue[5] != 0)
        || ++randoopCoverageInfo.branchFalse[5] == 0)) {
      root = change;
    } else if ((((parent.left == current) && ++randoopCoverageInfo.branchTrue[4] != 0)
        || ++randoopCoverageInfo.branchFalse[4] == 0)) {
      parent.left = change;
    } else {
      parent.right = change;
    }
    return true;
  }

  Node removeNode(Node current) {
    size--;
    Node left = current.left, right = current.right;
    if ((((left == null) && ++randoopCoverageInfo.branchTrue[6] != 0)
        || ++randoopCoverageInfo.branchFalse[6] == 0)) return right;
    if ((((right == null) && ++randoopCoverageInfo.branchTrue[7] != 0)
        || ++randoopCoverageInfo.branchFalse[7] == 0)) return left;
    if ((((left.right == null) && ++randoopCoverageInfo.branchTrue[8] != 0)
        || ++randoopCoverageInfo.branchFalse[8] == 0)) {
      current.info = left.info;
      current.left = left.left;
      return current;
    }
    Node temp = left;
    while ((((temp.right.right != null) && ++randoopCoverageInfo.branchTrue[9] != 0)
        || ++randoopCoverageInfo.branchFalse[9] == 0)) {
      temp = temp.right;
    }
    current.info = temp.right.info;
    temp.right = temp.right.left;
    return current;
  }

  public void add(Comparable info) {
    if ((((root == null) && ++randoopCoverageInfo.branchTrue[14] != 0)
        || ++randoopCoverageInfo.branchFalse[14] == 0)) {
      root = new Node(info);
    } else {
      Node t = root;
      while (true) {
        if ((((t.info.compareTo(info) < 0) && ++randoopCoverageInfo.branchTrue[13] != 0)
            || ++randoopCoverageInfo.branchFalse[13] == 0)) {
          if ((((t.right == null) && ++randoopCoverageInfo.branchTrue[10] != 0)
              || ++randoopCoverageInfo.branchFalse[10] == 0)) {
            t.right = new Node(info);
            break;
          } else {
            t = t.right;
          }
        } else if ((((t.info.compareTo(info) > 0) && ++randoopCoverageInfo.branchTrue[12] != 0)
            || ++randoopCoverageInfo.branchFalse[12] == 0)) {
          if ((((t.left == null) && ++randoopCoverageInfo.branchTrue[11] != 0)
              || ++randoopCoverageInfo.branchFalse[11] == 0)) {
            t.left = new Node(info);
            break;
          } else {
            t = t.left;
          }
        } else {
          return;
        }
      }
    }
    size++;
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
      indexList.add(5);
      methodToIndices.put(" boolean remove(Comparable info) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(6);
      indexList.add(7);
      indexList.add(8);
      indexList.add(9);
      methodToIndices.put(" Node removeNode(Node current) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(10);
      indexList.add(11);
      indexList.add(12);
      indexList.add(13);
      indexList.add(14);
      methodToIndices.put(" void add(Comparable info) ", indexList);
    }
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(15, methodToIndices);
  }
}
