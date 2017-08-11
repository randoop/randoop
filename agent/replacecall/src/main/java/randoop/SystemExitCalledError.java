package randoop;

/**
 * Exception to be thrown by default replacement for {@code System.exit()}.
 *
 * @see randoop.mock.java.lang.System
 */
public class SystemExitCalledError extends Error {

  /** status value for System.exit() call */
  private final int status;

  public SystemExitCalledError(int status) {
    super(String.format("System exit(%d) ignored", status));
    this.status = status;
  }
}
