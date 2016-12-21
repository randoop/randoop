package randoop.test.mst;

/**
 * A Java implementation of the <tt>mst</tt> Olden benchmark.  The Olden
 * benchmark computes the minimum spanning tree of a graph using
 * Bentley's algorithm.
 * <p><cite>
 * J. Bentley. "A Parallel Algorithm for Constructing Minimum Spanning Trees"
 * J. of Algorithms, 1:51-59, 1980.
 * </cite>
 * <p>
 * As with the original C version, this one uses its own implementation
 * of hashtable.
 */
public class MST {
  /**
   * The number of vertices in the graph.
   */
  private static int vertices = 0;
  /**
   * Set to true to print the final result.
   */
  private static boolean printResult = false;
  /**
   * Set to true to print information messages and timing values
   */
  private static boolean printMsgs = false;

  public static void main(String[] args) {
    parseCmdLine(args);

    if (printMsgs) System.out.println("Making graph of size " + vertices);
    long start0 = System.currentTimeMillis();
    Graph graph = new Graph(vertices);
    long end0 = System.currentTimeMillis();

    if (printMsgs) System.out.println("About to compute MST");
    long start1 = System.currentTimeMillis();
    int dist = computeMST(graph, vertices);
    long end1 = System.currentTimeMillis();

    if (printResult || printMsgs) {
      System.out.println("MST has cost " + dist);
    }

    if (printMsgs) {
      System.out.println("Build graph time " + (end0 - start0) / 1000.0);
      System.out.println("Compute time " + (end1 - start1) / 1000.0);
      System.out.println("Total time " + (end1 - start0) / 1000.0);
    }

    System.out.println("Done!");
  }

  /**
   * The method to compute the minimum spanning tree.
   * @param graph the graph data structure
   * @param numvert the number of vertices in the graph
   * @return the minimum spanning tree cost
   */
  public static int computeMST(Graph graph, int numvert) {
    int cost = 0;

    // Insert first node
    Vertex inserted = graph.firstNode();
    Vertex tmp = inserted.next();
    MyVertexList = tmp;
    numvert--;

    // Annonunce insertion and find next one
    while (numvert != 0) {
      //System.out.println("numvert= " +numvert);
      BlueReturn br = doAllBlueRule(inserted);
      inserted = br.vert();
      int dist = br.dist();
      numvert--;
      cost += dist;
    }
    return cost;
  }

  private static BlueReturn BlueRule(Vertex inserted, Vertex vlist) {
    BlueReturn retval = new BlueReturn();

    if (vlist == null) {
      retval.setDist(999999);
      return retval;
    }

    Vertex prev = vlist;
    retval.setVert(vlist);
    retval.setDist(vlist.mindist());
    Hashtable hash = vlist.neighbors();
    Object o = hash.get(inserted);
    if (o != null) {
      int dist = ((Integer) o).intValue();
      if (dist < retval.dist()) {
        vlist.setMindist(dist);
        retval.setDist(dist);
      }
    } else {
      System.out.println("Not found");
    }

    int count = 0;
    // We are guaranteed that inserted is not first in list
    for (Vertex tmp = vlist.next(); tmp != null; prev = tmp, tmp = tmp.next()) {
      count++;
      if (tmp == inserted) {
        Vertex next = tmp.next();
        prev.setNext(next);
      } else {
        hash = tmp.neighbors();
        int dist2 = tmp.mindist();
        o = hash.get(inserted);
        if (o != null) {
          int dist = ((Integer) o).intValue();
          if (dist < dist2) {
            tmp.setMindist(dist);
            dist2 = dist;
          }
        } else {
          System.out.println("Not found");
        }

        if (dist2 < retval.dist()) {
          retval.setVert(tmp);
          retval.setDist(dist2);
        }
      } // else
    } // for
    return retval;
  }

  private static Vertex MyVertexList = null;

  private static BlueReturn doAllBlueRule(Vertex inserted) {
    if (inserted == MyVertexList) MyVertexList = MyVertexList.next();
    return BlueRule(inserted, MyVertexList);
  }

  /**
   * Parse the command line options.
   * @param args the command line options
   */
  private static final void parseCmdLine(String[] args) {
    int i = 0;
    String arg;

    while (i < args.length && args[i].startsWith("-")) {
      arg = args[i++];

      if (arg.equals("-v")) {
        if (i < args.length) {
          vertices = Integer.parseInt(args[i++]);
        } else throw new RuntimeException("-v requires the number of vertices");
      } else if (arg.equals("-p")) {
        printResult = true;
      } else if (arg.equals("-m")) {
        printMsgs = true;
      } else if (arg.equals("-h")) {
        usage();
      }
    }
    if (vertices == 0) usage();
  }

  /**
   * The usage routine which describes the program options.
   */
  private static final void usage() {
    System.err.println("usage: java MST -v <levels> [-p] [-m] [-h]");
    System.err.println("    -v the number of vertices in the graph");
    System.err.println("    -p (print the result>)");
    System.err.println("    -m (print informative messages)");
    System.err.println("    -h (this message)");
    System.exit(0);
  }
}

/**
 * Not really sure what this is for?
 */
class BlueReturn {
  private Vertex vert;
  private int dist;

  public Vertex vert() {
    return vert;
  }

  public void setVert(Vertex v) {
    vert = v;
  }

  public int dist() {
    return dist;
  }

  public void setDist(int d) {
    dist = d;
  }
}
