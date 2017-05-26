package randoop.util;

import java.util.Date;
import java.util.Map;
import plume.UtilMDE;
import randoop.Globals;
import randoop.generation.AbstractGenerator;
import randoop.generation.RandoopListenerManager;
import randoop.main.GenInputsAbstract;

/** Modified from Daikon.FileIOProgress. */
// TODO: Split this class into two: one is responsible for
// displaying information at a regular interval, and a
// second class is responsible for monitoring for progress
// and terminating Randoop if it appears the tool is hanging.
// Currently this class does both things.
public class ProgressDisplay extends Thread {

  /** Lock so that unfortunate interleaving of this printing can be avoided */
  public static final Object print_synchro = new Object();

  private static int progresswidth = 170;

  private static int exit_if_no_new_sequences_after_mseconds = 10000;

  public enum Mode {
    SINGLE_LINE_OVERWRITE,
    MULTILINE,
    NO_DISPLAY
  }

  private Mode outputMode;

  private RandoopListenerManager listenerMgr;

  private AbstractGenerator generator;

  public ProgressDisplay(
      AbstractGenerator generator,
      RandoopListenerManager listenerMgr,
      Mode outputMode,
      int progressWidth) {
    super("randoop.util.ProgressDisplay");
    if (generator == null) {
      throw new IllegalArgumentException("generator is null");
    }
    this.generator = generator;
    this.outputMode = outputMode;
    this.listenerMgr = listenerMgr;
    ProgressDisplay.progresswidth = progressWidth;
    setDaemon(true);
  }

  public String message() {
    return "Progress update: test inputs generated="
        + generator.num_sequences_generated
        + ", failing inputs="
        + generator.num_failing_sequences
        + "      ("
        + new Date()
        + ")";
  }

  /**
   * Clients should set this variable instead of calling Thread.stop(), which is deprecated.
   * Typically a client calls "display()" before setting this.
   */
  public boolean shouldStop = false;

  @Override
  public void run() {
    while (true) {
      if (shouldStop) {
        clear();
        return;
      }
      display();
      if (listenerMgr != null) {
        listenerMgr.progressThreadUpdateNotify();
      }

      // Do not enforce a global timeout if we are using threads:
      // if several test threads time out in a row, the global timeout
      // will be exceeded even though nothing is wrong.
      if (!ReflectionExecutor.usethreads) {
        // Check that we're still doing progress. If no new inputs
        // generated for several seconds, we're probably in an infinite
        // loop, and should exit.
        updateLastSeqGen();
        long now = System.currentTimeMillis();
        if (now - lastNumSeqsIncrease > exit_if_no_new_sequences_after_mseconds) {
          printStackTraceAndExit();
        }
      }

      try {
        sleep(GenInputsAbstract.progressinterval);
      } catch (InterruptedException e) {
        // hmm
      }
    }
  }

  private void printStackTraceAndExit() {

    System.out.println();
    System.out.print("*** Randoop has detected no input generation attempts after ");
    System.out.println(exit_if_no_new_sequences_after_mseconds + " milliseconds.");
    System.out.println("This indicates Randoop may be executing a sequence");
    System.out.println("that leads to nonterminating behavior.");
    System.out.println("Last sequence generated:");
    System.out.println();
    System.out.println(AbstractGenerator.currSeq);
    System.out.println();
    System.out.println("Will print all thread stack traces and exit with code 1.");

    for (Map.Entry<Thread, StackTraceElement[]> trace : Thread.getAllStackTraces().entrySet()) {
      System.out.println("--------------------------------------------------");
      System.out.println("Thread " + trace.getKey().toString());
      System.out.println("Stack trace:");
      StackTraceElement[] elts = trace.getValue();
      for (StackTraceElement elt : elts) {
        System.out.println(elt);
      }
    }
    System.exit(1);
  }

  private long lastNumSeqsIncrease = System.currentTimeMillis();
  private long lastNumSeqs = 0;

  private void updateLastSeqGen() {
    long seqs = generator.num_steps;
    if (seqs > lastNumSeqs) {
      lastNumSeqsIncrease = System.currentTimeMillis();
      lastNumSeqs = seqs;
    }
  }

  /** Clear the display; good to do before printing to System.out. */
  public void clear() {
    if (GenInputsAbstract.progressinterval == -1) return;
    // "display("");" is wrong because it leaves the timestamp and writes
    // spaces across the screen.
    String status = UtilMDE.rpad("", progresswidth - 1);
    System.out.print("\r" + status);
    System.out.print("\r"); // return to beginning of line
    System.out.flush();
  }

  /**
   * Displays the current status. Call this if you don't want to wait until the next automatic
   * display.
   */
  public void display() {
    if (GenInputsAbstract.progressinterval == -1) return;
    display(message());
  }

  /**
   * Displays the given message.
   *
   * @param message the message to display
   */
  private void display(String message) {
    if (GenInputsAbstract.progressinterval == -1) return;
    synchronized (print_synchro) {
      System.out.print(
          (this.outputMode == Mode.SINGLE_LINE_OVERWRITE ? "\r" : Globals.lineSep) + message);
      System.out.flush();
    }
    // System.out.println (status);

    // if (Log.loggingOn) {
    // Log.log("Free memory: "
    // + java.lang.Runtime.getRuntime().freeMemory());
    // Log.log("Used memory: "
    // + (java.lang.Runtime.getRuntime().totalMemory() - java.lang.Runtime
    // .getRuntime().freeMemory()));
    // }
  }
}
