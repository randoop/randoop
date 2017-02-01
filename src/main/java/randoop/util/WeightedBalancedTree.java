package randoop.util;

import java.util.HashMap;

public class WeightedBalancedTree<T> implements WeightedRandomSampler<T> {

  // TODO may be issues with using T in hashmap, consider making WeightedElement
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
    while (node.leftEdge != null && node.rightEdge != null) {
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
    if (node.leftEdge != null) {
      totalWeight += node.leftEdge.weight;
    }
    if (node.rightEdge != null) {
      totalWeight += node.rightEdge.weight;
    }

    double randomValue = totalWeight * Randomness.random.nextDouble();
    if (randomValue <= node.data.getWeight()) {
      return node;
      // Don't need to consider the node.leftEdge == null and node.rightEdge == null case because
      // it would be guaranteed to be in this if statement
    } else if (node.leftEdge == null) {
      return node.rightEdge.child;
    } else if (node.rightEdge == null) {
      return node.leftEdge.child;
    } else {
      if (randomValue <= (node.leftEdge.weight + node.data.getWeight())) {
        return node.leftEdge.child;
      } else {
        return node.rightEdge.child;
      }
    }
  }

  @Override
  public void add(WeightedElement<T> weightedElement) {
    add(weightedElement, weightedElement.getWeight());
  }

  @Override
  public void add(T data, double weight) {
    add(new WeightedElement<T>(data, weight));
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
    } else if (currentParent.leftEdge == null) {
      // I think this will only be hit for the second call?
      currentParent.leftEdge = new Edge<T>(currentParent, n, n.data.getWeight());
      furthestLeftChild = currentParent.leftEdge.child;
      n.parentEdge = currentParent.leftEdge;
    } else if (currentParent.rightEdge == null) {
      currentParent.rightEdge = new Edge<T>(currentParent, n, n.data.getWeight());
      prevChild.adj = currentParent.rightEdge.child;
      n.parentEdge = currentParent.rightEdge;
    } else {
      if (currentParent.adj == null) {
        // go to the furthest leftEdge element
        currentParent = furthestLeftChild;
        currentParent.leftEdge = new Edge<T>(currentParent, n, n.data.getWeight());
        furthestLeftChild = currentParent.leftEdge.child;
        n.parentEdge = currentParent.leftEdge;
      } else {
        currentParent = currentParent.adj;
        currentParent.leftEdge = new Edge<T>(currentParent, n, n.data.getWeight());
        prevChild.adj = currentParent.leftEdge.child;
        n.parentEdge = currentParent.leftEdge;
      }
    }
    prevChild = n;

    Node<T> traversal = currentParent;
    while (traversal.parentEdge != null) {
      traversal.parentEdge.weight += n.data.getWeight();
      traversal = traversal.parentEdge.parent;
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
    while (traversal.parentEdge != null) {
      traversal.parentEdge.weight += diff;
      traversal = traversal.parentEdge.parent;
    }
  }

  @Override
  // returns number of elements
  public int getSize() {
    return currentElements.size();
  }

  private static class Node<T> {
    public Edge<T> parentEdge;
    public Edge<T> leftEdge;
    public Edge<T> rightEdge;
    public Node<T> adj; // represents a rightEdge pointer to the adjacent node.

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
