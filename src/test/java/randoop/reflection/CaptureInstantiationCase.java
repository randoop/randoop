package randoop.reflection;

import java.util.Collection;

/** based on problematic scenario from Apache Commons Collections */
public class CaptureInstantiationCase {
  public static class LocalPredicate<T> {
    public boolean test(T t) {
      return true;
    }
  }

  public static class OnePredicate<T> extends LocalPredicate<T> {}

  @SuppressWarnings("rawtypes")
  public static final LocalPredicate THE_RAW_PREDICATE = new LocalPredicate<Object>();

  public static <T> boolean filter(Iterable<T> collection, LocalPredicate<? super T> pred) {
    return true;
  }

  public static <T> LocalPredicate<T> oneOf(
      Collection<? extends LocalPredicate<? super T>> predicates) {
    return null;
  }
}
