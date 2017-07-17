package randoop.test.symexamples;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class TreeMap extends AbstractMap implements SortedMap {
  private Comparator comparator = null;

  private transient Entry root = null;

  private transient int size = 0;

  private void incrementSize() {
    size++;
  }

  private void decrementSize() {
    size--;
  }

  public TreeMap() {}

  private Entry getEntry(Integer key) {
    Entry p = root;
    while ((((p != null) && ++randoopCoverageInfo.branchTrue[2] != 0)
        || ++randoopCoverageInfo.branchFalse[2] == 0)) {
      if ((((key.compareTo(p.key) == 0) && ++randoopCoverageInfo.branchTrue[1] != 0)
          || ++randoopCoverageInfo.branchFalse[1] == 0)) return p;
      else if ((((key.compareTo(p.key) < 0) && ++randoopCoverageInfo.branchTrue[0] != 0)
          || ++randoopCoverageInfo.branchFalse[0] == 0)) p = p.left;
      else p = p.right;
    }
    return null;
  }

  public Object put(Integer key, Object value) {
    Entry t = root;
    if ((((t == null) && ++randoopCoverageInfo.branchTrue[3] != 0)
        || ++randoopCoverageInfo.branchFalse[3] == 0)) {
      incrementSize();
      root = new Entry(key, value, null);
      return null;
    }
    while (true) {
      if ((((key.compareTo(t.key) == 0) && ++randoopCoverageInfo.branchTrue[7] != 0)
          || ++randoopCoverageInfo.branchFalse[7] == 0)) {
        return t.setValue(value);
      } else if ((((key.compareTo(t.key) < 0) && ++randoopCoverageInfo.branchTrue[6] != 0)
          || ++randoopCoverageInfo.branchFalse[6] == 0)) {
        if ((((t.left != null) && ++randoopCoverageInfo.branchTrue[4] != 0)
            || ++randoopCoverageInfo.branchFalse[4] == 0)) {
          t = t.left;
        } else {
          incrementSize();
          t.left = new Entry(key, value, t);
          fixAfterInsertion(t.left);
          return null;
        }
      } else {
        if ((((t.right != null) && ++randoopCoverageInfo.branchTrue[5] != 0)
            || ++randoopCoverageInfo.branchFalse[5] == 0)) {
          t = t.right;
        } else {
          incrementSize();
          t.right = new Entry(key, value, t);
          fixAfterInsertion(t.right);
          return null;
        }
      }
    }
  }

  public Object remove(Integer key) {
    Entry p = getEntry(key);
    if ((((p == null) && ++randoopCoverageInfo.branchTrue[8] != 0)
        || ++randoopCoverageInfo.branchFalse[8] == 0)) {
      return null;
    }
    Object oldVariable = p.value;
    deleteEntry(p);
    return oldVariable;
  }

  private transient Set keySet = null;

  private transient Set entrySet = null;

  private transient Collection values = null;

  private static final int KEYS = 0;

  private static final int VALUES = 1;

  private static final int ENTRIES = 2;

  private static final boolean RED = false;

  private static final boolean BLACK = true;

  public static class Entry implements Map.Entry {
    Integer key;

    Object value;

    Entry left = null;

    Entry right = null;

    Entry parent;

    boolean color = BLACK;

    Entry(Integer key, Object value, Entry parent) {
      this.key = key;
      this.value = value;
      this.parent = parent;
    }

    public Object getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }

    public Object setValue(Object value) {
      Object oldVariable = this.value;
      this.value = value;
      return oldVariable;
    }

    private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

    static {
      java.util.Map<String, java.util.Set<Integer>> methodToIndices =
          new java.util.LinkedHashMap<>();
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(0);
        indexList.add(1);
        indexList.add(2);
        methodToIndices.put(" Entry getEntry(Integer key) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(3);
        indexList.add(4);
        indexList.add(5);
        indexList.add(6);
        indexList.add(7);
        methodToIndices.put(" Object put(Integer key, Object value) ", indexList);
      }
      {
        java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
        indexList.add(8);
        methodToIndices.put(" Object remove(Integer key) ", indexList);
      }
      randoopCoverageInfo = new randoop.util.TestCoverageInfo(0, methodToIndices);
    }
  }

  private Entry successor(Entry t) {
    if ((((t == null) && ++randoopCoverageInfo.branchTrue[12] != 0)
        || ++randoopCoverageInfo.branchFalse[12] == 0)) return null;
    else if ((((t.right != null) && ++randoopCoverageInfo.branchTrue[11] != 0)
        || ++randoopCoverageInfo.branchFalse[11] == 0)) {
      Entry p = t.right;
      while ((((p.left != null) && ++randoopCoverageInfo.branchTrue[9] != 0)
          || ++randoopCoverageInfo.branchFalse[9] == 0)) p = p.left;
      return p;
    } else {
      Entry p = t.parent;
      Entry ch = t;
      while ((((p != null && ch == p.right) && ++randoopCoverageInfo.branchTrue[10] != 0)
          || ++randoopCoverageInfo.branchFalse[10] == 0)) {
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
    if ((((p != null) && ++randoopCoverageInfo.branchTrue[13] != 0)
        || ++randoopCoverageInfo.branchFalse[13] == 0)) p.color = c;
  }

  private static Entry leftOf(Entry p) {
    return (p == null) ? null : p.left;
  }

  private static Entry rightOf(Entry p) {
    return (p == null) ? null : p.right;
  }

  private void rotateLeft(Entry p) {
    Entry r = p.right;
    p.right = r.left;
    if ((((r.left != null) && ++randoopCoverageInfo.branchTrue[14] != 0)
        || ++randoopCoverageInfo.branchFalse[14] == 0)) r.left.parent = p;
    r.parent = p.parent;
    if ((((p.parent == null) && ++randoopCoverageInfo.branchTrue[16] != 0)
        || ++randoopCoverageInfo.branchFalse[16] == 0)) root = r;
    else if ((((p.parent.left == p) && ++randoopCoverageInfo.branchTrue[15] != 0)
        || ++randoopCoverageInfo.branchFalse[15] == 0)) p.parent.left = r;
    else p.parent.right = r;
    r.left = p;
    p.parent = r;
  }

  private void rotateRight(Entry p) {
    Entry l = p.left;
    p.left = l.right;
    if ((((l.right != null) && ++randoopCoverageInfo.branchTrue[17] != 0)
        || ++randoopCoverageInfo.branchFalse[17] == 0)) l.right.parent = p;
    l.parent = p.parent;
    if ((((p.parent == null) && ++randoopCoverageInfo.branchTrue[19] != 0)
        || ++randoopCoverageInfo.branchFalse[19] == 0)) root = l;
    else if ((((p.parent.right == p) && ++randoopCoverageInfo.branchTrue[18] != 0)
        || ++randoopCoverageInfo.branchFalse[18] == 0)) p.parent.right = l;
    else p.parent.left = l;
    l.right = p;
    p.parent = l;
  }

  private void fixAfterInsertion(Entry x) {
    x.color = RED;
    while ((((x != null && x != root && x.parent.color == RED)
            && ++randoopCoverageInfo.branchTrue[27] != 0)
        || ++randoopCoverageInfo.branchFalse[27] == 0)) {
      if ((((parentOf(x) == leftOf(parentOf(parentOf(x))))
              && ++randoopCoverageInfo.branchTrue[26] != 0)
          || ++randoopCoverageInfo.branchFalse[26] == 0)) {
        Entry y = rightOf(parentOf(parentOf(x)));
        if ((((colorOf(y) == RED) && ++randoopCoverageInfo.branchTrue[22] != 0)
            || ++randoopCoverageInfo.branchFalse[22] == 0)) {
          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        } else {
          if ((((x == rightOf(parentOf(x))) && ++randoopCoverageInfo.branchTrue[20] != 0)
              || ++randoopCoverageInfo.branchFalse[20] == 0)) {
            x = parentOf(x);
            rotateLeft(x);
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          if ((((parentOf(parentOf(x)) != null) && ++randoopCoverageInfo.branchTrue[21] != 0)
              || ++randoopCoverageInfo.branchFalse[21] == 0)) rotateRight(parentOf(parentOf(x)));
        }
      } else {
        Entry y = leftOf(parentOf(parentOf(x)));
        if ((((colorOf(y) == RED) && ++randoopCoverageInfo.branchTrue[25] != 0)
            || ++randoopCoverageInfo.branchFalse[25] == 0)) {
          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        } else {
          if ((((x == leftOf(parentOf(x))) && ++randoopCoverageInfo.branchTrue[23] != 0)
              || ++randoopCoverageInfo.branchFalse[23] == 0)) {
            x = parentOf(x);
            rotateRight(x);
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          if ((((parentOf(parentOf(x)) != null) && ++randoopCoverageInfo.branchTrue[24] != 0)
              || ++randoopCoverageInfo.branchFalse[24] == 0)) rotateLeft(parentOf(parentOf(x)));
        }
      }
    }
    root.color = BLACK;
  }

  private void deleteEntry(Entry p) {
    decrementSize();
    if ((((p.left != null && p.right != null) && ++randoopCoverageInfo.branchTrue[28] != 0)
        || ++randoopCoverageInfo.branchFalse[28] == 0)) {
      Entry s = successor(p);
      swapPosition(s, p);
    }
    Entry replacement = (p.left != null ? p.left : p.right);
    if ((((replacement != null) && ++randoopCoverageInfo.branchTrue[37] != 0)
        || ++randoopCoverageInfo.branchFalse[37] == 0)) {
      replacement.parent = p.parent;
      if ((((p.parent == null) && ++randoopCoverageInfo.branchTrue[30] != 0)
          || ++randoopCoverageInfo.branchFalse[30] == 0)) root = replacement;
      else if ((((p == p.parent.left) && ++randoopCoverageInfo.branchTrue[29] != 0)
          || ++randoopCoverageInfo.branchFalse[29] == 0)) p.parent.left = replacement;
      else p.parent.right = replacement;
      p.left = p.right = p.parent = null;
      if ((((p.color == BLACK) && ++randoopCoverageInfo.branchTrue[31] != 0)
          || ++randoopCoverageInfo.branchFalse[31] == 0)) fixAfterDeletion(replacement);
    } else if ((((p.parent == null) && ++randoopCoverageInfo.branchTrue[36] != 0)
        || ++randoopCoverageInfo.branchFalse[36] == 0)) {
      root = null;
    } else {
      if ((((p.color == BLACK) && ++randoopCoverageInfo.branchTrue[32] != 0)
          || ++randoopCoverageInfo.branchFalse[32] == 0)) fixAfterDeletion(p);
      if ((((p.parent != null) && ++randoopCoverageInfo.branchTrue[35] != 0)
          || ++randoopCoverageInfo.branchFalse[35] == 0)) {
        if ((((p == p.parent.left) && ++randoopCoverageInfo.branchTrue[34] != 0)
            || ++randoopCoverageInfo.branchFalse[34] == 0)) p.parent.left = null;
        else if ((((p == p.parent.right) && ++randoopCoverageInfo.branchTrue[33] != 0)
            || ++randoopCoverageInfo.branchFalse[33] == 0)) p.parent.right = null;
        p.parent = null;
      }
    }
  }

  private void fixAfterDeletion(Entry x) {
    while ((((x != root && colorOf(x) == BLACK) && ++randoopCoverageInfo.branchTrue[45] != 0)
        || ++randoopCoverageInfo.branchFalse[45] == 0)) {
      if ((((x == leftOf(parentOf(x))) && ++randoopCoverageInfo.branchTrue[44] != 0)
          || ++randoopCoverageInfo.branchFalse[44] == 0)) {
        Entry sib = rightOf(parentOf(x));
        if ((((colorOf(sib) == RED) && ++randoopCoverageInfo.branchTrue[38] != 0)
            || ++randoopCoverageInfo.branchFalse[38] == 0)) {
          setColor(sib, BLACK);
          setColor(parentOf(x), RED);
          rotateLeft(parentOf(x));
          sib = rightOf(parentOf(x));
        }
        if ((((colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK)
                && ++randoopCoverageInfo.branchTrue[40] != 0)
            || ++randoopCoverageInfo.branchFalse[40] == 0)) {
          setColor(sib, RED);
          x = parentOf(x);
        } else {
          if ((((colorOf(rightOf(sib)) == BLACK) && ++randoopCoverageInfo.branchTrue[39] != 0)
              || ++randoopCoverageInfo.branchFalse[39] == 0)) {
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
      } else {
        Entry sib = leftOf(parentOf(x));
        if ((((colorOf(sib) == RED) && ++randoopCoverageInfo.branchTrue[41] != 0)
            || ++randoopCoverageInfo.branchFalse[41] == 0)) {
          setColor(sib, BLACK);
          setColor(parentOf(x), RED);
          rotateRight(parentOf(x));
          sib = leftOf(parentOf(x));
        }
        if ((((colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK)
                && ++randoopCoverageInfo.branchTrue[43] != 0)
            || ++randoopCoverageInfo.branchFalse[43] == 0)) {
          setColor(sib, RED);
          x = parentOf(x);
        } else {
          if ((((colorOf(leftOf(sib)) == BLACK) && ++randoopCoverageInfo.branchTrue[42] != 0)
              || ++randoopCoverageInfo.branchFalse[42] == 0)) {
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

  private void swapPosition(Entry x, Entry y) {
    Entry px = x.parent, lx = x.left, rx = x.right;
    Entry py = y.parent, ly = y.left, ry = y.right;
    boolean xWasLeftChild = px != null && x == px.left;
    boolean yWasLeftChild = py != null && y == py.left;
    if ((((x == py) && ++randoopCoverageInfo.branchTrue[49] != 0)
        || ++randoopCoverageInfo.branchFalse[49] == 0)) {
      x.parent = y;
      if ((((yWasLeftChild) && ++randoopCoverageInfo.branchTrue[46] != 0)
          || ++randoopCoverageInfo.branchFalse[46] == 0)) {
        y.left = x;
        y.right = rx;
      } else {
        y.right = x;
        y.left = lx;
      }
    } else {
      x.parent = py;
      if ((((py != null) && ++randoopCoverageInfo.branchTrue[48] != 0)
          || ++randoopCoverageInfo.branchFalse[48] == 0)) {
        if ((((yWasLeftChild) && ++randoopCoverageInfo.branchTrue[47] != 0)
            || ++randoopCoverageInfo.branchFalse[47] == 0)) py.left = x;
        else py.right = x;
      }
      y.left = lx;
      y.right = rx;
    }
    if ((((y == px) && ++randoopCoverageInfo.branchTrue[53] != 0)
        || ++randoopCoverageInfo.branchFalse[53] == 0)) {
      y.parent = x;
      if ((((xWasLeftChild) && ++randoopCoverageInfo.branchTrue[50] != 0)
          || ++randoopCoverageInfo.branchFalse[50] == 0)) {
        x.left = y;
        x.right = ry;
      } else {
        x.right = y;
        x.left = ly;
      }
    } else {
      y.parent = px;
      if ((((px != null) && ++randoopCoverageInfo.branchTrue[52] != 0)
          || ++randoopCoverageInfo.branchFalse[52] == 0)) {
        if ((((xWasLeftChild) && ++randoopCoverageInfo.branchTrue[51] != 0)
            || ++randoopCoverageInfo.branchFalse[51] == 0)) px.left = y;
        else px.right = y;
      }
      x.left = ly;
      x.right = ry;
    }
    if ((((x.left != null) && ++randoopCoverageInfo.branchTrue[54] != 0)
        || ++randoopCoverageInfo.branchFalse[54] == 0)) x.left.parent = x;
    if ((((x.right != null) && ++randoopCoverageInfo.branchTrue[55] != 0)
        || ++randoopCoverageInfo.branchFalse[55] == 0)) x.right.parent = x;
    if ((((y.left != null) && ++randoopCoverageInfo.branchTrue[56] != 0)
        || ++randoopCoverageInfo.branchFalse[56] == 0)) y.left.parent = y;
    if ((((y.right != null) && ++randoopCoverageInfo.branchTrue[57] != 0)
        || ++randoopCoverageInfo.branchFalse[57] == 0)) y.right.parent = y;
    boolean c = x.color;
    x.color = y.color;
    y.color = c;
    if ((((root == x) && ++randoopCoverageInfo.branchTrue[59] != 0)
        || ++randoopCoverageInfo.branchFalse[59] == 0)) root = y;
    else if ((((root == y) && ++randoopCoverageInfo.branchTrue[58] != 0)
        || ++randoopCoverageInfo.branchFalse[58] == 0)) root = x;
  }

  private static randoop.util.TestCoverageInfo randoopCoverageInfo = null;

  static {
    java.util.Map<String, java.util.Set<Integer>> methodToIndices = new java.util.LinkedHashMap<>();
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(0);
      indexList.add(1);
      indexList.add(2);
      methodToIndices.put(" Entry getEntry(Integer key) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(3);
      indexList.add(4);
      indexList.add(5);
      indexList.add(6);
      indexList.add(7);
      methodToIndices.put(" Object put(Integer key, Object value) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(8);
      methodToIndices.put(" Object remove(Integer key) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(9);
      indexList.add(10);
      indexList.add(11);
      indexList.add(12);
      methodToIndices.put(" Entry successor(Entry t) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(13);
      methodToIndices.put(" void setColor(Entry p, boolean c) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(14);
      indexList.add(15);
      indexList.add(16);
      methodToIndices.put(" void rotateLeft(Entry p) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(17);
      indexList.add(18);
      indexList.add(19);
      methodToIndices.put(" void rotateRight(Entry p) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(20);
      indexList.add(21);
      indexList.add(22);
      indexList.add(23);
      indexList.add(24);
      indexList.add(25);
      indexList.add(26);
      indexList.add(27);
      methodToIndices.put(" void fixAfterInsertion(Entry x) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(28);
      indexList.add(29);
      indexList.add(30);
      indexList.add(31);
      indexList.add(32);
      indexList.add(33);
      indexList.add(34);
      indexList.add(35);
      indexList.add(36);
      indexList.add(37);
      methodToIndices.put(" void deleteEntry(Entry p) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(38);
      indexList.add(39);
      indexList.add(40);
      indexList.add(41);
      indexList.add(42);
      indexList.add(43);
      indexList.add(44);
      indexList.add(45);
      methodToIndices.put(" void fixAfterDeletion(Entry x) ", indexList);
    }
    {
      java.util.Set<Integer> indexList = new java.util.LinkedHashSet<>();
      indexList.add(46);
      indexList.add(47);
      indexList.add(48);
      indexList.add(49);
      indexList.add(50);
      indexList.add(51);
      indexList.add(52);
      indexList.add(53);
      indexList.add(54);
      indexList.add(55);
      indexList.add(56);
      indexList.add(57);
      indexList.add(58);
      indexList.add(59);
      methodToIndices.put(" void swapPosition(Entry x, Entry y) ", indexList);
    }
    randoopCoverageInfo = new randoop.util.TestCoverageInfo(60, methodToIndices);
  }
}
