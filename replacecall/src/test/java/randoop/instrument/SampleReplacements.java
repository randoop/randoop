package randoop.instrument;

import java.util.AbstractMap;

/** Replacement methods for the replacement files used by {@link ReplacementFileTest}. */
public final class SampleReplacements {

  /** Do not instantiate. */
  private SampleReplacements() {
    throw new UnsupportedOperationException("Do not instantiate");
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
}
