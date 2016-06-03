package randoop.types.test;

import java.util.List;

/**
 * Testing for capture conversion
 */
public class CaptureTestClass<T> {
  public void a(List<? extends T> l) {}
  public void b(List<?> l) {}
  public void c(List<? super T> l) {}
  public void d(List<String> l) {}
}
