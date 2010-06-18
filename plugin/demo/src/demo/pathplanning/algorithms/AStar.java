package demo.pathplanning.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import demo.pathplanning.model.CartesianGraph;
import demo.pathplanning.model.Location;
import demo.pathplanning.model.Node;

public class AStar implements PathPlanner {
  CartesianGraph fGraph;
  
  public AStar(CartesianGraph graph) {
    fGraph = graph;
  }
  
  @Override
  public List<Node> getPath(Location startLoc, Location goalLoc) {
    Node start = fGraph.getNode(startLoc);
    Node goal = fGraph.getNode(goalLoc);

    HashSet<Node> closedSet = new HashSet<Node>();
    HashSet<Node> openSet = new HashSet<Node>();
    openSet.add(start);

    HashMap<Node, Integer> gScoreByNode = new HashMap<Node, Integer>();
    gScoreByNode.put(start, 0);

    int h = manhattanDist(start, goal);
    HashMap<Node, Integer> hScoreByNode = new HashMap<Node, Integer>();
    hScoreByNode.put(start, h);

    HashMap<Node, Integer> fScoreByNode = new HashMap<Node, Integer>();
    fScoreByNode.put(start, h);

    HashMap<Node, Node> preceedingNodeByNode = new HashMap<Node, Node>();
    while (!openSet.isEmpty()) {
      Node x = getLowestFScore(openSet, fScoreByNode);
      if (x.equals(goal)) {
        return reconstructPath(preceedingNodeByNode, goal);
      }

      openSet.remove(x);
      closedSet.add(x);

      for (Node y : x.getNeighbors()) {
        if (!closedSet.contains(y)) {
          int tmpGScore = gScoreByNode.get(x) + y.getCost();
          boolean tmpIsBetter;

          if (!openSet.contains(y)) {
            openSet.add(y);
            tmpIsBetter = true;
          } else if (tmpGScore < gScoreByNode.get(y)) {
            tmpIsBetter = true;
          } else {
            tmpIsBetter = false;
          }
          if (tmpIsBetter == true) {
            preceedingNodeByNode.put(y, x);

            h = manhattanDist(y, goal);
            gScoreByNode.put(y, tmpGScore);
            hScoreByNode.put(y, h);
            fScoreByNode.put(y, tmpGScore + h);
          }
        }
      }
    }
    return null;
  }

  private List<Node> reconstructPath(HashMap<Node, Node> preceedingNodeByNode, Node goal) {
    Stack<Node> path = new Stack<Node>();

    path.push(goal);

    Node node = preceedingNodeByNode.get(goal);
    while (node != null) {
      path.push(node);
      node = preceedingNodeByNode.get(node);
    }

    return path;
  }

  /**
   * 
   * @return the node in openSet with the lowest fScore
   */
  private Node getLowestFScore(HashSet<Node> openSet, HashMap<Node, Integer> fScoreByNode) {
    Node minFNode = null;
    int minFScore = Integer.MAX_VALUE;
    for (Node node : openSet) {
      Integer otherFScore = fScoreByNode.get(node);
      if (otherFScore != null) {
        if (otherFScore < minFScore) {
          minFNode = node;
          minFScore = otherFScore;
        }
      }
    }
    return minFNode;
  }

  private int manhattanDist(Node node, Node end) {
    int dx = Math.abs(node.getLocation().getX() - end.getLocation().getY());
    int dy = Math.abs(node.getLocation().getY() - end.getLocation().getY());

    return dx + dy;
  }

  @Override
  public CartesianGraph getGraph() {
    return fGraph;
  }
}
