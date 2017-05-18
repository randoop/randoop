package randoop.mock;

import randoop.SystemExitCalledError;

/**
 * Default replacement for {@link randoop.instrument.MapCallsAgent} Replacement should be specified
 * in {@code "resources/replacements.txt"}
 */
public class System {
  public static void exit(int status) {
    String message = String.format("System exit with status %d ignored%n", status);
    throw new SystemExitCalledError(message);
  }
}
