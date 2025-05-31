package randoop.mock.java.lang;

import randoop.SystemExitCalledError;

/**
 * Default replacement for {@link randoop.instrument.ReplaceCallAgent}. Replacement should be
 * specified in {@code "resources/replacements.txt"}
 */
@SuppressWarnings("JavaLangClash")
public class System {

  /**
   * Default mock for {@code System.exit(status)}. Throws an exception to allow Randoop to generate
   * tests that acknowledge that exit occurs.
   *
   * @param status the exit status
   * @throws SystemExitCalledError with the status
   */
  @SuppressWarnings("DoNotCallSuggester")
  public static void exit(int status) {
    throw new SystemExitCalledError(status);
  }
}
