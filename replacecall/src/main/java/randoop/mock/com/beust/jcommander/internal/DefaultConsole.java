package randoop.mock.com.beust.jcommander.internal;

/**
 * Class of mock methods for {@code com.beust.jcommander.internal.DefaultConsole}. The mock methods
 * do not read from standard input.
 *
 * <p>This is not a default replacement, because JCommander is not part of the JDK. To use it, put
 * this line in a replacement file:
 *
 * <pre>
 * com.beust.jcommander.internal.DefaultConsole randoop.mock.com.beust.jcommander.internal.DefaultConsole
 * </pre>
 */
public final class DefaultConsole {

  /** Do not instantiate. */
  private DefaultConsole() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * Mock of {@code com.beust.jcommander.internal.DefaultConsole.readPassword(boolean)}. Returns an
   * empty password without reading from standard input.
   *
   * @param console the receiver (ignored)
   * @param echoInput whether to echo the input (ignored)
   * @return an empty char array
   */
  public static char[] readPassword(
      com.beust.jcommander.internal.DefaultConsole console, boolean echoInput) {
    return new char[0];
  }
}
