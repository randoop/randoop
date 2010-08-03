package graph;

import java.util.*;

public class Graph {
    private Map<Node, Set<Node>> edges;
    
    public static Graph generateGraph() {
        Graph g = new Graph();
        g.init();
        return g;
    }

    public void init() {
        edges = new HashMap<Node, Set<Node>>();
    }

    public void addNode(Node n) {
        edges.put(n, new HashSet<Node>());
    }

    public void addEdge(Node n1, Node n2) {
        addToNode(n1,n2);
        addToNode(n2,n1);
    }
    
    private void addToNode(Node source, Node target) {
        Set<Node> succ = edges.get(source);
        succ.add(target);
        edges.put(source, succ);
    }

    public int getDegree(Node n) {
        return edges.get(n).size();
    }
}
