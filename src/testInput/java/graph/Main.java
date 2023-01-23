package graph;

public class Main {
  public static void main(String[] args) throws ClassNotFoundException {
    Class.forName("graph.Graph");
    Graph g1 = Graph.generateGraph();
    Node n1 = new Node("NYC");
    n1.setOwner(g1);
    Node n2 = new Node("Boston");
    n2.setOwner(g1);
    g1.addNode(n1);
    g1.addNode(n2);
    n1.addEdge(n2);
    n1.getDegree();
  }
}
