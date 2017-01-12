package randoop.types;

import java.util.Collection;

/**
 * Input code for testing capture conversion over LazyParameterBound
 */
public class CapConvInput {
  public class ValType<T> {}

  public static <T> ValType<T> method(Collection<? extends ValType<? super T>> coll) {
    return null;
  }
}
