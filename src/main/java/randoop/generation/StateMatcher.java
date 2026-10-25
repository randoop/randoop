package randoop.generation;

public interface StateMatcher {

  /**
   * Adds the given object to this matcher, if it does not match an object already in this matcher.
   *
   * @param object the object to add
   * @return true if the object was added; false if it matches an object already in this matcher or
   *     if determining whether it matches threw an exception
   */
  boolean add(Object object);

  int size();
}
