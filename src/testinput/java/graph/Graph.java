package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph {
  private Map<Node, Set<Node>> edges;

  static List<Graph> allGraphs = new ArrayList<Graph>();

  // This is public so that Node.setOwnerNameDirectly can set it, to
  // test that this.foo.bar = baz gets caught
  public String name = "[graph]";

  public static Graph generateGraph() {
    Graph g = new Graph();
    g.init();
    allGraphs.add(g);
    return g;
  }

  public void init() {
    edges = new HashMap<Node, Set<Node>>();
  }

  public void addNode(Node n) {
    edges.put(n, new HashSet<Node>());
  }

  public void addEdge(Node n1, Node n2) {
    addToNode(n1, n2);
    addToNode(n2, n1);
  }

  private void addToNode(Node source, Node target) {
    Set<Node> succ = edges.get(source);
    succ.add(target);
    edges.put(source, succ);
  }

  public int getDegree(Node n) {
    return edges.get(n).size();
  }

  public void setName(String newName) {
    name = newName;
  }
}
