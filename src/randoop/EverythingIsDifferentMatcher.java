package randoop;

public class EverythingIsDifferentMatcher implements StateMatcher {

  int size = 0;

  public boolean add(Object object) {
    size++;
    return true;
  }

  public int size() {
    return size;
  }

}
