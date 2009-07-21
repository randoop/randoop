package randoop.experiments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;






public class JPFRun {

  public static String JPF_LOCATION = "/afs/csail.mit.edu/u/c/cpacheco/jpf2/javapathfinder/trunk/examples/issta2006";
  public static String JOE_LOCATION = "/afs/csail.mit.edu/u/c/cpacheco/temp-eclipse/randoop_svn";

  public static int numRuns = 10;

  private enum Container { BINTREE, BINOMIALHEAP, FIBHEAP, TREEMAP }

  private static class OneRunResults {
    public Container container;
    public int coverage;
    public long timeToMax; // millis
    public long totalTime; // millis

    public OneRunResults(Container container, int coverage, long timeToMax, long totalTime) {
      this.container = container;
      this.coverage = coverage;
      this.timeToMax = timeToMax;
      this.totalTime = totalTime;
    }

    @Override
    public String toString() {
      return container + " coverage: " + coverage + " timeToMax: " + timeToMax + " totalTime: " + totalTime;
    }

    public OneRunResults(Container container, String string) {

      long lastTimeCoverageImprovedMillis = -1;
      long totalTimeMillis = -1;
      int finalCoverage = -1;
      for (String line : string.split("\\n")) {
        line = line.trim();
        if (line.startsWith("TIME=")) {
          lastTimeCoverageImprovedMillis = Integer.parseInt(line.substring("TIME=".length()).trim());
        } else if (line.startsWith("Execution time:")) {
          assert totalTimeMillis == -1; // Check only occurs once.
          totalTimeMillis = Integer.parseInt(line.substring("Execution time:".length()).trim()) * 1000; // JPF gives seconds.
        } else if (line.startsWith("TestsCovered:")) {
          assert finalCoverage == -1; // Check only occurs once.
          finalCoverage = Integer.parseInt(line.substring("TestsCovered:".length()).trim());
        }
      }

      assert lastTimeCoverageImprovedMillis != -1 && totalTimeMillis != -1 && finalCoverage != -1 : string;

      // This happens if the JPF run is very fast; it only reports seconds so it can say
      // that it took 0 seconds to finish. In that case, set the total time to the
      // last time coverage improved (this favors JPF in terms of time; that's ok).
      if (totalTimeMillis == 0) totalTimeMillis = lastTimeCoverageImprovedMillis;

      this.container = container;
      this.coverage = finalCoverage;
      this.timeToMax = lastTimeCoverageImprovedMillis;
      this.totalTime = totalTimeMillis;
    }
  }

  public static OneRunResults runShape(Container container, int bound) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(bos);

    List<String> command = new ArrayList<String>();
    command.add(JPF_LOCATION + "/../../bin/jpf");
    command.add("-c");
    command.add("examples/issta2006/test.properties");
    command.add(envName(container));
    command.add(Integer.toString(bound));
    command.add(Integer.toString(bound));
    command.add("6");
    Command.exec(command.toArray(new String[0]), printStream, printStream, "", true, new File(JPF_LOCATION));
    return new OneRunResults(container, bos.toString());
  }

  private static enum RandoopMode { DIRECTED, UNDIRECTED }

  public static OneRunResults runRandoop(Container container, RandoopMode mode) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(bos);
    List<String> command = new ArrayList<String>();
    command.add(JOE_LOCATION + "/randoop");
    command.add("containers");
    command.add(randoopName(container));
    command.add(mode == RandoopMode.DIRECTED ? "directed" : "undirected");
    Command.exec(command.toArray(new String[0]), printStream, printStream, "", true, new File(JOE_LOCATION));
    return new OneRunResults(container, bos.toString());
  }

  public static OneRunResults runRandom(Container container, int bound) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(bos);

    List<String> command = new ArrayList<String>();
    command.add(JPF_LOCATION + "/../../bin/jpf");
    command.add("-c");
    command.add("examples/issta2006/rand.properties");
    command.add(envName(container));
    command.add(Integer.toString(bound));
    command.add(Integer.toString(bound));
    command.add("0");
    Command.exec(command.toArray(new String[0]), printStream, printStream, "", true, new File(JPF_LOCATION));
    return new OneRunResults(container, bos.toString());
  }

  public static void run() {

//  For testing.
//  run(Container.BINTREE, 2, 2);
//  run(Container.BINOMIALHEAP, 2, 2);
//  run(Container.FIBHEAP, 2, 2);
//  run(Container.TREEMAP, 2, 2);
    run(Container.BINOMIALHEAP, 29, 48);
    run(Container.TREEMAP, 20, 39);        
    run(Container.BINTREE, 9, 8);
    run(Container.FIBHEAP, 15, 39);


  }

  public static void run(Container container, int seqLengthShape, int seqLengthRandom) {

    List<OneRunResults> results = new ArrayList<OneRunResults>();

    results.clear();
    for (int i = 0 ; i < numRuns ; i++) {
      OneRunResults r = runRandoop(container, RandoopMode.DIRECTED);
      results.add(r);
      System.out.println("% " + r);
    }
    average(results, texName(container) + "Randoop");

    results.clear();
    for (int i = 0 ; i < numRuns ; i++) {
      OneRunResults r = runRandoop(container, RandoopMode.UNDIRECTED);
      results.add(r);
      System.out.println("% " + r);
    }
    average(results, texName(container) + "RandoopRand");


//  for (int i = 0 ; i < numRuns ; i++) {
//  OneRunResults r = runShape(container, seqLengthShape);
//  results.add(r);
//  System.out.println("% " + r);
//  }
//  average(results, texName(container) + "Shape");


//  results.clear();
//  for (int i = 0 ; i < numRuns ; i++) {
//  OneRunResults r = runRandom(container, seqLengthRandom);
//  results.add(r);
//  System.out.println("% " + r);
//  }
//  average(results, texName(container) + "Rand");


//  results.clear();
//  for (int i = 0 ; i < numRuns ; i++) {
//  OneRunResults r = runRandoop(container, RandoopMode.DIRECTED);
//  results.add(r);
//  System.out.println("% " + r);
//  }
//  average(results, texName(container) + "Randoop");

//  results.clear();
//  for (int i = 0 ; i < numRuns ; i++) {
//  OneRunResults r = runRandoop(container, RandoopMode.UNDIRECTED);
//  results.add(r);
//  System.out.println("% " + r);
//  }
//  average(results, texName(container) + "RandoopRand");
  }

  private static void average(List<OneRunResults> binTreeResults, String prefix) {

    double coverageAccum = 0;
    double timeToMaxAccum = 0;
    double totalTimeAccum = 0;

    for (OneRunResults r : binTreeResults) {
      coverageAccum += r.coverage;
      timeToMaxAccum += r.timeToMax;
      totalTimeAccum += r.totalTime;
    }

    MathContext mc = new MathContext(3);

    BigDecimal avCoverage = new BigDecimal(coverageAccum / binTreeResults.size(), mc);
    System.out.println("\\newcommand{\\" + prefix + "Cov}{" + avCoverage + "}");

    mc = new MathContext(2);

    BigDecimal avTimeToMax = new BigDecimal((timeToMaxAccum / binTreeResults.size())/1000, mc);
    System.out.println("\\newcommand{\\" + prefix + "TimeToMax}{" + avTimeToMax + "}");

    BigDecimal avTotalTime = new BigDecimal((totalTimeAccum / binTreeResults.size())/1000, mc);
    System.out.println("\\newcommand{\\" + prefix + "TotalTime}{" + avTotalTime + "}");

  }

  private static String envName(Container container) {
    if (container == Container.TREEMAP) {
      return "issta2006.TreeMap.EnvTreeMap";   
    } else if (container == Container.BINOMIALHEAP) {
      return "issta2006.BinomialHeap.EnvBinomialHeap";
    } else if (container == Container.FIBHEAP) {
      return "issta2006.FibHeap.EnvFibHeap";
    } else if (container == Container.BINTREE) {
      return "issta2006.BinTree.EnvBinTree";
    } else {
      throw new RuntimeException(container.toString());
    }
  }

  private static String randoopName(Container container) {
    if (container == Container.TREEMAP) {
      return "randoop.test.issta2006.TreeMap";   
    } else if (container == Container.BINOMIALHEAP) {
      return "randoop.test.issta2006.BinomialHeap";
    } else if (container == Container.FIBHEAP) {
      return "randoop.test.issta2006.FibHeap";
    } else if (container == Container.BINTREE) {
      return "randoop.test.issta2006.BinTree";
    } else {
      throw new RuntimeException(container.toString());
    }
  }

  private static String texName(Container container) {
    if (container == Container.TREEMAP) {
      return "treemap";   
    } else if (container == Container.BINOMIALHEAP) {
      return "binheap";
    } else if (container == Container.FIBHEAP) {
      return "fibheap";
    } else if (container == Container.BINTREE) {
      return "bintree";
    } else {
      throw new RuntimeException(container.toString());
    }
  }
}
