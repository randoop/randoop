package randoop.util.predicate;

/**
 * Provides the default implementation of the {@code or} and {@code and} methods
 * of the Predicate interface.
 *
 * @param <T>
 *          the type of object to be tested by the predicate
 */
public abstract class DefaultPredicate<T> implements Predicate<T> {

  @Override
  public Predicate<T> or(Predicate<T> p) {
    return new OrPredicate<>(this, p);
  }

  @Override
  public Predicate<T> and(Predicate<T> p) {
    return new AndPredicate<>(this, p);
  }
}
