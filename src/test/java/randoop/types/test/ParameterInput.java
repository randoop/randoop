package randoop.types.test;

public class ParameterInput {
  public <T> void m(
      Iterable<String> s,
      Iterable<T> t,
      Iterable<? extends T> tw,
      Iterable<? super T> ts,
      Iterable<? extends Comparable<T>> twc,
      Iterable<? super Comparable<T>> tsc) {}
}
