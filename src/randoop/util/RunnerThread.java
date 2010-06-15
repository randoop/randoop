package randoop.util;

public class RunnerThread extends Thread {

  // Fields assigned when calling setup(..)
  private ReflectionCode code;

  // Fields assigned when calling run()
  public boolean runFinished;
  public Throwable exceptionThrown;

  // The state of the thread.
  private NextCallMustBe state;

  private enum NextCallMustBe { SETUP, RUN }

  /**
   * Create a new runner thread.
   * 
   * @param threadGroup
   */
  public RunnerThread(ThreadGroup threadGroup) {
    super(threadGroup, "");
    this.code = null;
    this.runFinished = false;
    this.exceptionThrown = null;
    this.state = NextCallMustBe.SETUP;
    this.setUncaughtExceptionHandler(RandoopUncaughtRunnerThreadExceptionHandler.getHandler());
  }

  public void setup(ReflectionCode code) {
    if (state != NextCallMustBe.SETUP) throw new IllegalArgumentException();
    if (code == null) throw new IllegalArgumentException("code cannot be null.");
    this.code = code;
    this.state = NextCallMustBe.RUN;
  }

  @Override
  public final void run() {
    if (state != NextCallMustBe.RUN) throw new IllegalArgumentException();
    runFinished = false;
    executeReflectionCode();
    runFinished = true;
    this.state = NextCallMustBe.SETUP;
  }

  private void executeReflectionCode() {
    try {
      code.runReflectionCode();
      // exceptionThrown remains null.
    } catch (ThreadDeath e) {// can't stop these guys
      throw e;
    } catch (ReflectionCode.NotCaughtIllegalStateException e) {// bug in randoop code
      throw e;
    } catch (Throwable e) {
      if (e instanceof java.lang.reflect.InvocationTargetException)
        e = e.getCause();
      exceptionThrown = e;
    }
  }
}
