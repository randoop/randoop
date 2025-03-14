package randoop.generation;

public class EverythingIsDifferentMatcher implements StateMatcher {

  private int size = 0;

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
