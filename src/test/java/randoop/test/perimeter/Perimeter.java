package randoop.test.perimeter;

/**
 * A Java version of the <tt>perimeter</tt> Olden benchmark.
 * <p>
 * The algorithm computes the total perimeter of a region
 * in a binary image represented by a quadtree.  The
 * algorithm was presented in the paper:
 * <p>
 * <cite>
 * Hanan Samet, "Computing Perimeters of Regions in Images
 * Represented by Quadtrees," IEEE Transactions on Pattern
 * Analysis and Machine Intelligence, PAMI-3(6), November, 1981.
 * </cite>
 * <p>
 * The benchmark creates an image, count the number of leaves on the
 * quadtree and then computes the perimeter of the image using Samet's
 * algorithm.
 */
public class Perimeter {
  /**
   * The number of levels in the tree/image.
   */
  private static int levels = 0;
  /**
   * Set to true to print the final result.
   */
  private static boolean printResult = false;
  /**
   * Set to true to print information messages.
   */
  private static boolean printMsgs = false;

  /**
   * The entry point to computing the perimeter of an image.
   * @param args the command line arguments
   */
  public static void main(String[] args) {

    parseCmdLine(args);

    int size = 1 << levels;
    int msize = 1 << (levels - 1);
    QuadTreeNode.gcmp = size * 1024;
    QuadTreeNode.lcmp = msize * 1024;

    long start0 = System.currentTimeMillis();
    QuadTreeNode tree = QuadTreeNode.createTree(msize, 0, 0, null, Quadrant.cSouthEast, levels);
    long end0 = System.currentTimeMillis();

    long start1 = System.currentTimeMillis();
    int leaves = tree.countTree();
    long end1 = System.currentTimeMillis();

    long start2 = System.currentTimeMillis();
    int perm = tree.perimeter(size);
    long end2 = System.currentTimeMillis();

    if (printResult) {
      System.out.println("Perimeter is " + perm);
      System.out.println("Number of leaves " + leaves);
    }

    if (printMsgs) {
      System.out.println("QuadTree alloc time " + (end0 - start0) / 1000.0);
      System.out.println("Count leaves time " + (end1 - start1) / 1000.0);
      System.out.println("Perimeter compute time " + (end2 - start2) / 1000.0);
    }
    //System.out.println("Done!");
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

      if (arg.equals("-l")) {
        if (i < args.length) {
          levels = Integer.parseInt(args[i++]);
        } else {
          throw new Error("-l requires the number of levels");
        }
      } else if (arg.equals("-p")) {
        printResult = true;
      } else if (arg.equals("-m")) {
        printMsgs = true;
      } else if (arg.equals("-h")) {
        usage();
      }
    }
    if (levels == 0) usage();
  }

  /**
   * The usage routine which describes the program options.
   */
  private static final void usage() {
    System.err.println("usage: java Perimeter -l <num> [-p] [-m] [-h]");
    System.err.println("    -l number of levels in the quadtree (image size = 2^l)");
    System.err.println("    -p (print the results)");
    System.err.println("    -m (print informative messages)");
    System.err.println("    -h (this message)");
    System.exit(0);
  }
}
