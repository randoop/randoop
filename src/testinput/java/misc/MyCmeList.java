package misc;

import java.util.ArrayList;
import java.util.List;

/** This class, when tested, will throw a ConcurrentModificationException in equals(). */
public class MyCmeList {

  private List<String> delegate;

  public MyCmeList() {
    this.delegate = new ArrayList<>();
  }

  public MyCmeList(List<String> delegate) {
    if (delegate == null) {
      throw new IllegalArgumentException("null not allowed");
    }
    this.delegate = delegate;
  }

  public boolean add(String e) {
    return delegate.add(e);
  }

  public void clear() {
    delegate.clear();
  }

  public MyCmeList subList(int fromIndex, int toIndex) {
    return new MyCmeList(delegate.subList(fromIndex, toIndex));
  }

  public boolean equals(Object o) {
    if (!(o instanceof MyCmeList)) {
      return false;
    }
    MyCmeList that = (MyCmeList) o;
    return this.delegate.equals(that.delegate);
  }

  public int hashCode() {
    return delegate.hashCode();
  }
}
