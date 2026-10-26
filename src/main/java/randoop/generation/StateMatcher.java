package randoop.generation;

/**
 * A set of objects, in which an object is not added if it matches an object already in the set. The
 * notion of matching depends on the implementation.
 */
public interface StateMatcher {

  /**
   * Adds the given object to this matcher, if it does not match an object already in this matcher.
   *
   * @param object the object to add
   * @return true if the object was added; false if it matches an object already in this matcher or
   *     if determining whether it matches threw an exception
   */
  boolean add(Object object);

  /**
   * Returns the number of objects in this matcher.
   *
   * @return the number of objects in this matcher
   */
  int size();
}
