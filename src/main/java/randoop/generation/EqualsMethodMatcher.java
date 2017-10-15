package randoop.generation;

import java.util.LinkedHashSet;
import java.util.Set;

public class EqualsMethodMatcher implements StateMatcher {

  private final Set<Object> cache = new LinkedHashSet<>();

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
