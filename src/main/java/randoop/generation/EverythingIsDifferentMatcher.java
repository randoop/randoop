package randoop.generation;

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
  public boolean add(Object object) {
    size++;
    return true;
  }

  @Override
  public int size() {
    return size;
  }
}
