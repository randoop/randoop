package randoop.reflection;

/** Inspired by org.apache.commons.math3.geometry.partitioning.utilities.AVLTree */
public class GenericTreeWithInnerNode<T extends Comparable<T>> {

  private Node root;

  public GenericTreeWithInnerNode() {
    root = null;
  }

  public void insert(final T element) {}

  public Node getTheOne() {
    return root;
  }

  public class Node {
    private T element;
    private Node left;
    // private Node right;

    Node(final T element) {
      this.element = element;
      left = null;
      // right = null;
    }

    boolean insert(final T newElement) {
      Node node = new Node(newElement);
      node.left = this.left;
      this.left = node;
      return true;
    }

    T getElement() {
      return this.element;
    }
  }
}
