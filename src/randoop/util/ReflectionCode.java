package randoop.util;

import java.lang.reflect.InvocationTargetException;

import randoop.util.RandoopSecurityManager.Status;

/**
 * Implemented by parts of randoop that want to execute reflection
 * code via ReflectionExecutor.
 *
 */
public abstract class ReflectionCode {

  private boolean runAlready;        /** has this been executed already*/

  /**
   * Runs the reflection code that this object represents, but first, if
   * System.getSecurityManager() returns a RandoopSecurityManager, this method
   * sets the security manager's status to ON. Before exiting, this method
   * sets the security manager's status to its status before this call.
   */
  public final void runReflectionCode() throws InstantiationException, IllegalAccessException,
  InvocationTargetException, NotCaughtIllegalStateException {

    // The following few lines attempt to find out if there is a
    // RandoopSecurityManager installed, and if so, record its status.
    RandoopSecurityManager randoopsecurity = null;
    RandoopSecurityManager.Status oldStatus = null;

    SecurityManager security = System.getSecurityManager();
    if (security != null && security instanceof RandoopSecurityManager) {
      randoopsecurity = (RandoopSecurityManager)security;
      oldStatus = randoopsecurity.status;
      randoopsecurity.status = Status.ON;
    }

    // At this point, this is the state of the method.
    assert Util.iff(security != null && security instanceof RandoopSecurityManager,
        randoopsecurity != null && oldStatus != null);

    try {

      runReflectionCodeRaw();

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
   * Executed the reflection code. All internal exceptions must be thrown as
   * NotCaughtIllegalStateException because everything else is caught.
   *
   */
  protected abstract void runReflectionCodeRaw() throws InstantiationException, IllegalAccessException,
  InvocationTargetException, NotCaughtIllegalStateException;



  protected final void setRunAlready() {
    // called from inside runReflectionCode, so use NotCaughtIllegalStateException
    if (runAlready) throw new NotCaughtIllegalStateException("cannot call this twice");
    runAlready= true;
  }

  public abstract Object getReturnVariable();

  public abstract Throwable getExceptionThrown();

  public final boolean hasRunAlready() {
    return runAlready;
  }

  /*
   * See comment in runReflectionCode
   */
  static final class NotCaughtIllegalStateException extends IllegalStateException {
    private static final long serialVersionUID = -7508201027241079866L;
    NotCaughtIllegalStateException(String msg) { super(msg); }
  }
}
