package randoop.util;

import java.util.Map;

import randoop.Globals;
import randoop.SequenceGeneratorStats;
import utilpag.UtilMDE;

/**
 * Modified from Daikon.FileIOProgress.
 */
public class ProgressDisplay extends Thread {

  private static int progresswidth = 170;

  private static int exit_if_no_new_sequences_after_mseconds = 10000;

  public static enum Mode { SINGLE_LINE_OVERWRITE, MULTILINE, NO_DISPLAY }

  private Mode outputMode;
  private final long progressIntervalMillis = 1000;
  private SequenceGeneratorStats stats;

  public ProgressDisplay(SequenceGeneratorStats stats,
      Mode outputMode, int progressWidth) {
    this.stats = stats;
    this.outputMode = outputMode;
    this.progresswidth = progressWidth;
    setDaemon(true);
  }

  private int queries = 0;

  public String message() {
    StringBuilder b = new StringBuilder();
    if (queries++ % 10 == 0)
      b.append(stats.getTitle());
    b.append(stats.toStringGlobal());
    return b.toString();
  }

  /**
   * Clients should set this variable instead of calling Thread.stop(),
   * which is deprecated.  Typically a client calls "display()" before
   * setting this.
   **/
  public boolean shouldStop = false;

  @Override
  public void run() {
    while (true) {
      if (shouldStop) {
        clear();
        return;
      }
      display();
      updateLastBranchCov();

      // Check that we're still doing progress. If no new inputs
      // generated for several seconds, we're probably in an infinite
      // loop, and should exit.
      updateLastSeqGen();
      long now = System.currentTimeMillis();
      if (now - lastNumSeqsIncrease > exit_if_no_new_sequences_after_mseconds) {
        printStackTraceAndExit();
      }

      try {
        sleep(progressIntervalMillis);
      } catch (InterruptedException e) {
        // hmm
      }
    }
  }

  private void printStackTraceAndExit() {

    System.out.println();
    System.out.print("*** Randoop has detected no input generation attempts after ");
    System.out.println(exit_if_no_new_sequences_after_mseconds + " milliseconds.");
    System.out.println("This indicates Randoop may be executing an sequence");
    System.out.println("that leads to nonterminating behavior.");
    System.out.println("Last sequence generated:");
    System.out.println();
    System.out.println(stats.currSeq);
    System.out.println();
    System.out.println("Will print all thread stack traces and exit with code 1.");

    for (Map.Entry<Thread,StackTraceElement[]> trace : Thread.getAllStackTraces().entrySet()) {
      System.out.println("--------------------------------------------------");
      System.out.println("Thread " + trace.getKey().toString());
      System.out.println("Stack trace:");
      StackTraceElement[] elts = trace.getValue();
      for (int i = 0 ; i < elts.length ; i++) {
        System.out.println(elts[i]);
      }
    }
    System.exit(1);
  }

  private long lastNumSeqsIncrease = System.currentTimeMillis();
  private long lastNumSeqs = 0;

  private void updateLastSeqGen() {
    long seqs = SequenceGeneratorStats.steps;
    if (seqs > lastNumSeqs) {
      lastNumSeqsIncrease = System.currentTimeMillis();
      lastNumSeqs = seqs;
    }
  }


  public long lastCovIncrease = System.currentTimeMillis();
  private int lastNumBranches = 0;

  private void updateLastBranchCov() {

    String str =
      Integer.toString(stats.branchesCovered.size()) +
      " " +
      Integer.toString(stats.errors.size());

    stats.covPlot.add(str);
    if (stats.branchesCovered.size() > lastNumBranches) {
      lastCovIncrease = System.currentTimeMillis();
      lastNumBranches = stats.branchesCovered.size();
    }
  }

  /** Clear the display; good to do before printing to System.out. * */
  public void clear() {
    if (progressIntervalMillis == -1)
      return;
    // "display("");" is wrong becuase it leaves the timestamp and writes
    // spaces across the screen.
    String status = UtilMDE.rpad("", progresswidth - 1);
    System.out.print("\r" + status);
    System.out.print("\r"); // return to beginning of line
    System.out.flush();
  }

  /**
   * Displays the current status. Call this if you don't want to wait
   * until the next automatic display.
   */
  public void display() {
    if (progressIntervalMillis == -1)
      return;
    display(message());
  }

  /** Displays the given message. * */
  public void display(String message) {
    if (progressIntervalMillis == -1)
      return;
    String status = message;
    System.out.print((this.outputMode == Mode.SINGLE_LINE_OVERWRITE ? "\r" : Globals.lineSep) + status);
    System.out.flush();
    // System.out.println (status);

//  if (Log.loggingOn) {
//  Log.log("Free memory: "
//  + java.lang.Runtime.getRuntime().freeMemory());
//  Log.log("Used memory: "
//  + (java.lang.Runtime.getRuntime().totalMemory() - java.lang.Runtime
//  .getRuntime().freeMemory()));
//  }
  }
}
