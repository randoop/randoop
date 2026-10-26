package randoop.reflection.intersectiontypes;

import java.util.List;

/** Corresponds to outofboundsfactory. */
public interface BaseInterface<T, F> {
  public List<T> m(F f);
}
