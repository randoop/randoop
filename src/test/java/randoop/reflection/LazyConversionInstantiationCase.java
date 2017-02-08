package randoop.reflection;

import java.util.List;

public class LazyConversionInstantiationCase {
  public static <O extends Comparable<? super O>> List<O> collate(
      Iterable<? extends O> a, Iterable<? extends O> b) {
    return null;
  }
}
