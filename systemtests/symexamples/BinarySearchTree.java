package symexamples;
import java.util.*;

public class BinarySearchTree {
    private Node root; // root node
    private int size;  // number of nodes in the tree
    
    public static class Node {
        private  Node left;  // left child
        private  Node right; // right child
        private  Comparable info; // data
            
        Node(Node left, Node right, Comparable info){
            this.left = left;
            this.right = right;
            this.info = info;
        }
        Node(Comparable info){
            this.info = info;
        }
        /*
        public String toString() {
            Set visited = new LinkedHashSet();
            visited.add(this);
            return toString(visited);
        }
        private String toString(Set visited) {
            StringBuffer buf = new StringBuffer();
            // buf.append(" ");
            // buf.append(System.identityHashCode(this));
            buf.append(" {");
            if (left != null)
                if (visited.add(left))
                    buf.append(left.toString(visited));
                else
                    buf.append("!tree");

            buf.append("" + this.info + "");

            if (right != null)
                if (visited.add(right))
                    buf.append(right.toString(visited));
                else
                    buf.append("!tree");
            buf.append("} ");
            return buf.toString();
        }

        public boolean equals(Object that){
            if (!(that instanceof Node))
                return false;
            Node n = (Node)that;
            if (this.info.compareTo(n.info) != 0)
                return false;
            boolean b = true;
            if (left == null)
                b = b && (n.left == null);
            else
                b = b && (left.equals(n.left));
            if (right == null)
                b = b && (n.right == null);
            else 
                b = b && (right.equals(n.right));
            return b;
        }
        */
    }

    
    public boolean remove(Comparable info) {     
        Node parent = null;
        Node current = root;
        while (current != null) {
            int cmp = info.compareTo(current.info);
            if (cmp < 0) {
                parent = current;
                current = current.left;
            } else if (cmp > 0) {
                parent = current;
                current = current.right;
            } else {
                break;
            }
        }
        if (current == null) return false;
        Node change = removeNode(current);
        if (parent == null) {
            root = change;
        } else if (parent.left == current) {
            parent.left = change;
        } else {
            parent.right = change;
        }
        return true;
    }

    Node removeNode(Node current) {
        size--;
        Node left = current.left, right = current.right;
        if (left == null) return right;
        if (right == null) return left;
        if (left.right == null) {
            current.info = left.info;
            current.left = left.left;
            return current;
        }
        Node temp = left;
        while (temp.right.right != null) {
            temp = temp.right;
        }
        current.info = temp.right.info;
        temp.right = temp.right.left;
        return current;
    }
    
    /*
    static final class NodeWrapper{
        BinarySearchTree.Node node;
        NodeWrapper(BinarySearchTree.Node n){
            this.node = n;
        }
        public boolean equals(Object o){
            if (!(o instanceof NodeWrapper)) return false;
            return node == ((NodeWrapper)o).node;
        }
        public int hashCode(){
            return System.identityHashCode(node);
        }
    }
    
    
    public boolean repOk() {
        // checks that empty tree has size zero
        if (root == null) return size == 0;
        // checks that the input is a tree
        if (!isAcyclic()) return false;
        // checks that size is consistent
        if (numNodes(root) != size) return false;
        // checks that data is ordered
        if (!isOrdered(root)) return false;
        return true;
    }

    private boolean isAcyclic() {
        Set visited = new LinkedHashSet();
        visited.add(new NodeWrapper(root));
        java.util.LinkedList workList = new java.util.LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            Node current = (Node)workList.removeFirst();
            if (current.left != null) {
                // checks that the tree has no cycle
                if (!visited.add(new NodeWrapper(current.left))) 
                    return false;
                workList.add(current.left);
            }
            if (current.right != null) {
                // checks that the tree has no cycle
                if (!visited.add(new NodeWrapper(current.right))) 
                    return false;
                workList.add(current.right);
            }
        }
        return true;
    }

    private int numNodes(Node n) {
        if (n == null) return 0;
        return 1 + numNodes(n.left) + numNodes(n.right);
    }

    private boolean isOrdered(Node n) {
        return isOrdered(n, null, null);
    }

    private boolean isOrdered(Node n, Comparable min, Comparable max) {
        if (n.info == null) return false;
        if ((min != null && n.info.compareTo(min) <= 0) ||
            (max != null && n.info.compareTo(max) >= 0))
            return false;
        if (n.left != null)
            if (!isOrdered(n.left, min, n.info))
                return false;
        if (n.right != null)
            if (!isOrdered(n.right, n.info, max))
                return false;
        return true;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        if (root != null) 
            buf.append(root.toString());
        buf.append("}");
        return buf.toString();
    }
    */

//      /*@ public normal_behavior      // specification for remove:
//        @     requires repOk(); //     precondition
//        @     ensures repOk() && !contains(info) &&
//        @             \result == \old(contains(info)); //     postcondition
//        @*/
//      public boolean remove(Comparable info) {
//          /********/
//          
//          root = findAndRemove(root, info);
//          
//      }

//      protected Node findAndRemove(Node current, Comparable info){
//          if (current == null) return null;
//          int comparison = current.info.compareTo(info);
//          if (comparison == 0)
//              return removeRoot(current);
//          else if (comparison < 0) // current.info < info
//              current.right = findAndRemove(current.right, info);
//          else // info < current.info
//              current.left = findAndRemove(current.left, info);
//          return current;
//      }

//      protected Node removeRoot(Node current){
//          size--;
//          Node left = current.left, right = current.right;
//          if ((left == null) && (right == null))
//              return null;
//          if (left == null)
//              return right;
//          if (right == null)
//              return left;
//          if (left.right == null){
//              current.info = left.info;
//              current.left = left.left;
//              return current;
//          }
//          Node q = left;
//          while (q.right.right != null)
//              q = q.right;
//          current.info = q.right.info;
//          q.right = q.right.left;
//          return current;
//      }

    /*@ public normal_behavior
      @     requires repOk(); 
      @     ensures repOk() && contains(info);
      @*/
    public void add(Comparable info) {
        if (root == null) {
            root = new Node(info);
        } else {
            Node t = root;
            while (true) {
                if (t.info.compareTo(info) < 0) {
                    if (t.right == null) {
                        t.right = new Node(info);
                        break;
                    } else {
                        t = t.right;
                    }
                } else if (t.info.compareTo(info) > 0) {
                    if (t.left == null) {
                        t.left = new Node(info);
                        break;
                    } else {
                        t = t.left;
                    }
                } else { // no duplicates
                    return;
                }
            }
        }
        size++;
    }

    /*
    //@ requires repOk();
    public boolean contains(Node n) {
        if (root == null) return false;
        java.util.LinkedList workList = new java.util.LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            Node current = (Node)workList.removeFirst();
            if (current == n) return true;
            if (current.left != null)
                workList.add(current.left);
            if (current.right != null)
                workList.add(current.right);
        }
        return false;
    }

    //@ requires repOk();
    public boolean contains(Comparable info) {
        if (root == null) return false;
        java.util.LinkedList workList = new java.util.LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            Node current = (Node)workList.removeFirst();
            if (current.info.compareTo(info) == 0) return true;
            if (current.left != null)
                workList.add(current.left);
            if (current.right != null)
                workList.add(current.right);
        }
        return false;
    }

    public boolean equals(Object that){
        if (!(that instanceof BinarySearchTree))
            return false;
        BinarySearchTree b = (BinarySearchTree)that;
        if (size != b.size)
            return false;
        if (((root == null) && (b.root != null)) || 
            ((root != null) && (b.root == null)))
            return false;
        if ((root == null) && (b.root == null))
            return true;
        return root.equals(b.root);
    }
    */
}
