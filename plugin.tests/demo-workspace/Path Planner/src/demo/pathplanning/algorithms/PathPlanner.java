package demo.pathplanning.algorithms;

import java.util.List;

import demo.pathplanning.model.CartesianGraph;
import demo.pathplanning.model.Location;
import demo.pathplanning.model.Node;

public interface PathPlanner {
  
  public CartesianGraph getGraph();
  
  public List<Node> getPath(Location start, Location goal);
}
