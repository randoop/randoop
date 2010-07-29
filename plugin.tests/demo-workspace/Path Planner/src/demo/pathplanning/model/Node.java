package demo.pathplanning.model;

import java.util.Collection;
import java.util.HashSet;


public class Node {
  private CartesianGraph fGraph;
  private Location fLocation;
  private int fCost;
  
  public Node(CartesianGraph graph, Location location, int cost) {
    fGraph = graph;
    fLocation = location;
    fCost = cost;
  }

  public Collection<Node> getNeighbors() {
    Collection<Node> neighbors = new HashSet<Node>();
    for (Direction dir : Direction.values()) {
      Node neighbor = getNeighbor(dir);
      if (neighbor != null)
        neighbors.add(neighbor);
    }
    return neighbors;
  }

  public Node getNeighbor(Direction dir) {
    return fGraph.getNode(getLocation(), dir);
  }
  
  public Location getLocation() {
    return fLocation;
  }

  public int getCost() {
    return fCost;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Node))
      return false;
    Node otherNode = (Node) obj;

    return getCost() == otherNode.getCost()
        && getLocation().equals(otherNode.getLocation());
  }

  @Override
  public int hashCode() {
    return getCost() + getLocation().hashCode();
  }
  
  @Override
  public String toString() {
    Location loc = getLocation();
    return "(" + loc.getX() + "," + loc.getY() + ")=" + getCost(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
