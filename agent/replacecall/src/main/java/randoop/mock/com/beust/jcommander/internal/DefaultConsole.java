package randoop.mock.com.beust.jcommander.internal;

public class DefaultConsole {

  /**
   * Mock of com.beust.jcommander.internal.DefaultConsole.readPassword(boolean). This method does
   * nothing and returns an empty char array.
   *
   * @param instance the instance of DefaultConsole (ignored)
   * @param echoInput the echoInput parameter (ignored)
   * @return an empty char array
   */
  public static char[] readPassword(
      com.beust.jcommander.internal.Console instance, boolean echoInput) {
    return new char[0];
  }
}
