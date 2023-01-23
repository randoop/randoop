package randoop.util;

import org.plumelib.util.StringsPlume;

/**
 * Wraps a method or constructor together with its arguments. Can be run only once. {@link
 * #hasRun()} indicates whether it has been run.
 *
 * <p>Implemented by parts of Randoop that want to execute reflection code via ReflectionExecutor.
 */
public abstract class ReflectionCode {

  /** Has this started execution? */
  private boolean hasStarted;

  /** Has this been executed already? */
  private boolean hasRun;

  // Before runReflectionCodeRaw is executed, both of these fields are null. After
  // runReflectionCodeRaw is executed, if exceptionThrown is null, then retval is the returned value
  // (which might be null).
  /** The value yielded by execution. */
  protected Object retval;
  /** The exception thrown by execution. */
  protected Throwable exceptionThrown;

  public final boolean hasStarted() {
    return hasStarted;
  }

  public final boolean hasRun() {
    return hasRun;
  }

  protected final void setHasStarted() {
    if (hasStarted) {
      throw new ReflectionCodeException("cannot run this twice");
    }
    hasStarted = true;
  }

  protected final void setHasRun() {
    if (hasRun) {
      throw new ReflectionCodeException("cannot run this twice");
    }
    hasRun = true;
  }

  /**
   * Runs the reflection code that this object represents.
   *
   * <ol>
   *   <li>This method calls {@link #runReflectionCodeRaw()} to perform the actual work. {@link
   *       #runReflectionCodeRaw()} sets the {@code retVal} or {@code exceptionThrown} field, or
   *       throws an exception if there is a bug in Randoop.
   * </ol>
   *
   * @throws ReflectionCodeException if execution results in conflicting error and success states;
   *     this results from a bug in Randoop
   */
  public final void runReflectionCode() throws ReflectionCodeException {
    this.setHasStarted();
    runReflectionCodeRaw();
    this.setHasRun();
  }

  /**
   * Execute the reflection code. All Randoop implementation errors must be thrown as
   * ReflectionCodeException because everything else is caught.
   *
   * @throws ReflectionCodeException if execution results in conflicting error and success states;
   *     this results from a bug in Randoop
   */
  protected abstract void runReflectionCodeRaw() throws ReflectionCodeException;

  public Object getReturnValue() {
    if (!hasRun()) {
      throw new IllegalStateException("run first, then ask");
    }
    return retval;
  }

  public Throwable getExceptionThrown() {
    if (!hasRun()) {
      throw new IllegalStateException("run first, then ask");
    }
    return exceptionThrown;
  }

  /**
   * A suffix to be called by toString().
   *
   * @return the status of the command
   */
  protected String status() {
    if (!hasStarted() && !hasRun()) {
      return " not run yet";
    } else if (hasStarted() && !hasRun()) {
      return " failed to run";
    } else if (!hasStarted() && hasRun()) {
      return " ILLEGAL STATE";
    } else if (exceptionThrown == null) {
      return " returned: " + StringsPlume.toStringAndClass(retval);
    } else {
      return " threw: " + exceptionThrown;
    }
  }

  /** Indicates a bug in the ReflectionCode class. */
  static final class ReflectionCodeException extends IllegalStateException {
    private static final long serialVersionUID = -7508201027241079866L;

    ReflectionCodeException(String msg) {
      super(msg);
    }

    ReflectionCodeException(String msg, Throwable cause) {
      super(msg, cause);
    }

    ReflectionCodeException(Throwable cause) {
      super(cause);
    }
  }
}
