package demo.pathplanning.algorithms;

import java.util.List;

import demo.pathplanning.model.CartesianGraph;
import demo.pathplanning.model.Location;
import demo.pathplanning.model.Node;

public class PathPlanningContext {
  PathPlanner fPathPlanner;

  public PathPlanningContext(PathPlanner pathPlanner) {
    fPathPlanner = pathPlanner;
  }

  public List<Node> getPath(Location start, Location goal) {
    return fPathPlanner.getPath(start, goal);
  }

  public static void main(String[] args) {
    CartesianGraph graph = new CartesianGraph(10, 10);
    PathPlanner strategy = new AStar(graph);
    PathPlanningContext ppc = new PathPlanningContext(strategy);

    System.out.println(graph);
    List<Node> path = ppc.getPath(new Location(0, 0), new Location(9, 9));
    for (int i = 0; i < graph.getXSize(); i++) {
      for (int j = 0; j < graph.getYSize(); j++) {
        Location loc = new Location(i, j);
        Node n = graph.getNode(loc);

        if (path.contains(n)) {
          System.out.print("X"); //$NON-NLS-1$
        } else {
          System.out.print(" "); //$NON-NLS-1$
        }
      }
      System.out.println();
    }
  }
}
