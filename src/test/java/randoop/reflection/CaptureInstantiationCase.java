package randoop.reflection;

/**
 * based on problematic scenario from Apache Commons Collections
 */
public class CaptureInstantiationCase {
  public static class LocalPredicate<T> {
    public boolean test(T t) {
      return true;
    }
  }

  public final static LocalPredicate THE_RAW_PREDICATE = new LocalPredicate<Object>();

  public static <T> boolean filter(Iterable<T> collection, LocalPredicate<? super T> pred) {
    return true;
  }
}
