package randoop.reflection.intersectiontypes;

import java.util.ArrayList;
import java.util.List;

/** Created by bjkeller on 12/5/16. */
public class ExtendedBase<T, F extends Interval & RandomAccessible<T>>
    implements BaseInterface<T, F> {
  private final Bumble bumble;

  public static enum Bumble {
    BEE,
    DEE
  }

  public ExtendedBase(final Bumble bumble) {
    this.bumble = bumble;
  }

  @Override
  public List<T> m(final F f) {
    return new ArrayList<T>();
  }
}
