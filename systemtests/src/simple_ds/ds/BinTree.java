package ds;

import java.util.*;

public class BinTree {

  private  BTNode root;

  public BinTree() {
    root = null;
  }

  public void add(int x) {
    BTNode current = root;

    if (root == null) {
      root = new  BTNode(x);
      return;
    }

    while (current.value != x) {
      if (x < current.value) {
	if (current.left == null) {
	  current.left = new  BTNode(x);
	} else {
	  current = current.left;
	}
      } else {
	if (current.right == null) {
	  current.right = new  BTNode(x);
	} else {
	  current = current.right;
	}
      }
    }
  }

  public boolean find(int x) {
    BTNode current = root;

    while (current != null) {

      if (current.value == x) {
	return true;
      }

      if (x < current.value) {
	current = current.left;
      } else {
	current = current.right;
      }
    }

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
	    bigson = bigson.right;
	    branch = false;
	  } else {
	    bigson = bigson.left;
	    branch = true;
	  }
	}

	if (parent != null) {
	  if (branch) {
	    parent.left = null;
	  } else {
	    parent.right = null;
	  }
	}

	if (bigson != current) {
	  current.value = bigson.value;
	} else {
	}

	return true;
      }

      parent = current;
      if (current.value > x) {
	current = current.left;
	branch = true;
      } else {
	current = current.right;
	branch = false;
      }
    }

    return false;
  }
}
