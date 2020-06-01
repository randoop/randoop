package randoop.util;

import java.io.File;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.plumelib.util.DumpHeap;
import org.plumelib.util.UtilPlume;
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

  /**
   * Return the progress message.
   *
   * @param withTime whether to include time and memory usage
   * @return the progress message
   */
  public String message(boolean withTime) {
    return "Progress update: steps="
        + generator.num_steps
        + ", test inputs generated="
        + generator.num_sequences_generated
        + ", failing inputs="
        + generator.num_failing_sequences
        + (withTime
            ? ("      (" + Instant.now() + "     " + Util.usedMemory(false) + "MB used)")
            : "");
  }

  /**
   * Clients should set this variable instead of calling Thread.stop(), which is deprecated.
   * Typically a client calls "display()" before setting this.
   */
  public boolean shouldStop = false;

  @Override
  public void run() {
    long progressInterval = GenInputsAbstract.progressintervalmillis;
    while (true) {
      if (shouldStop) {
        clear();
        return;
      }
      if (progressInterval > 0) {
        display(true);
      }
      if (listenerMgr != null) {
        listenerMgr.progressThreadUpdateNotify();
      }

      // Do not enforce a global timeout if we are using threads:
      // if several test threads time out in a row, the global timeout
      // will be exceeded even though nothing is wrong.
      if (!ReflectionExecutor.usethreads) {
        // Check that we're still making progress.  If no new inputs are generated
        // for several seconds, we're probably in an infinite loop, and should exit.
        updateLastStepTime();
        long now = System.currentTimeMillis();
        if (now - lastStepTime > exit_if_no_steps_after_milliseconds) {
          // TODO: The stack trace of this thread is not interesting.
          // This should print the stack trace of the thread that is running a test.
          exitDueToNoSteps();
        }
      }

      try {
        sleep(progressInterval > 0 ? progressInterval : 1000);
      } catch (InterruptedException e) {
        // hmm
      }
    }
  }

  /** Exit due to too much time without taking a step. */
  // Ideally, on timeout we would terminate step() without shutting down the entire Randoop process.
  // That is not possible in general, unless the test is running in its own thread.
  // Thread.interrupt() just sets the thread's interrupt status.
  // So, tell the user to fix the problem or to run with --usethreads.
  private void exitDueToNoSteps() {
    System.out.println();
    System.out.println();
    System.out.printf(
        "*** Randoop has spent over %s seconds executing the following test.%n",
        exit_if_no_steps_after_milliseconds / 1000);
    System.out.println(
        "See https://randoop.github.io/randoop/manual/index.html#no-input-generation .");
    System.out.println();
    System.out.println(AbstractGenerator.currSeq);
    System.out.println();
    System.out.println("Will dump a heap profile to randoop-slow.hprof.");
    File hprofFile = new File("randoop-slow.hprof");
    if (hprofFile.exists()) {
      hprofFile.delete();
    }
    DumpHeap.dumpHeap("randoop-slow.hprof");
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

  /** When the most recent step completed. */
  private long lastStepTime = System.currentTimeMillis();
  /** The step number of the most recent step. */
  private long lastNumSteps = 0;

  /** Set {@code lastStepTime} to when the most recent step completed. */
  private void updateLastStepTime() {
    long seqs = generator.num_steps;
    if (seqs > lastNumSteps) {
      lastStepTime = System.currentTimeMillis();
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
    System.out.print("\r" + UtilPlume.rpad("", 199)); // erase about 200 characters of text
    System.out.print("\r"); // return to beginning of line
    System.out.flush();
  }

  /**
   * Displays the current status. Call this if you don't want to wait until the next automatic
   * display.
   *
   * @param withTime whether to print time and memory usage
   */
  public void display(boolean withTime) {
    if (noProgressOutput()) return;
    display(message(withTime));
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

    // Log.logPrintf("Free memory: %s%n", Runtime.getRuntime().freeMemory());
    // Log.logPrintf("Used memory: %s%n",
    //    Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
  }
}
