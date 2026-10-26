package randoop.generation;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link StateMatcher} that considers every object to be different from all previously-seen
 * objects, so {@link #add} always returns true.
 */
public class EverythingIsDifferentMatcher implements StateMatcher {

  /** The number of calls to {@link #add} on this matcher. */
  private int size = 0;

  /** Creates an EverythingIsDifferentMatcher. */
  public EverythingIsDifferentMatcher() {}

  @Override
  public boolean add(@Nullable Object object) {
    size++;
    return true;
  }

  @Override
  public int size() {
    return size;
  }
}
