package randoop.util.predicate;

import java.util.function.Predicate;

/**
 * A predicate that always returns false.
 *
 * @param <T> the type of the object to test
 */
public class AlwaysFalse<T> implements Predicate<T> {

  /** Creates a AlwaysFalse. */
  public AlwaysFalse() {}

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
