package randoop.reflection;

/** Created by bjkeller on 10/20/16. */
public abstract class PBInterface<T> {
  public boolean m0(T t) {
    return false;
  }

  public <U> PBInterface<T> m1(FInterface<? super T, ? extends U> f) {
    return null;
  }
}
