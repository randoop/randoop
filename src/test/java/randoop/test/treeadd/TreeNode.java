package randoop.test.treeadd;

/**
 * A Tree node data structure.
 */
public class TreeNode {
  private int value = 0;
  private TreeNode left = null;
  private TreeNode right = null;

  /**
   * Create a node in the tree with a given value and two children.
   * @param v the node's value
   * @param l the left child
   * @param r the right child
   */
  public TreeNode(int v, TreeNode l, TreeNode r) {
    value = v;
    left = l;
    right = r;
  }

  /**
   * Create a tree node given the two children.  The initial node
   * value is 1.
   */
  public TreeNode(TreeNode l, TreeNode r) {
    this(1, l, r);
  }

  /**
   * Create a tree node given the two children.  The initial node
   * value is 1.
   */
  public TreeNode() {
    this(1, null, null);
  }

  /**
   * Construct a subtree with the specified number of levels.
   * We recursively call the constructor to create the tree.
   * @param levels the number of levels in the subtree
   */
  public TreeNode(int levels) {
    value = 1;
    if (levels <= 1) {
      if (levels <= 0) throw new RuntimeException("Number of levels must be positive no.");
      left = null;
      right = null;
    } else {
      left = new TreeNode(levels - 1);
      right = new TreeNode(levels - 1);
    }
  }

  /**
   * Set the children of the tree
   * @param l the left child
   * @param r the right child
   */
  public void setChildren(TreeNode l, TreeNode r) {
    left = l;
    right = r;
  }

  /**
   * Create a tree with the given number of levels.
   * @param levels the number of levels in the tree
   */
  public static TreeNode createTree(int levels) {
    if (levels == 0) {
      return null;
    } else {
      TreeNode n = new TreeNode();
      n.left = createTree(levels - 1);
      n.right = createTree(levels - 1);
      return n;
    }
  }

  /**
   * Add the value of this node with the cumulative values
   * of the children of this node.
   * @return the cumulative value of this tree
   */
  public int addTree() {
    int total = value;
    if (left != null) total += left.addTree();
    if (right != null) total += right.addTree();
    return total;
  }
}
