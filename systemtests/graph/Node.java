package graph;

public class Node {
    private Graph owner;
    private String name;
    
    public Node(String name) {
        this.name = name;
    }

    public void setOwner(Graph a) {
        this.owner = a;
    }

    public void addEdge(Node n) {
        owner.addEdge(this, n);
    }

    public int getDegree() {
        return owner.getDegree(this);
    }
}
