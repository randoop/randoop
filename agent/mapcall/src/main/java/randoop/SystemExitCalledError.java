package randoop;

/**
 * Exception to be thrown by default replacement for {@code System.exit()}.
 *
 * @see randoop.mock.System
 */
public class SystemExitCalledError extends Error {
  public SystemExitCalledError(String message) {
    super(message);
  }
}
