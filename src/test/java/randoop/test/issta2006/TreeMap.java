package randoop.test.issta2006;

import java.util.HashSet;
import java.util.Set;

//Taken from JPF examples directory.

public class TreeMap {

  private static long startTime = System.currentTimeMillis();

  // @NotPartOfState
  // private Comparator comparator = null;

  private transient Entry root = null;

  private transient int size = 0;

  private void incrementSize() {
    /*modCount++;*/
    size++;
  }

  private void decrementSize() {
    /*modCount++;*/
    size--;
  }

  //--------------------------------------------------------------------
  private static void outputTestSequence(int number) {}

  private native boolean checkAbstractState(int which);

  public static Set<String> tests = new HashSet<>();

  public static int counter = 0;

  private static int gen_native(int br, Entry e, Entry root) {

    String res = br + ",";
    //For Basic Block Coverage
    //START comment here

    if (e == null) {
      res += "entry=null";
    } else {
      Entry e_left = e.left;
      res += (e_left == null) ? "L-" : "L+";
      Entry e_right = e.right;
      res += (e_right == null) ? "R-" : "R+";
      Entry e_parent = e.parent;
      res += (e_parent == null) ? "P-" : "P+";
      boolean e_color = e.color;
      res += (e_color == false) ? "RED" : "BLACK";
      res += (e == root) ? "root" : "";
    }
    //For Basic Block Coverage
    //END comment here

    if (!tests.contains(res)) {
      tests.add(res);
      System.out.println("TIME=" + (System.currentTimeMillis() - startTime));
      System.out.println("Test case number " + tests.size() + " for '" + res + "': ");
      //System.out.println("tree = " + tree);
      counter = tests.size();
      return tests.size();
    }
    return 0;
  }

  void gen(int branch, Entry e) { //SPECIFY
    int c = gen_native(branch, e, root); //SPECIFY
    if (c != 0) outputTestSequence(c);
  }

  //-------------------------------------------------------------------

  public TreeMap() {}

  public int size() {
    return size;
  }

  public boolean containsKey(int key) {
    return getEntry(key) != null;
  }

  private Entry getEntry(int key) {
    Entry p = root;
    while (p != null) {
      if (key == p.key) return p;
      else if (key < p.key) p = p.left;
      else p = p.right;
    }
    return null;
  }

  public void put(int key) {
    Entry t = root;

    if (t == null) {
      incrementSize();
      root = new Entry(key, null);
      return;
    }

    while (true) {
      if (key == t.key) {
        return;
      } else if (key < t.key) {
        if (t.left != null) {
          t = t.left;
        } else {
          incrementSize();
          t.left = new Entry(key, t);
          fixAfterInsertion(t.left);
          return;
        }
      } else { // key > t.key
        if (t.right != null) {
          t = t.right;
        } else {
          incrementSize();
          t.right = new Entry(key, t);
          fixAfterInsertion(t.right);
          return;
        }
      }
    }
  }

  public void remove(int key) {
    Entry p = getEntry(key);
    if (p == null) {
      return;
    }

    deleteEntry(p);
    return;
  }

  public void print() {
    //System.out.println("*******************************************");
    if (root != null) root.print(0);
    //System.out.println("*******************************************");
  }

  @Override
  public String toString() {
    String res = "";
    if (root != null) res = root.toString();
    return res;
  }

  public String concreteString(int max_level) {
    String res = "";
    if (root != null) res = root.concreteString(max_level, 0);
    return res;
  }

  private static final boolean RED = false;

  private static final boolean BLACK = true;

  private static class Entry {
    int key;

    Entry left = null;

    Entry right = null;

    Entry parent;

    boolean color = BLACK;

    Entry(int key, Entry parent) {
      this.key = key;
      this.parent = parent;
    }

    Entry(int key, Entry left, Entry right, Entry parent, boolean color) {
      this.key = key;
      this.left = left;
      this.right = right;
      this.parent = parent;
      this.color = color;
    }

    public int getKey() {
      return key;
    }

    @Override
    public String toString() {
      String res = "{ " + (color == BLACK ? "B" : "R") + " ";
      if (left == null) res += "null";
      else res += left.toString();
      res += " ";
      if (right == null) res += "null";
      else res += right.toString();
      res += " }";
      return res;
    }

    public String concreteString(int max_level, int cur_level) {
      String res;
      if (cur_level == max_level) {
        res = "{ subtree }";
        //        System.out.println("Brekekek");
      } else {
        res = "{ " + (color == BLACK ? "B" : "R") + key + " ";
        if (left == null) res += "null";
        else res += left.concreteString(max_level, cur_level + 1);
        res += " ";
        if (right == null) res += "null";
        else res += right.concreteString(max_level, cur_level + 1);
        res += " }";
      }

      return res;
    }

    public void print(int k) {
      for (int i = 0; i < k; i++) {
        //System.out.print(" ");
      }
      //System.out.println(key + (color == BLACK ? "(B)" : "(R)"));
      for (int i = 0; i < k; i++) {
        //System.out.print(" ");
      }
      //System.out.println("L:");
      if (left != null) {
        left.print(k + 2);
      }

      for (int i = 0; i < k; i++) {
        //System.out.print(" ");
      }
      //System.out.println("R:");
      if (right != null) right.print(k + 2);
    }
  }

  private Entry successor(Entry t) {
    if (t == null) {
      gen(7, t);
      return null;
    } else if (t.right != null) {
      Entry p = t.right;
      while (p.left != null) {
        gen(8, t);
        p = p.left;
      }
      return p;
    } else {
      Entry p = t.parent;
      Entry ch = t;
      while (p != null && ch == p.right) {
        gen(9, t);
        ch = p;
        p = p.parent;
      }
      return p;
    }
  }

  private static boolean colorOf(Entry p) {
    return (p == null ? BLACK : p.color);
  }

  private static Entry parentOf(Entry p) {
    return (p == null ? null : p.parent);
  }

  private static void setColor(Entry p, boolean c) {
    if (p != null) p.color = c;
  }

  private static Entry leftOf(Entry p) {
    return (p == null) ? null : p.left;
  }

  private static Entry rightOf(Entry p) {
    return (p == null) ? null : p.right;
  }

  /** From CLR */
  private void rotateLeft(Entry p) {
    Entry r = p.right;
    p.right = r.left;
    if (r.left != null) {
      gen(10, p);
      r.left.parent = p;
    }
    r.parent = p.parent;
    if (p.parent == null) {
      gen(11, p);
      root = r;
    } else if (p.parent.left == p) {
      gen(12, p);
      p.parent.left = r;
    } else {
      gen(13, p);
      p.parent.right = r;
    }
    r.left = p;
    p.parent = r;
  }

  /** From CLR */
  private void rotateRight(Entry p) {
    Entry l = p.left;
    p.left = l.right;
    if (l.right != null) {
      gen(14, p);
      l.right.parent = p;
    }
    l.parent = p.parent;
    if (p.parent == null) {
      gen(15, p);
      root = l;
    } else if (p.parent.right == p) {
      gen(16, p);
      p.parent.right = l;
    } else {
      gen(17, p);
      p.parent.left = l;
    }
    l.right = p;
    p.parent = l;
  }

  /** From CLR */
  private void fixAfterInsertion(Entry x) {
    x.color = RED;

    while (x != null && x != root && x.parent.color == RED) {
      if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
        Entry y = rightOf(parentOf(parentOf(x)));
        if (colorOf(y) == RED) {
          gen(18, x);
          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        } else {
          if (x == rightOf(parentOf(x))) {
            gen(19, x);
            x = parentOf(x);
            rotateLeft(x);
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          if (parentOf(parentOf(x)) != null) {
            gen(20, x);
            rotateRight(parentOf(parentOf(x)));
          }
        }
      } else {
        Entry y = leftOf(parentOf(parentOf(x)));
        if (colorOf(y) == RED) {
          gen(21, x);
          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        } else {
          if (x == leftOf(parentOf(x))) {
            gen(22, x);
            x = parentOf(x);
            rotateRight(x);
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          if (parentOf(parentOf(x)) != null) {
            gen(23, x);
            rotateLeft(parentOf(parentOf(x)));
          }
        }
      }
    }
    root.color = BLACK;
  }

  private void deleteEntry(Entry p) {
    decrementSize();

    // If strictly internal, first swap position with successor.
    if (p.left != null && p.right != null) {
      gen(24, p);
      Entry s = successor(p);
      swapPosition(s, p);
    }

    // Start fixup at replacement node, if it exists.
    Entry replacement = (p.left != null ? p.left : p.right);

    if (replacement != null) {
      // Link replacement to parent
      replacement.parent = p.parent;
      if (p.parent == null) {
        gen(25, p);
        root = replacement;
      } else if (p == p.parent.left) {
        gen(26, p);
        p.parent.left = replacement;
      } else {
        gen(27, p);
        p.parent.right = replacement;
      }

      // Null out links so they are OK to use by fixAfterDeletion.
      p.left = p.right = p.parent = null;

      // Fix replacement
      if (p.color == BLACK) {
        gen(28, p);
        fixAfterDeletion(replacement);
      } // MISSING else { test case .. not reachable anyway}
    } else if (p.parent == null) { // return if we are the only node.
      gen(29, p);
      root = null;
    } else { //  No children. Use self as phantom replacement and unlink.
      if (p.color == BLACK) {
        gen(30, p);
        fixAfterDeletion(p);
      }

      if (p.parent != null) {
        gen(31, p);
        if (p == p.parent.left) {
          gen(32, p);
          p.parent.left = null;
        } else if (p == p.parent.right) {
          gen(33, p);
          p.parent.right = null;
        } // MISSING else {test... not reachable}
        p.parent = null;
      } // MISSING else ...
    }
  }

  /** From CLR */
  private void fixAfterDeletion(Entry x) {
    while (x != root && colorOf(x) == BLACK) {
      if (x == leftOf(parentOf(x))) {
        Entry sib = rightOf(parentOf(x));

        if (colorOf(sib) == RED) {
          //assert false;
          gen(34, x);
          setColor(sib, BLACK);
          setColor(parentOf(x), RED);
          rotateLeft(parentOf(x));
          sib = rightOf(parentOf(x));
        }

        if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK) {
          gen(35, x);
          setColor(sib, RED);
          x = parentOf(x);
        } else {
          if (colorOf(rightOf(sib)) == BLACK) {
            gen(36, x);
            setColor(leftOf(sib), BLACK);
            setColor(sib, RED);
            rotateRight(sib);
            sib = rightOf(parentOf(x));
          }
          setColor(sib, colorOf(parentOf(x)));
          setColor(parentOf(x), BLACK);
          setColor(rightOf(sib), BLACK);
          rotateLeft(parentOf(x));
          x = root;
        }
      } else { // symmetric
        Entry sib = leftOf(parentOf(x));

        if (colorOf(sib) == RED) {
          gen(37, x);
          setColor(sib, BLACK);
          setColor(parentOf(x), RED);
          rotateRight(parentOf(x));
          sib = leftOf(parentOf(x));
        }

        if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK) {
          gen(38, x);
          setColor(sib, RED);
          x = parentOf(x);
        } else {
          if (colorOf(leftOf(sib)) == BLACK) {
            gen(39, x);
            setColor(rightOf(sib), BLACK);
            setColor(sib, RED);
            rotateLeft(sib);
            sib = leftOf(parentOf(x));
          }
          setColor(sib, colorOf(parentOf(x)));
          setColor(parentOf(x), BLACK);
          setColor(leftOf(sib), BLACK);
          rotateRight(parentOf(x));
          x = root;
        }
      }
    }

    setColor(x, BLACK);
  }

  /**
   * Swap the linkages of two nodes in a tree.
   */
  private void swapPosition(Entry x, Entry y) {
    // Save initial values.
    Entry px = x.parent, lx = x.left, rx = x.right;
    Entry py = y.parent, ly = y.left, ry = y.right;
    boolean xWasLeftChild = px != null && x == px.left;
    boolean yWasLeftChild = py != null && y == py.left;

    //    System.out.println("Swap: "+x.key+" "+y.key);
    // Swap, handling special cases of one being the other's parent.
    if (x == py) { // x was y's parent
      x.parent = y;
      if (yWasLeftChild) {
        gen(40, x);
        y.left = x;
        y.right = rx;
      } else {
        gen(41, x);
        y.right = x;
        y.left = lx;
      }
    } else {
      x.parent = py;
      if (py != null) {
        if (yWasLeftChild) {
          gen(42, x);
          py.left = x;
        } else {
          gen(43, x);
          py.right = x;
        }
      }
      y.left = lx;
      y.right = rx;
    }

    if (y == px) { // y was x's parent
      y.parent = x;
      if (xWasLeftChild) {
        gen(44, x);
        x.left = y;
        x.right = ry;
      } else {
        gen(45, x);
        x.right = y;
        x.left = ly;
      }
    } else {
      y.parent = px;
      if (px != null) {
        if (xWasLeftChild) {
          gen(46, x);
          px.left = y;
        } else {
          gen(47, x);
          px.right = y;
        }
      }
      x.left = ly;
      x.right = ry;
    }

    // Fix children's parent pointers
    if (x.left != null) {
      gen(48, x);
      x.left.parent = x;
    }
    if (x.right != null) {
      gen(49, x);
      x.right.parent = x;
    }
    if (y.left != null) {
      gen(50, x);
      y.left.parent = y;
    }
    if (y.right != null) {
      gen(51, x);
      y.right.parent = y;
    }

    // Swap colors
    boolean c = x.color;
    x.color = y.color;
    y.color = c;

    // Check if root changed
    if (root == x) {
      gen(52, x);
      root = y;
    } else if (root == y) {
      gen(53, x);
      root = x;
    }
  }
}
