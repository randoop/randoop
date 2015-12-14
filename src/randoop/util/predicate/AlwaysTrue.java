package randoop.util.predicate;

/**
 * A predicate that always returns true.
 *
 * @param <T>  the type of object to be tested
 */
public class AlwaysTrue<T> extends DefaultPredicate<T> {

  /**
   * {@inheritDoc}
   * @return true, always
   */
  @Override
  public boolean test(T t) {
    return true;
  }

  /**
   * {@inheritDoc}
   * @return this predicate, since the or-else with this object is always true
   */
  @Override
  public Predicate<T> or(Predicate<T> p) {
    return this;
  }
  
  /**
   * {@inheritDoc}
   * @return the second predicate, since the other predicate must be true for 
   * the and-also to be true.
   */
  @Override
  public Predicate<T> and(Predicate<T> p) {
    return p;
  }
}
