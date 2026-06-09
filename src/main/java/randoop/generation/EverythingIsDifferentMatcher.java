package randoop.generation;

public class EverythingIsDifferentMatcher implements StateMatcher {

  /** The number of calls to {@link #add} on this matcher. */
  private int size = 0;

  /** Creates a EverythingIsDifferentMatcher. */
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
