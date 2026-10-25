package randoop.util;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RunnerThread extends Thread {

  // Fields assigned when calling setup(..)
  private @MonotonicNonNull ReflectionCode code;

  // Fields assigned when calling run()
  boolean runFinished;

  // The state of the thread.
  private NextCallMustBe state;

  private enum NextCallMustBe {
    SETUP,
    RUN
  }

  /**
   * Create a new runner thread.
   *
   * @param threadGroup the group for this thread
   */
  @SuppressWarnings("initialization:method.invocation") // needed for CF 3.49.4 and earlier
  RunnerThread(@Nullable ThreadGroup threadGroup) {
    super(threadGroup, "randoop.util.RunnerThread");
    this.code = null;
    this.runFinished = false;
    this.state = NextCallMustBe.SETUP;
    this.setUncaughtExceptionHandler(RandoopUncaughtRunnerThreadExceptionHandler.getHandler());
  }

  public void setup(ReflectionCode code) {
    if (state != NextCallMustBe.SETUP) throw new IllegalStateException();
    if (code == null) throw new IllegalArgumentException("code cannot be null.");
    this.code = code;
    this.state = NextCallMustBe.RUN;
  }

  @Override
  public final void run() {
    if (state != NextCallMustBe.RUN) throw new IllegalStateException();
    runFinished = false;
    executeReflectionCode();
    runFinished = true;
    this.state = NextCallMustBe.SETUP;
  }

  private void executeReflectionCode() throws ReflectionCode.ReflectionCodeException {
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
