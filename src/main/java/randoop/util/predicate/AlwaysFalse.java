package randoop.util.predicate;

import java.util.function.Predicate;

/**
 * Predicate that always return false.
 *
 * @param <T> the type of the object to test
 */
public class AlwaysFalse<T> implements Predicate<T> {

  /**
   * {@inheritDoc}
   *
   * @return false, always
   */
  @Override
  public boolean test(T t) {
    return false;
  }
}
