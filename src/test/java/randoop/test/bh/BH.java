package randoop.test.bh;

import java.util.Enumeration;

/**
 * A Java implementation of the <tt>bh</tt> Olden benchmark.
 * The Olden benchmark implements the Barnes-Hut benchmark
 * that is decribed in :
 * <p><cite>
 * J. Barnes and P. Hut, "A hierarchical o(NlogN) force-calculation algorithm",
 * Nature, 324:446-449, Dec. 1986
 * </cite>
 * <p>
 * The original code in the Olden benchmark suite is derived from the
 * <a href="ftp://hubble.ifa.hawaii.edu/pub/barnes/treecode">
 * source distributed by Barnes</a>.
 */
public class BH {
  /**
   * The user specified number of bodies to create.
   */
  private static int nbody = 0;

  /**
   * The maximum number of time steps to take in the simulation
   */
  private static int nsteps = 10;

  /**
   * Should we print information messsages
   */
  private static boolean printMsgs = false;
  /**
   * Should we print detailed results
   */
  private static boolean printResults = false;

  static double DTIME = 0.0125;
  private static double TSTOP = 2.0;

  public static final void main(String[] args) {
    parseCmdLine(args);

    if (printMsgs) {
      System.out.println("nbody = " + nbody);
    }

    long start0 = System.currentTimeMillis();
    Tree root = new Tree();
    root.createTestData(nbody);
    long end0 = System.currentTimeMillis();
    if (printMsgs) {
      System.out.println("Bodies created");
    }

    long start1 = System.currentTimeMillis();
    double tnow = 0.0;
    int i = 0;
    while ((tnow < TSTOP + 0.1 * DTIME) && (i < nsteps)) {
      root.stepSystem(i++);
      tnow += DTIME;
    }
    long end1 = System.currentTimeMillis();

    if (printResults) {
      int j = 0;
      for (Enumeration<Body> e = root.bodies(); e.hasMoreElements(); ) {
        Body b = e.nextElement();
        System.out.println("body " + j++ + " -- " + b.pos);
      }
    }

    if (printMsgs) {
      System.out.println("Build Time " + (end0 - start0) / 1000.0);
      System.out.println("Compute Time " + (end1 - start1) / 1000.0);
      System.out.println("Total Time " + (end1 - start0) / 1000.0);
    }
    System.out.println("Done!");
  }

  /**
   * Random number generator used by the orignal BH benchmark.
   * @param seed the seed to the generator
   * @return a random number
   */
  public static final double myRand(double seed) {
    double t = 16807.0 * seed + 1;

    seed = t - (2147483647.0 * Math.floor(t / 2147483647.0));
    return seed;
  }

  /**
   * Generate a floating point random number.  Used by
   * the original BH benchmark.
   *
   * @param xl lower bound
   * @param xh upper bound
   * @param r seed
   * @return a floating point randon number
   */
  public static final double xRand(double xl, double xh, double r) {
    double res = xl + (xh - xl) * r / 2147483647.0;
    return res;
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

      // check for options that require arguments
      if (arg.equals("-b")) {
        if (i < args.length) {
          nbody = Integer.parseInt(args[i++]);
        } else {
          throw new Error("-l requires the number of levels");
        }
      } else if (arg.equals("-s")) {
        if (i < args.length) {
          nsteps = Integer.parseInt(args[i++]);
        } else {
          throw new Error("-l requires the number of levels");
        }
      } else if (arg.equals("-m")) {
        printMsgs = true;
      } else if (arg.equals("-p")) {
        printResults = true;
      } else if (arg.equals("-h")) {
        usage();
      }
    }
    if (nbody == 0) usage();
  }

  /**
   * The usage routine which describes the program options.
   */
  private static final void usage() {
    // Commented out to avoid confusing printout during tests.
    //    System.err.println("usage: java BH -b <size> [-s <steps>] [-p] [-m] [-h]");
    //    System.err.println("    -b the number of bodies");
    //    System.err.println("    -s the max. number of time steps (default=10)");
    //    System.err.println("    -p (print detailed results)");
    //    System.err.println("    -m (print information messages");
    //    System.err.println("    -h (this message)");
  }
}
