package randoop.test.issta2006;

//Taken from JPF examples directory.

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

//XXX not implemented, but keeping because historical and might want to implement eventually
//import randoop.NotPartOfState;

// FibHeap.java
// from : http://sciris.shu.edu/~borowski/Puzzle/Puzzle.html

public class FibHeap {

  private static long startTime = System.currentTimeMillis();

  public Node min;

  private int n;

  public FibHeap() {}

  //    --------------------------------------------------------------------
  private static void outputTestSequence(int number) {}

  //public native boolean checkAbstractState(int which);

  // XXX see note above
  //   @NotPartOfState
  private Vector<Node> cachedNodes = new Vector<>();

  /**
   * Gives information similar to branch coverage. Each element is created by {@link #gen}. Each
   * element is a string of the form branchNumber + "," + node1 + node2 + extra, where branchNumber
   * is a branch output in the program and nodeN is a brief string fingerprint of the node; see
   * {@link #nodeFingerprint}.
   */
  public static Set<String> branchFingerprints = new HashSet<>();

  // private static Set abs_states = new HashSet();

  public static int counter = 0;

  /** Return a string "1" or "0" depending on whether the argument is true or false. */
  private static String asBinary(boolean b) {
    return b ? "1" : "0";
  }

  /**
   * Given a node, produces a 5-character fingerprint of the node, where each character is '0' or
   * '1'. Returns "null" if the argument is null.
   */
  private static String nodeFingerprint(Node n) {
    if (n == null) {
      return "null";
    } else {
      return asBinary(n.child == null)
          + asBinary(n.parent == null)
          + asBinary(n.right == n)
          + asBinary(n.left == n)
          + asBinary(n.degree == 0);
    }
  }

  private static int gen_native(int br, Node n, Node m) {
    String res = br + ",";
    //        For Basic Block Coverage
    //        START comment here

    res += nodeFingerprint(n);
    res += nodeFingerprint(m);
    if (n != null && m != null) {
      // commented out because of symbolic execution...
      //        res += asBinary(n.cost>m.cost);
      res += asBinary(n.child == m);
      res += asBinary(m.child == n);
    }
    //For Basic Block Coverage
    //END comment here

    if (!branchFingerprints.contains(res)) {
      branchFingerprints.add(res);
      // System.out.println("TIME=" + (System.currentTimeMillis() - startTime));
      System.out.println("Test case number " + branchFingerprints.size() + " for '" + res + "': ");
      counter = branchFingerprints.size();
      return branchFingerprints.size();
    }
    return 0;
  }

  /**
   * This method constructs a string representation of its three arguments and inserts that in the
   * {@link #branchFingerprints} set; see it for documentation of the output of this method.
   *
   * <p>In this file, gen() is called as the first statement, on both branches of every conditional.
   * Therefore, at the end of execution, {@link #branchFingerprints} contains very fine-grained
   * information about branch coverege: for each branch, each time it was executed, the relevant
   * node(s).
   */
  private static void gen(int br, Node n, Node m) { //SPECIFY
    int c = gen_native(br, n, m); //SPECIFY
    if (c != 0) outputTestSequence(c);
  }

  //-------------------------------------------------------------------

  private void cascadingCut(Node y) {
    Node z = y.parent;
    if (z != null) {
      if (!y.mark) {
        gen(0, y, null);
        y.mark = true;
      } else {
        gen(1, y, null);
        cut(y, z);
        cascadingCut(z);
      }
    } else {
      gen(2, y, null);
    }
  }

  private void consolidate() {
    int D = n + 1;
    Node[] A = new Node[D];
    for (int i = 0; i < D; i++) {
      gen(3, A[i], null);
      A[i] = null;
    }

    int k = 0;
    Node x = min;
    if (x != null) {
      k++;
      for (x = x.right; x != min; x = x.right) {
        gen(4, x, null);
        k++;
      }
    }
    while (k > 0) {
      int d = x.degree;
      Node rightNode = x.right;
      gen(5, x, null);
      while (A[d] != null) {
        Node y = A[d];
        if (x.cost > y.cost) {
          gen(6, x, y);
          Node temp = y;
          y = x;
          x = temp;
        } else {
          gen(7, x, y);
        }
        link(y, x);
        A[d] = null;
        d++;
      }

      gen(8, x, null);
      A[d] = x;
      x = rightNode;
      k--;
    }

    min = null;
    for (int i = 0; i < D; i++)
      if (A[i] != null)
        if (min != null) {
          gen(9, A[i], null);
          A[i].left.right = A[i].right;
          A[i].right.left = A[i].left;
          A[i].left = min;
          A[i].right = min.right;
          min.right = A[i];
          A[i].right.left = A[i];
          if (A[i].cost < min.cost) {
            gen(10, A[i], min);
            min = A[i];
          } else {
            gen(11, A[i], min);
          }
        } else {
          gen(12, A[i], null);
          min = A[i];
        }
  }

  private void cut(Node x, Node y) {
    x.left.right = x.right;
    x.right.left = x.left;
    y.degree--;
    if (y.child == x) {
      gen(13, x, y);
      y.child = x.right;
    } else {
      gen(20, x, y);
    }
    if (y.degree == 0) {
      gen(14, y, x);
      y.child = null;
    } else {
      gen(24, x, y);
    }
    x.left = min;
    x.right = min.right;
    min.right = x;
    x.right.left = x;
    x.parent = null;
    x.mark = false;
  }

  public void decreaseKey(Node x, int c) {
    if (c > x.cost) {
      //System.err.println("Error: new key is greater than current key.");
      return;
    }
    x.cost = c;
    Node y = x.parent;

    if ((y != null) && (x.cost < y.cost)) {
      cut(x, y);
      cascadingCut(y);
    }

    //System.out.println("min = " + min);

    if (x.cost < min.cost) min = x;
  }

  public static java.util.Random rand = new java.util.Random(0);

  public void deleteRandomNode() {
    if (cachedNodes.size() == 0) return;
    delete(cachedNodes.get(rand.nextInt(cachedNodes.size())));
  }

  public void delete(Node node) {
    decreaseKey(node, Integer.MIN_VALUE);
    removeMin();
  }

  public boolean empty() {
    return min == null;
  }

  public void insert(int c) {
    Node n = new Node(c, this);
    cachedNodes.add(n);
    insert(n);
  }

  public Node insert(Node toInsert) {
    if (min != null) {
      toInsert.left = min;
      toInsert.right = min.right;
      min.right = toInsert;
      toInsert.right.left = toInsert;
      if (toInsert.cost < min.cost) {
        gen(21, min, null);
        min = toInsert;
      } else {
        gen(22, min, null);
      }
    } else {
      min = toInsert;
      gen(23, min, null);
    }
    n++;
    return toInsert;
  }

  private void link(Node node1, Node node2) {
    node1.left.right = node1.right;
    node1.right.left = node1.left;
    node1.parent = node2;
    if (node2.child == null) {
      gen(15, node1, node2);
      node2.child = node1;
      node1.right = node1;
      node1.left = node1;
    } else {
      gen(16, node1, node2);
      node1.left = node2.child;
      node1.right = node2.child.right;
      node2.child.right = node1;
      node1.right.left = node1;
    }
    node2.degree++;
    node1.mark = false;
  }

  public Node min() {
    return min;
  }

  public Node removeMin() {
    Node z = min;
    if (z != null) {
      int i = z.degree;
      Node x = z.child;
      while (i > 0) {
        gen(17, x, z);
        Node nextChild = x.right;
        x.left.right = x.right;
        x.right.left = x.left;
        x.left = min;
        x.right = min.right;
        min.right = x;
        x.right.left = x;
        x.parent = null;
        x = nextChild;
        i--;
      }
      z.left.right = z.right;
      z.right.left = z.left;
      if (z == z.right) {
        gen(18, x, z);
        min = null;
      } else {
        gen(19, x, z);
        min = z.right;
        consolidate();
      }

      n--;
    }
    cachedNodes.remove(z);
    return z;
  }

  public int size() {
    return n;
  }

  public static FibHeap union(FibHeap heap1, FibHeap heap2) {
    FibHeap heap = new FibHeap();
    if ((heap1 != null) && (heap2 != null)) {
      heap.min = heap1.min;
      if (heap.min != null) {
        if (heap2.min != null) {
          heap.min.right.left = heap2.min.left;
          heap2.min.left.right = heap.min.right;
          heap.min.right = heap2.min;
          heap2.min.left = heap.min;
          if (heap2.min.cost < heap1.min.cost) heap.min = heap2.min;
        }
      } else heap.min = heap2.min;
      heap.n = heap1.n + heap2.n;
    }
    return heap;
  }

  public static void main(String[] Argv) {
    FibHeap h = new FibHeap();

    h.insert(3);
    System.out.println(h.min().cost);
    h.insert(2);
    System.out.println(h.min().cost);
    h.insert(4);
    System.out.println(h.min().cost);
    h.insert(1);
    System.out.println(h.min().cost);
    h.removeMin();
    System.out.println(h.min().cost);
    h.removeMin();
    System.out.println(h.min().cost);
    h.removeMin();
    System.out.println(h.min().cost);
    h.removeMin();
    h.removeMin();
    h.removeMin();
  }
}
