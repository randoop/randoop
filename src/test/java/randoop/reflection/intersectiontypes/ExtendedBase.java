package randoop.reflection.intersectiontypes;

import java.util.Collections;
import java.util.List;

public class ExtendedBase<T, F extends Interval & RandomAccessible<T>>
    implements BaseInterface<T, F> {
  @SuppressWarnings("UnusedVariable")
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
    return Collections.emptyList();
  }
}
