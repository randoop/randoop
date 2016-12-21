package randoop.test.mst;

/**
 * A class that represents a graph data structure.
 */
public class Graph {
  /**
   * List of vertices in the graph.
   */
  private Vertex[] nodes;

  // parameters for the random number generater
  private final static int CONST_m1 = 10000;
  private final static int CONST_b = 31415821;
  private final static int RANGE = 2048;

  /**
   * Create a graph.
   * @param numvert the number of vertices in the graph
   */
  public Graph(int numvert) {
    nodes = new Vertex[numvert];
    Vertex v = null;
    // the original C code creates them in reverse order
    for (int i = numvert - 1; i >= 0; i--) {
      Vertex tmp = nodes[i] = new Vertex(v, numvert);
      v = tmp;
    }
    addEdges(numvert);
  }

  /**
   * Create a graph.  This is just another method for
   * creating the graph data structure.
   * @param numvert the size of the graph
   */
  public void createGraph(int numvert) {
    nodes = new Vertex[numvert];
    Vertex v = null;
    // the original C code creates them in reverse order
    for (int i = numvert - 1; i >= 0; i--) {
      Vertex tmp = nodes[i] = new Vertex(v, numvert);
      v = tmp;
    }

    addEdges(numvert);
  }

  /**
   * Return the first node in the graph.
   * @return the first node in the graph
   */
  public Vertex firstNode() {
    return nodes[0];
  }

  /**
   * Add edges to the graph.  Edges are added to/from every node
   * in the graph and a distance is computed for each of them.
   * @param numvert the number of nodes in the graph
   */
  private void addEdges(int numvert) {
    int count1 = 0;

    for (Vertex tmp = nodes[0]; tmp != null; tmp = tmp.next()) {
      Hashtable hash = tmp.neighbors();
      for (int i = 0; i < numvert; i++) {
        if (i != count1) {
          int dist = computeDist(i, count1, numvert);
          hash.put(nodes[i], new Integer(dist));
        }
      }
      count1++;
    }
  }

  /**
   * Compute the distance between two edges.  A random number generator
   * is used to compute the distance.
   */
  private int computeDist(int i, int j, int numvert) {
    int less, gt;
    if (i < j) {
      less = i;
      gt = j;
    } else {
      less = j;
      gt = i;
    }
    return (random(less * numvert + gt) % RANGE) + 1;
  }

  private static int mult(int p, int q) {
    int p1, p0, q1, q0;

    p1 = p / CONST_m1;
    p0 = p % CONST_m1;
    q1 = q / CONST_m1;
    q0 = q % CONST_m1;
    return (((p0 * q1 + p1 * q0) % CONST_m1) * CONST_m1 + p0 * q0);
  }

  private static int random(int seed) {
    return mult(seed, CONST_b) + 1;
  }
}
