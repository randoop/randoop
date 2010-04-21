package randoop.util;

import java.util.List;

public abstract class SimpleList<T> {

  public abstract int size();

  public abstract T get(int index);

  public final boolean isEmpty(){
    return size() == 0;
  }

  /**
   * Returns a java.util.List version of this list.
   */
  public abstract List<T> toJDKList();
}
