package randoop.util;

import randoop.sequence.Sequence;

import java.util.HashMap;

public class WeightedBalancedTree implements WeightedRandomSampler {

  private HashMap<Sequence, Node> currentElements;
  private Node root;
  private Node currentParent;
  private Node prevChild;
  private Node furthestLeftChild;

  public WeightedBalancedTree() {
    currentElements = new HashMap<>();
    root = null;
  }

  @Override
  public Sequence getRandomElement() {
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
    double totalWeight = node.weight;
    if (node.left != null) {
      totalWeight += node.left.weight;
    }
    if (node.right != null) {
      totalWeight += node.right.weight;
    }

    double randomValue = totalWeight * Randomness.random.nextDouble();
    if (randomValue <= node.weight) {
      return node;
      // Don't need to consider the node.left == null and node.right == null case because
      // it would be guaranteed to be in this if statement
    } else if (node.left == null) {
      return node.right.child;
    } else if (node.right == null) {
      return node.left.child;
    } else {
      if (randomValue <= (node.left.weight + node.weight)) {
        return node.left.child;
      } else {
        return node.right.child;
      }
    }
  }

  @Override
  public void add(Sequence weightedElement) {
    add(weightedElement, weightedElement.getWeight());
  }

  // TODO test this.
  public void add(Sequence weightedElement, double weight) {
    // This method is going to be tricky, hope this works out :)
    if (currentElements.containsKey(weightedElement)) {
      throw new IllegalArgumentException("Cannot add an element already in the Tree");
    }
    Node n = new Node(weightedElement, weight);
    if (root == null) {
      root = n;
      furthestLeftChild = n;
      currentParent = n;
    } else if (currentParent.left == null) {
      // I think this will only be hit for the second call?
      currentParent.left = new Edge(currentParent, n, n.weight);
      furthestLeftChild = currentParent.left.child;
    } else if (currentParent.right == null) {
      currentParent.right = new Edge(currentParent, n, n.weight);
      prevChild.adj = currentParent.right.child;
    } else {
      if (currentParent.adj == null) {
        // go to the furthest left element
        currentParent = furthestLeftChild;
        currentParent.left = new Edge(currentParent, n, n.weight);
        furthestLeftChild = currentParent.left.child;
      } else {
        currentParent = currentParent.adj;
        currentParent.left = new Edge(currentParent, n, n.weight);
        prevChild.adj = currentParent.left.child;
      }
    }
    prevChild = n;

    Node traversal = n;
    while (traversal.parent != null) {
      traversal.parent.weight += n.weight;
    }
    currentElements.put(weightedElement, n);
  }

  private static class Node {
    public Edge parent;
    public Edge left;
    public Edge right;
    public Node adj; // represents a right pointer to the adjacent node.

    public Sequence data;
    public double weight;

    public Node(Sequence data, double weight) {
      this.data = data;
      this.weight = weight;
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
