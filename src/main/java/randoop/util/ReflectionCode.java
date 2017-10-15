package randoop.util;

import java.lang.reflect.InvocationTargetException;
import randoop.util.RandoopSecurityManager.Status;

/**
 * Wraps a method or constructor together with its arguments, ready for execution. Can be run only
 * once.
 *
 * <p>Implemented by parts of Randoop that want to execute reflection code via ReflectionExecutor.
 */
public abstract class ReflectionCode {

  /** has this been executed already */
  protected boolean runAlready;

  // Before runReflectionCodeRaw is executed, both of these fields are null. After
  // runReflectionCodeRaw is executed, exactly one of these fields is null (unless
  // runReflectionCodeRaw itself threw an exception, in which case both fields remain null).
  protected Object retval;
  protected Throwable exceptionThrown;

  /**
   * Runs the reflection code that this object represents, but first, if System.getSecurityManager()
   * returns a RandoopSecurityManager, this method sets the security manager's status to ON. Before
   * exiting, this method sets the security manager's status to its status before this call.
   *
   * @throws InvocationTargetException if executed code throws an exception
   * @throws IllegalAccessException if the executed code involves inaccessible method or constructor
   * @throws InstantiationException if unable to create a new instance
   */
  public final void runReflectionCode()
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
          NotCaughtIllegalStateException {

    if (hasRunAlready()) {
      throw new NotCaughtIllegalStateException("cannot run this twice " + this);
    }
    this.setRunAlready();

    // The following few lines attempt to find out if there is a
    // RandoopSecurityManager installed, and if so, record its status.
    RandoopSecurityManager randoopsecurity = null;
    RandoopSecurityManager.Status oldStatus = null;

    SecurityManager security = System.getSecurityManager();
    if (security != null && security instanceof RandoopSecurityManager) {
      randoopsecurity = (RandoopSecurityManager) security;
      oldStatus = randoopsecurity.status;
      randoopsecurity.status = Status.ON;
    }

    // At this point, this is the state of the method.
    assert Util.iff(
        security != null && security instanceof RandoopSecurityManager,
        randoopsecurity != null && oldStatus != null);

    try {

      runReflectionCodeRaw();

      // Not checked if runReflectionCodeRaw throws an exception, but this can't go in a finally
      // block because exceptions thrown in a finally block have no effect.
      if (retval != null && exceptionThrown != null) {
        throw new NotCaughtIllegalStateException("cannot have both retval and exception not null");
      }

    } finally {

      // Before exiting, restore the RandoopSecurityManager's status to its
      // original status, if such a manager was installed.
      if (randoopsecurity != null) {
        assert oldStatus != null;
        randoopsecurity.status = oldStatus;
      }
    }
  }

  /**
   * Execute the reflection code. All internal exceptions must be thrown as
   * NotCaughtIllegalStateException because everything else is caught.
   *
   * @throws InstantiationException if unable to create a new instance
   * @throws IllegalAccessException if executed code involves inaccessible method
   * @throws InvocationTargetException if executed code throws an exception
   * @throws NotCaughtIllegalStateException if execution results in conflicting error and success
   *     states
   */
  protected abstract void runReflectionCodeRaw()
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
          NotCaughtIllegalStateException;

  protected final void setRunAlready() {
    // Called from inside runReflectionCode, so use NotCaughtIllegalStateException.
    if (runAlready) {
      throw new NotCaughtIllegalStateException("cannot call this twice");
    }
    runAlready = true;
  }

  public Object getReturnValue() {
    if (!hasRunAlready()) {
      throw new IllegalStateException("run first, then ask");
    }
    return retval;
  }

  public Throwable getExceptionThrown() {
    if (!hasRunAlready()) {
      throw new IllegalStateException("run first, then ask");
    }
    return exceptionThrown;
  }

  public final boolean hasRunAlready() {
    return runAlready;
  }

  /*
   * See comment in runReflectionCode
   */
  static final class NotCaughtIllegalStateException extends IllegalStateException {
    private static final long serialVersionUID = -7508201027241079866L;

    NotCaughtIllegalStateException(String msg) {
      super(msg);
    }
  }
}
