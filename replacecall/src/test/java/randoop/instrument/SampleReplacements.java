package randoop.instrument;

import java.util.AbstractMap;
import java.util.Map;

/** Replacement methods for the replacement files used by {@link ReplacementFileTest}. */
public final class SampleReplacements {

  /** Do not instantiate. */
  private SampleReplacements() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /** A type that contains a nested class. */
  public interface Middle {
    /** A doubly-nested class. */
    class Inner {
      /**
       * Replacement for the {@code Inner()} constructor.
       *
       * @return a new Inner
       */
      public static Inner make() {
        return new Inner();
      }
    }
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
   * Replacement for the {@code AbstractMap.SimpleEntry(Map.Entry)} constructor.
   *
   * @param entry the entry to copy
   * @return a new entry with the key and value of {@code entry}
   */
  public static AbstractMap.SimpleEntry<Object, Object> newSimpleEntry(Map.Entry<?, ?> entry) {
    return new AbstractMap.SimpleEntry<>(entry);
  }
}
