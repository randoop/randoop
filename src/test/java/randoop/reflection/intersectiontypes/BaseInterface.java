package randoop.reflection.intersectiontypes;

import java.util.List;

/** corresponds to outofboundsfactory */
public interface BaseInterface<T, F> {
  public List<T> m(F f);
}
