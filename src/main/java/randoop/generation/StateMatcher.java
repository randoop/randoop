package randoop.generation;

/**
 * Records a collection of runtime values and determines whether a given value is new, according to
 * the matcher's notion of equivalence.
 */
public interface StateMatcher {

  /**
   * Records the given value in this matcher.
   *
   * @param object the value to record
   * @return true if the value is new (not equivalent to any previously-recorded value), false
   *     otherwise
   */
  boolean add(Object object);

  /**
   * Returns the number of values recorded by this matcher.
   *
   * @return the number of values recorded by this matcher
   */
  int size();
}
