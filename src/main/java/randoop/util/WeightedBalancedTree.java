package randoop.util;

import java.util.HashMap;

/**
 * Created by Justin on 1/28/2017.
 */
public class WeightedBalancedTree implements WeightedRandomSampler {

  private HashMap<WeightedElement, Node> currentElements;
  private Node root;
  private Node currentParent;
  private Node prevChild;
  private Node furthestLeftChild;

  public WeightedBalancedTree() {
    currentElements = new HashMap<>();
    root = null;
  }

  @Override
  public WeightedElement getRandomElement() {
    if (root == null) {
      return null;
    }
    Node node = root;
    while (node.left != null && node.right != null) {
      Node randomChoice = getRandomNode(node);
      if (randomChoice == node) {
        return node.data;
      }
      node = randomChoice;
    }
    return node.data;
  }

  // TODO test this
  private Node getRandomNode(Node node) {
    double totalWeight = node.data.getWeight();
    if (node.left != null) {
      totalWeight += node.left.weight;
    }
    if (node.right != null) {
      totalWeight += node.right.weight;
    }

    double randomValue = totalWeight * Randomness.random.nextDouble();
    if (randomValue <= node.data.getWeight()) {
      return node;
      // Don't need to consider the node.left == null and node.right == null case because
      // it would be guaranteed to be in this if statement
    } else if (node.left == null) {
      return node.right.child;
    } else if (node.right == null) {
      return node.left.child;
    } else {
      if (randomValue <= (node.left.weight + node.data.getWeight())) {
        return node.left.child;
      } else {
        return node.right.child;
      }
    }
  }

  @Override
  // TODO test this.
  public void add(WeightedElement weightedElement) {
    // This method is going to be tricky, hope this works out :)
    if (currentElements.containsKey(weightedElement)) {
      throw new IllegalArgumentException("Cannot add an element already in the Tree");
    }
    Node n = new Node(weightedElement);
    if (root == null) {
      root = n;
      furthestLeftChild = n;
      currentParent = n;
    } else if (currentParent.left == null) {
      // I think this will only be hit for the second call?
      currentParent.left = new Edge(currentParent, n, n.data.getWeight());
      furthestLeftChild = currentParent.left.child;
    } else if (currentParent.right == null) {
      currentParent.right = new Edge(currentParent, n, n.data.getWeight());
      prevChild.adj = currentParent.right.child;
    } else {
      if (currentParent.adj == null) {
        // go to the furthest left element
        currentParent = furthestLeftChild;
        currentParent.left = new Edge(currentParent, n, n.data.getWeight());
        furthestLeftChild = currentParent.left.child;
      } else {
        currentParent = currentParent.adj;
        currentParent.left = new Edge(currentParent, n, n.data.getWeight());
        prevChild.adj = currentParent.left.child;
      }
    }
    prevChild = n;
  
    Node traversal = n;
    while (traversal.parent != null) {
      traversal.parent.weight += n.data.getWeight();
    }
    currentElements.put(weightedElement, n);
  }

  private static class Node {
    public Edge parent;
    public Edge left;
    public Edge right;
    public Node adj; // represents a right pointer to the adjacent node.

    public WeightedElement data;
    public double weight;
    public Node(WeightedElement data) {
      this.data = data;
      weight = data.getWeight();
    }
  }

  private static class Edge {
    public Node parent;
    public Node child;
    public double weight;

    public Edge(Node parent, Node child, double weight) {
      this.parent = parent;
      this.child = child;
      this.weight = weight;
    }
  }
}
