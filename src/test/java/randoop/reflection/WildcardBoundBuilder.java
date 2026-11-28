package randoop.reflection;

/**
 * Test input based on issue #1889: a type parameter whose bound is the declaring type applied to a
 * wildcard.
 *
 * @param <T> the type of builder
 */
public interface WildcardBoundBuilder<T extends WildcardBoundBuilder<?>> {
  /**
   * Returns a new builder.
   *
   * @return a new builder
   */
  WildcardBoundBuilder<T> create();
}
