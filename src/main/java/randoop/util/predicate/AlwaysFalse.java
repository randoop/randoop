package randoop.util.predicate;

/**
 * Predicate that always return false.
 *
 * @param <T>
 *          the type of the object to test
 */
public class AlwaysFalse<T> extends DefaultPredicate<T> {

  /**
   * {@inheritDoc}
   *
   * @return false, always
   */
  @Override
  public boolean test(T t) {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @return the second predicate, since whether or-else is true determined by
   *         second predicate
   */
  @Override
  public Predicate<T> or(Predicate<T> p) {
    return p;
  }

  /**
   * {@inheritDoc}
   *
   * @return this object since this object always false in and-also expression
   */
  @Override
  public Predicate<T> and(Predicate<T> p) {
    return this;
  }
}
