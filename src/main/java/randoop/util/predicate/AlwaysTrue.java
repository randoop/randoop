package randoop.util.predicate;

import java.util.function.Predicate;

/**
 * A predicate that always returns true.
 *
 * @param <T> the type of object to be tested
 */
public class AlwaysTrue<T> implements Predicate<T> {

  /**
   * {@inheritDoc}
   *
   * @return true, always
   */
  @Override
  public boolean test(T t) {
    return true;
  }
}
