package randoop.test.issta2006;

import java.util.ArrayList;
import java.util.List;

//Taken from JPF examples directory.

public class Node {

  public Node parent, left, right, child;

  public boolean mark = false;
  public int cost, degree = 0;

  private FibHeap heap;

  public Node(int c, FibHeap heap) {
    cost = c;
    right = this;
    left = this;
    this.heap = heap;
  }

  public String toString(int k, boolean flag) {
    String res = "{" + mark + " ";
    if (flag) res += cost + " ";
    if (k != 0) {
      if (left == null || left == this) res += "null";
      else res += left.toString(k - 1, flag);
      if (right == null || right == this) res += "null";
      else res += right.toString(k - 1, flag);
      if (child == null) res += "null";
    }
    return res;
  }

  @Override
  public String toString() {
    return toString(0, true);
  }

  public List<Object> getAbstraction() {
    List<Object> retval = new ArrayList<>();
    retval.add(parent);
    retval.add(left);
    retval.add(right);
    retval.add(child);
    retval.add(
        "min_cost_rel:"
            + Integer.valueOf(heap.min == null ? -1 : heap.min.cost)
                .compareTo(Integer.valueOf(this.cost)));
    return retval;
  }

  public boolean shouldAbstract() {
    return true;
  }
}
