package randoop.util;

import java.lang.InterruptedException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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

  /** Global lock to prevent interleaving of progress display messages. */
  public static final Object print_synchro = new Object();

  /**
   * Give up after this many milliseconds, if the generator has not taken a step. That is, if it has
   * not attempted to generate a new test sequence.
   */
  private static int exit_if_no_steps_after_milliseconds = 10 * 1000;

  public enum Mode {
    SINGLE_LINE_OVERWRITE,
    MULTILINE,
    NO_DISPLAY
  }

  private Mode outputMode;

  private RandoopListenerManager listenerMgr;

  private AbstractGenerator generator;

  public ProgressDisplay(
      AbstractGenerator generator, RandoopListenerManager listenerMgr, Mode outputMode) {
    super("randoop.util.ProgressDisplay");
    if (generator == null) {
      throw new IllegalArgumentException("generator is null");
    }
    this.generator = generator;
    this.outputMode = outputMode;
    this.listenerMgr = listenerMgr;
    setDaemon(true);
  }

  public String messageWithoutTime() {
    return "Progress update: steps="
        + generator.num_steps
        + ", test inputs generated="
        + generator.num_sequences_generated
        + ", failing inputs="
        + generator.num_failing_sequences;
  }

  public String messageWithTime() {
    return messageWithoutTime() + "      (" + new Date() + ")";
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
      displayWithTime();
      if (listenerMgr != null) {
        listenerMgr.progressThreadUpdateNotify();
      }

      // Do not enforce a global timeout if we are using threads:
      // if several test threads time out in a row, the global timeout
      // will be exceeded even though nothing is wrong.
      if (!ReflectionExecutor.usethreads) {
        // Check that we're still making progress.  If no new inputs are generated
        // for several seconds, we're probably in an infinite loop, and should exit.
        updateLastStep();
        long now = System.currentTimeMillis();
        if (now - lastNumStepsIncrease > exit_if_no_steps_after_milliseconds) {
          printStackTraceAndExit();
        }
      }

      try {
        sleep(GenInputsAbstract.progressintervalmillis);
      } catch (InterruptedException e) {
        // hmm
      }
    }
  }

  private void printStackTraceAndExit() {

    System.out.println();
    System.out.println();
    System.out.print("*** Randoop has detected no input generation attempts after ");
    System.out.println((exit_if_no_steps_after_milliseconds / 1000) + " seconds.");
    System.out.println("Two possible reasons are:");
    System.out.println(" * Java has run out of memory and is thrashing.");
    System.out.println("   This is likely if the progress update has become progressively slower.");
    System.out.println("   Give Java more memory by running with, say, -Xmx3000m.");
    System.out.println(" * Randoop is executing a sequence that contains nonterminating behavior.");
    System.out.println(
        "   Determine the nonterminating method and fix it or exclude it from Randoop.");
    System.out.println("Last sequence generated:");
    System.out.println();
    System.out.println(AbstractGenerator.currSeq);
    System.out.println();
    System.out.println("Will print all thread stack traces (twice) and exit with code 1.");
    System.out.println();

    printAllStackTraces();
    System.out.println();
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      // if interrupted, just proceed
    }
    printAllStackTraces();

    System.exit(1);
  }

  private void printAllStackTraces() {
    for (Map.Entry<Thread, StackTraceElement[]> trace : Thread.getAllStackTraces().entrySet()) {
      System.out.println("--------------------------------------------------");
      System.out.println("Thread " + trace.getKey().toString());
      System.out.println("Stack trace:");
      StackTraceElement[] elts = trace.getValue();
      for (StackTraceElement elt : elts) {
        System.out.println(elt);
      }
    }
    System.out.println("--------------------------------------------------");
  }

  private long lastNumStepsIncrease = System.currentTimeMillis();
  private long lastNumSteps = 0;

  private void updateLastStep() {
    long seqs = generator.num_steps;
    if (seqs > lastNumSteps) {
      lastNumStepsIncrease = System.currentTimeMillis();
      lastNumSteps = seqs;
    }
  }

  /**
   * Return true iff no progress output should be displayed.
   *
   * @return true iff no progress output should be displayed
   */
  private boolean noProgressOutput() {
    return GenInputsAbstract.progressintervalmillis <= 0
        && GenInputsAbstract.progressintervalsteps <= 0;
  }

  /** Clear the display; good to do before printing to System.out. */
  public void clear() {
    if (noProgressOutput()) return;
    // "display("");" is wrong because it leaves the timestamp and writes
    // spaces across the screen.
    System.out.print("\r" + UtilMDE.rpad("", 199)); // erase about 200 characters of text
    System.out.print("\r"); // return to beginning of line
    System.out.flush();
  }

  /**
   * Displays the current status. Call this if you don't want to wait until the next automatic
   * display.
   */
  public void displayWithTime() {
    if (noProgressOutput()) return;
    display(messageWithTime());
  }

  /**
   * Displays the current status. Call this if you don't want to wait until the next automatic
   * display.
   */
  public void displayWithoutTime() {
    if (noProgressOutput()) return;
    display(messageWithoutTime());
  }

  /**
   * Displays the given message.
   *
   * @param message the message to display
   */
  private void display(String message) {
    if (noProgressOutput()) return;
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
