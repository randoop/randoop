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

  public void thisIsNotCalled() {
    getDegree();
  }

  // tests the fact that mutating a field is like mutating its own
  // state (for propagation)
  public void setOwnerName(String name) {
    owner.setName(name);
  }

  // tests the fact that mutating a field is like mutating its own
  // state (for simple analysis)
  public void setOwnerNameDirectly(String name) {
    owner.name = name;
  }
}
