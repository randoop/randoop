package randoop;

/**
 * Exception to be thrown by default replacement for {@code System.exit()}.
 *
 * @see randoop.mock.System
 */
public class SystemExitCalledError extends Error {

  /** status value for System exit call */
  private final int status;

  public SystemExitCalledError(int status) {
    super(String.format("System exit with status %d ignored%n", status));
    this.status = status;
  }
}
