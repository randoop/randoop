package randoop.util;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A thread that runs a {@link ReflectionCode}. To run the code in this thread, call {@link #setup}
 * and then {@link #start}. A thread can be started only once.
 *
 * <p>Calls to {@link #setup} and {@link #run} must alternate. {@link #start} calls {@link #run} in
 * this thread; a direct call to {@link #run} executes the code in the calling thread.
 */
public class RunnerThread extends Thread {

  /** The code to run. Set by {@link #setup}. */
  private @MonotonicNonNull ReflectionCode code;

  /** True if the most recent call to {@link #run} has completed normally. Set by {@link #run}. */
  /*package*/ boolean runFinished;

  /** The state of the thread: which method must be called next. */
  private NextCallMustBe state;

  /** Which method must be called next on a {@code RunnerThread}. */
  private enum NextCallMustBe {
    /** {@link RunnerThread#setup} must be called next. */
    SETUP,
    /** {@link RunnerThread#run} must be called next. */
    RUN
  }

  /**
   * Create a new runner thread.
   *
   * @param threadGroup the group for this thread
   */
  /*package*/
  @SuppressWarnings({
    "initialization:method.invocation", // needed for CF 3.49.4 and earlier
    "PMD.ConstructorCallsOverridableMethod"
  })
  RunnerThread(@Nullable ThreadGroup threadGroup) {
    super(threadGroup, "randoop.util.RunnerThread");
    this.code = null;
    this.runFinished = false;
    this.state = NextCallMustBe.SETUP;
    this.setUncaughtExceptionHandler(RandoopUncaughtRunnerThreadExceptionHandler.getHandler());
  }

  /**
   * Sets the code that the next call to {@link #run} will execute.
   *
   * @param code the code to run
   * @throws IllegalStateException if {@link #run} must be called next
   * @throws IllegalArgumentException if {@code code} is null
   */
  public void setup(ReflectionCode code) {
    if (state != NextCallMustBe.SETUP) {
      throw new IllegalStateException();
    }
    if (code == null) {
      throw new IllegalArgumentException("code cannot be null.");
    }
    this.code = code;
    this.state = NextCallMustBe.RUN;
  }

  @Override
  public final void run() {
    if (state != NextCallMustBe.RUN) {
      throw new IllegalStateException();
    }
    runFinished = false;
    executeReflectionCode();
    runFinished = true;
    this.state = NextCallMustBe.SETUP;
  }

  /** Runs the {@link ReflectionCode} that this thread was set up with. */
  private void executeReflectionCode() {
    code.runReflectionCode();
  }

  /**
   * Returns the ReflectionCode that is being, or was, run.
   *
   * @return the ReflectionCode that is being, or was, run
   */
  public ReflectionCode getCode() {
    return code;
  }
}
