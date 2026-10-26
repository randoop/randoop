package randoop.mock.com.beust.jcommander.internal;

/**
 * Class of mock methods for {@code com.beust.jcommander.internal.Console}. The mock methods do not
 * read from standard input.
 *
 * <p>JCommander calls {@code readPassword} through the {@code Console} interface, so the
 * replacement is for the interface method rather than for an implementation such as {@code
 * DefaultConsole}.
 *
 * <p>This is not a default replacement, because JCommander is not part of the JDK. To use it, put
 * this line in a replacement file:
 *
 * <pre>com.beust.jcommander.internal.Console randoop.mock.com.beust.jcommander.internal.Console
 * </pre>
 */
public final class Console {

  /** Do not instantiate. */
  private Console() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * Mock of {@code com.beust.jcommander.internal.Console.readPassword(boolean)}. Returns an empty
   * password without reading from standard input.
   *
   * @param console the receiver (ignored)
   * @param echoInput whether to echo the input (ignored)
   * @return an empty char array
   */
  public static char[] readPassword(
      com.beust.jcommander.internal.Console console, boolean echoInput) {
    return new char[0];
  }
}
