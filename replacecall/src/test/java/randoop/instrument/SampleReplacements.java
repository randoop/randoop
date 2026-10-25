package randoop.instrument;

import java.util.AbstractMap;
import java.util.Random;

/** Replacement methods for the replacement files used by {@link ReplacementFileTest}. */
public final class SampleReplacements {

  /** Do not instantiate. */
  private SampleReplacements() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /** A type that contains a nested class. */
  public interface Middle {
    /** A doubly-nested class. */
    class Inner {}
  }

  /**
   * Replacement for the {@code StringBuilder(String)} constructor.
   *
   * @param s the initial contents
   * @return a new StringBuilder with the given contents
   */
  public static StringBuilder newStringBuilder(String s) {
    return new StringBuilder(s);
  }

  /**
   * Replacement for the {@code AbstractMap.SimpleEntry(Object, Object)} constructor.
   *
   * @param key the key
   * @param value the value
   * @return a new entry with the given key and value
   */
  public static AbstractMap.SimpleEntry<Object, Object> newSimpleEntry(Object key, Object value) {
    return new AbstractMap.SimpleEntry<>(key, value);
  }

  /**
   * A private method, which cannot be a replacement.
   *
   * @return a new Random
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used in a replacement file
  private static Random privateNewRandom() {
    return new Random(0);
  }

  /**
   * A replacement for {@code String.length()} with the wrong return type.
   *
   * @param s a string
   * @return the length of the string
   */
  public static long badLength(String s) {
    return s.length();
  }
}
