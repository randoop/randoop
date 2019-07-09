package randoop.types.test;

import java.util.List;

/** Input class for capture conversion used in {@link randoop.types.CaptureConversionTest}. */
public class CaptureTestClass<T> {
  public void a(List<? extends T> l) {}

  public void b(List<?> l) {}

  public void c(List<? super T> l) {}

  public void d(List<String> l) {}

  public void a(Container<?> container) {}

  public void b(Container<? extends T> container) {}

  @SuppressWarnings("signature:type.argument.type.incompatible") // bug in Checker Framework?
  public void c(Container<? super T> container) {}
}
