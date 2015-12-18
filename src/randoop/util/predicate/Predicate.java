package randoop.util.predicate;

/**
 * A predicate interface because we are still in Java 7.
 *
 * @param <T>  the type over which predicates operate
 */
public interface Predicate<T> {
   
  /**
   * Determine whether object satisfies this predicate.
   * 
   * @param t  the object to test
   * @return true if predicate holds for t, and false otherwise
   */
  boolean test(T t);
  
  /**
   * Creates a new predicate that performs an or-else operator on this
   * and the given predicate.
   *  
   * @param p  the second predicate to check
   * @return a predicate that returns true if this predicate is true or the second
   * predicate is true, and false if neither is true
   */
  Predicate<T> or(Predicate<T> p);
  
  /**
   * Creates a new predicate that performs an and-also operator on this and the
   * given predicate.
   * 
   * @param q  the second predicate to check
   * @return a predicate that returns true when this and the second predicate
   * return true, and false otherwise
   */
  Predicate<T> and(Predicate<T> q);

}
