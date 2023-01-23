package randoop.test.issta2006;

import java.util.HashSet;
import java.util.Set;

class BTNode {

  public int value;

  public BTNode left, right;

  public BTNode(int x) {
    value = x;
    left = null;
    right = null;
  }
}

public class BinTree {

  private static long startTime = System.currentTimeMillis();

  private BTNode root;

  public BinTree() {
    root = null;
  }

  //----
  private static void outputTestSequence(int number) {}

  public static Set<String> branchFingerprints = new HashSet<>();

  // private static Set abs_states = new HashSet();

  public static int counter = 0;

  public static int gen_native(int br, BTNode n0, int x, BTNode n1, BTNode n2) {
    String res = br + ",";
    //For Basic Block Coverage
    //START comment here

    BTNode temp;
    if (n0 == null) {
      res += "-";
    } else {
      // commented out because of symbolic version
      //        temp = env.getIntField(n0,null, "value");
      //        if (temp < x) res+= "<";
      //        if (temp == x) res+= "=";
      //        if (temp > x) res+= ">";
      temp = n0.left;
      res += (temp == null) ? "L-" : "L+";
      temp = n0.right;
      res += (temp == null) ? "R-" : "R+";
    }
    res += (n1 == null) ? "P-" : "P+";
    if (n2 == null) {
      res += "B-";
    } else {
      temp = n2.left;
      res += (temp == null) ? "BL-" : "BL+";
      temp = n2.right;
      res += (temp == null) ? "BR-" : "BR+";
    }
    //End comment here

    if (!branchFingerprints.contains(res)) {
      branchFingerprints.add(res);
      System.out.println("Test case number " + branchFingerprints.size() + " for '" + res + "': ");
      // System.out.println("TIME=" + (System.currentTimeMillis() - startTime));
      counter = branchFingerprints.size();
      return branchFingerprints.size();
    }
    return 0;
  }

  private static void gen(int br, BTNode n0, int x, BTNode n1, BTNode n2) {
    int c = gen_native(br, n0, x, n1, n2);
    if (c != 0) outputTestSequence(c);
  }

  //----

  public void add(int x) {
    BTNode current = root;

    if (root == null) {
      gen(0, current, x, null, null);
      root = new BTNode(x);
      return;
    }

    while (current.value != x) {
      if (x < current.value) {
        if (current.left == null) {
          gen(1, current, x, null, null);
          current.left = new BTNode(x);
        } else {
          gen(2, current, x, null, null);
          current = current.left;
        }
      } else {
        if (current.right == null) {
          gen(3, current, x, null, null);
          current.right = new BTNode(x);
        } else {
          gen(4, current, x, null, null);
          current = current.right;
        }
      }
    }
  }

  public boolean find(int x) {
    BTNode current = root;

    while (current != null) {

      if (current.value == x) {
        gen(5, current, x, null, null);
        return true;
      }

      if (x < current.value) {
        gen(6, current, x, null, null);
        current = current.left;
      } else {
        gen(7, current, x, null, null);
        current = current.right;
      }
    }
    gen(16, current, x, null, null);

    return false;
  }

  public boolean remove(int x) {
    BTNode current = root;
    BTNode parent = null;
    boolean branch = true; //true =left, false =right

    while (current != null) {

      if (current.value == x) {
        BTNode bigson = current;
        while (bigson.left != null || bigson.right != null) {
          parent = bigson;
          if (bigson.right != null) {
            gen(8, current, x, bigson, parent);
            bigson = bigson.right;
            branch = false;
          } else {
            gen(9, current, x, bigson, parent);
            bigson = bigson.left;
            branch = true;
          }
        }

        //        System.out.println("Remove: current "+current.value+" parent "+parent.value+" bigson "+bigson.value);
        if (parent != null) {
          if (branch) {
            gen(10, current, x, bigson, parent);
            parent.left = null;
          } else {
            gen(11, current, x, bigson, parent);
            parent.right = null;
          }
        }

        if (bigson != current) {
          gen(12, current, x, bigson, parent);
          current.value = bigson.value;
        } else {
          gen(13, current, x, bigson, parent);
        }

        return true;
      }

      parent = current;
      //        if (current.value <x ) { // THERE WAS ERROR
      if (current.value > x) {
        gen(14, current, x, null, parent);
        current = current.left;
        branch = true;
      } else {
        gen(15, current, x, null, parent);
        current = current.right;
        branch = false;
      }
    }

    gen(17, current, x, null, parent);
    return false;
  }
}
