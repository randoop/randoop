package randoop.generation;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A {@link StateMatcher} that considers two objects to be the same state if they are equal
 * according to their {@code equals} method.
 */
public class EqualsMethodMatcher implements StateMatcher {

  /** The distinct objects (according to {@code equals}) that have been added. */
  private final Set<Object> cache = new LinkedHashSet<>();

  /** Creates a EqualsMethodMatcher. */
  public EqualsMethodMatcher() {}

  @Override
  public boolean add(Object object) {
    try {
      return this.cache.add(object);
    } catch (Throwable e) {
      // This could happen, because we're actually running code under test.
      return false;
    }
  }

  @Override
  public int size() {
    return this.cache.size();
  }
}
