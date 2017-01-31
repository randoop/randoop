package randoop.util;

import java.util.HashMap;

public class WeightedBalancedTree<T> implements WeightedRandomSampler<T> {

  private HashMap<T, Node<T>> currentElements;
  private Node<T> root;
  private Node<T> currentParent;
  private Node<T> prevChild;
  private Node<T> furthestLeftChild;

  public WeightedBalancedTree() {
    currentElements = new HashMap<>();
    root = null;
  }

  @Override
  public WeightedElement<T> getRandomElement() {
    if (root == null) {
      return null;
    }
    Node<T> node = root;
    while (node.left != null && node.right != null) {
      Node<T> randomChoice = getRandomNode(node);
      if (randomChoice == node) {
        return node.data;
      }
      node = randomChoice;
    }
    return node.data;
  }

  // TODO test this
  private Node<T> getRandomNode(Node<T> node) {
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
  public void add(WeightedElement<T> weightedElement) {
    add(weightedElement, weightedElement.getWeight());
  }

  // TODO test this.
  public void add(WeightedElement<T> weightedElement, double weight) {
    // This method is going to be tricky, hope this works out :)
    if (currentElements.containsKey(weightedElement)) {
      throw new IllegalArgumentException("Cannot add an element already in the Tree");
    }
    Node<T> n = new Node<T>(weightedElement);
    if (root == null) {
      root = n;
      furthestLeftChild = n;
      currentParent = n;
    } else if (currentParent.left == null) {
      // I think this will only be hit for the second call?
      currentParent.left = new Edge<T>(currentParent, n, n.data.getWeight());
      furthestLeftChild = currentParent.left.child;
    } else if (currentParent.right == null) {
      currentParent.right = new Edge<T>(currentParent, n, n.data.getWeight());
      prevChild.adj = currentParent.right.child;
    } else {
      if (currentParent.adj == null) {
        // go to the furthest left element
        currentParent = furthestLeftChild;
        currentParent.left = new Edge<T>(currentParent, n, n.data.getWeight());
        furthestLeftChild = currentParent.left.child;
      } else {
        currentParent = currentParent.adj;
        currentParent.left = new Edge<T>(currentParent, n, n.data.getWeight());
        prevChild.adj = currentParent.left.child;
      }
    }
    prevChild = n;

    Node<T> traversal = n;
    while (traversal.parent != null) {
      traversal.parent.weight += n.data.getWeight();
    }
    currentElements.put(weightedElement.getData(), n);
  }

  public void update(T sequence, double newWeight) {
    if (!currentElements.containsKey(sequence)) {
      throw new IllegalArgumentException("Object is not in set of nodes");
    }
    Node<T> node = currentElements.get(sequence);
    double prevWeight = node.data.getWeight();
    double diff = newWeight - prevWeight;
    node.data.setWeight(newWeight);
    Node<T> traversal = node;
    while (traversal.parent != null) {
      traversal.parent.weight += diff;
    }
  }

  private static class Node<T> {
    public Edge<T> parent;
    public Edge<T> left;
    public Edge<T> right;
    public Node<T> adj; // represents a right pointer to the adjacent node.

    public WeightedElement<T> data;

    public Node(WeightedElement<T> data) {
      this.data = data;
    }
  }

  private static class Edge<T> {
    public Node<T> parent;
    public Node<T> child;
    public double weight;

    public Edge(Node<T> parent, Node<T> child, double weight) {
      this.parent = parent;
      this.child = child;
      this.weight = weight;
    }
  }
}
