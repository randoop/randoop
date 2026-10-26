package randoop.generation;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Records a collection of runtime values and determines whether a given value is new, according to
 * the matcher's notion of equivalence.
 */
public interface StateMatcher {

  /**
   * Records the given value in this matcher.
   *
   * @param object the value to record, or null
   * @return true if the value is new (not equivalent to any previously-recorded value); false if
   *     the value is not new, or if this matcher cannot determine whether it is new (for example,
   *     because the value's {@code equals} or {@code hashCode} method throws an exception)
   */
  boolean add(@Nullable Object object);

  /**
   * Returns the number of distinct values (according to this matcher's notion of equivalence)
   * recorded by this matcher.
   *
   * @return the number of distinct values recorded by this matcher
   */
  int size();
}
