package randoop.test;
/**
 * A class that represents a value to be sorted by the <tt>BiSort</tt>
 * algorithm.  We represents a values as a node in a binary tree.
 */
public class BiSortVal {
  private int value;
  private BiSortVal left;
  private BiSortVal right;

  static final boolean FORWARD = false;
  static final boolean BACKWARD = true;

  // These are used by the Olden benchmark random no. generator
  private static final int CONST_m1 = 10000;
  private static final int CONST_b = 31415821;
  static final int RANGE = 100;

  /**
   * Constructor for a node representing a value in the bitonic sort tree.
   * @param v the integer value which is the sort key
   */
  public BiSortVal(int v) {
    value = v;
    left = right = null;
  }

  /**
   * Create a random tree of value to be sorted using the bitonic sorting algorithm.
   *
   * @param size the number of values to create
   * @param seed a random number generator seed value
   * @return the root of the (sub) tree
   */
  public static BiSortVal createTree(int size, int seed) {
    if (size > 1) {
      seed = random(seed);
      int next_val = seed % RANGE;

      BiSortVal retval = new BiSortVal(next_val);
      retval.left = createTree(size / 2, seed);
      retval.right = createTree(size / 2, skiprand(seed, size + 1));
      return retval;
    } else {
      return null;
    }
  }

  /**
   * Perform a bitonic sort based upon the Bilardi and Nicolau algorithm.
   *
   * @param spr_val the "spare" value in the algorithm
   * @param direction the direction of the sort (forward or backward)
   * @return the new "spare" value
   */
  public int bisort(int spr_val, boolean direction) {
    if (left == null) {
      if ((value > spr_val) ^ direction) {
        int tmpval = spr_val;
        spr_val = value;
        value = tmpval;
      }
    } else {
      int val = value;
      value = left.bisort(val, direction);
      boolean ndir = !direction;
      spr_val = right.bisort(spr_val, ndir);
      spr_val = bimerge(spr_val, direction);
    }
    return spr_val;
  }

  /**
   * Perform the merge part of the bitonic sort.  The merge part does
   * the actualy sorting.
   * @param spr_val the "spare" value in the algorithm
   * @param direction the direction of the sort (forward or backward)
   * @return the new "spare" value
   */
  public int bimerge(int spr_val, boolean direction) {
    int rv = value;
    BiSortVal pl = left;
    BiSortVal pr = right;

    boolean rightexchange = (rv > spr_val) ^ direction;
    if (rightexchange) {
      value = spr_val;
      spr_val = rv;
    }

    while (pl != null) {
      int lv = pl.value;
      BiSortVal pll = pl.left;
      BiSortVal plr = pl.right;
      rv = pr.value;
      BiSortVal prl = pr.left;
      BiSortVal prr = pr.right;

      boolean elementexchange = (lv > rv) ^ direction;
      if (rightexchange) {
        if (elementexchange) {
          pl.swapValRight(pr);
          pl = pll;
          pr = prl;
        } else {
          pl = plr;
          pr = prr;
        }
      } else {
        if (elementexchange) {
          pl.swapValLeft(pr);
          pl = plr;
          pr = prr;
        } else {
          pl = pll;
          pr = prl;
        }
      }
    }

    if (left != null) {
      value = left.bimerge(value, direction);
      spr_val = right.bimerge(spr_val, direction);
    }
    return spr_val;
  }

  /**
   * Swap the values and the right subtrees.
   * @param n the other subtree involved in the swap
   */
  public void swapValRight(BiSortVal n) {
    int tmpv = n.value;
    BiSortVal tmpr = n.right;

    n.value = value;
    n.right = right;

    value = tmpv;
    right = tmpr;
  }

  /**
   * Swap the values and the left subtrees.
   * @param n the other subtree involved in the swap
   */
  public void swapValLeft(BiSortVal n) {
    int tmpv = n.value;
    BiSortVal tmpl = n.left;

    n.value = value;
    n.left = left;

    value = tmpv;
    left = tmpl;
  }

  /**
   * Print out the nodes in the binary tree in infix order.
   */
  public void inOrder() {
    if (left != null) left.inOrder();
    //System.out.println(value + " " + hashCode());
    if (right != null) right.inOrder();
  }

  /**
   * A random generator.  The original Olden benchmark uses its
   * own random generator.  We use the same one in the Java version.
   * @return the next random number in the sequence
   */
  private static int mult(int p, int q) {
    int p1 = p / CONST_m1;
    int p0 = p % CONST_m1;
    int q1 = q / CONST_m1;
    int q0 = q % CONST_m1;
    return (((p0 * q1 + p1 * q0) % CONST_m1) * CONST_m1 + p0 * q0);
  }

  /**
   * A routine to skip the next <i>n</i> random numbers.
   * @param seed the current random number seed
   * @param n the number of numbers to skip
   */
  private static int skiprand(int seed, int n) {
    for (; n != 0; n--) seed = random(seed);
    return seed;
  }

  /**
   * Return a random number based upon the seed value.
   * @param seed the random number seed value
   * @return a random number based upon the seed value
   */
  public static int random(int seed) {
    int tmp = mult(seed, CONST_b) + 1;
    return tmp;
  }
}
