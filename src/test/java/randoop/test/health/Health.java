package randoop.test.health;

/**
 * A Java implementation of the <tt>health</tt> Olden benchmark.  The Olden
 * benchmark simulates the Columbian health-care system:
 * <p>
 * <cite>
 * G. Lomow , J. Cleary, B. Unger, and D. West. "A Performance Study of
 * Time Warp," In SCS Multiconference on Distributed Simulation, pages 50-55,
 * Feb. 1988.
 * </cite>
 */
public class Health {
  /**
   * The size of the health-care system.
   */
  private static int maxLevel = 0;
  /**
   * The maximum amount of time to use in the simulation.
   */
  private static int maxTime = 0;
  /**
   * A seed value for the random no. generator.
   */
  private static int seed = 0;
  /**
   * Set to true to print the results.
   */
  private static boolean printResult = false;
  /**
   * Set to true to print information messages.
   */
  private static boolean printMsgs = false;

  /**
   * The main routnie which creates the data structures for the Columbian
   * health-care system and executes the simulation for a specified time.
   * @param args the command line arguments
   */
  public static final void main(String[] args) {
    parseCmdLine(args);

    long start0 = System.currentTimeMillis();
    Village top = Village.createVillage(maxLevel, 0, null, seed);
    long end0 = System.currentTimeMillis();

    if (printMsgs) {
      System.out.println("Columbian Health Care Simulator. Working...");
    }

    long start1 = System.currentTimeMillis();
    for (int i = 0; i < maxTime; i++) {
      if ((i % 50) == 0 && printMsgs) System.out.println(i);
      top.simulate();
    }

    Results r = top.getResults();

    long end1 = System.currentTimeMillis();

    if (printResult || printMsgs) {
      System.out.println("# of people treated:            " + r.totalPatients + " people");
      System.out.println(
          "Average length of stay:         " + r.totalTime / r.totalPatients + " time units");
      System.out.println("Average # of hospitals visited: " + r.totalHospitals / r.totalPatients);
    }
    if (printMsgs) {
      System.out.println("Build Time " + (end0 - start0) / 1000.0);
      System.out.println("Run Time " + (end1 - start1) / 1000.0);
      System.out.println("Total Time " + (end1 - start0) / 1000.0);
    }

    System.out.println("Done!");
  }

  private static final void parseCmdLine(String[] args) {
    String arg;
    int i = 0;
    while (i < args.length && args[i].startsWith("-")) {
      arg = args[i++];

      // check for options that require arguments
      if (arg.equals("-l")) {
        if (i < args.length) {
          maxLevel = Integer.parseInt(args[i++]);
        } else {
          throw new Error("-l requires the number of levels");
        }
      } else if (arg.equals("-t")) {
        if (i < args.length) {
          maxTime = Integer.parseInt(args[i++]);
        } else {
          throw new Error("-t requires the amount of time");
        }
      } else if (arg.equals("-s")) {
        if (i < args.length) {
          seed = Integer.parseInt(args[i++]);
        } else {
          throw new Error("-s requires a seed value");
        }
      } else if (arg.equals("-p")) {
        printResult = true;
      } else if (arg.equals("-m")) {
        printMsgs = true;
      } else if (arg.equals("-h")) {
        usage();
      }
    }
    if (maxTime == 0 || maxLevel == 0 || seed == 0) usage();
  }

  /**
   * The usage routine which describes the program options.
   */
  private static final void usage() {
    System.err.println("usage: java Health -l <levels> -t <time> -s <seed> [-p] [-m] [-h]");
    System.err.println("    -l the size of the health care system");
    System.err.println("    -t the amount of simulation time");
    System.err.println("    -s a random no. generator seed");
    System.err.println("    -p (print results)");
    System.err.println("    -m (print information messages");
    System.err.println("    -h (this message)");
    System.exit(0);
  }
}
