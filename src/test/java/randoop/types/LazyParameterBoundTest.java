package randoop.types;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.Test;

/** Tests {@link LazyParameterBound}. */
public class LazyParameterBoundTest {

  /**
   * A type parameter whose bound contains the type parameter, but not as the last type argument.
   *
   * @param <T> the type parameter
   */
  static class SelfFirstArgumentBound<T extends Map<T, String>> {}

  /**
   * A class with a type parameter.
   *
   * @param <V> the type parameter
   */
  static class Unbounded<V> {}

  @Test
  public void testSubstituteVariable() {
    TypeVariable t =
        GenericClassType.forClass(SelfFirstArgumentBound.class).getTypeParameters().get(0);
    TypeVariable v = GenericClassType.forClass(Unbounded.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound().substitute(new Substitution(t, v));
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
  }
}
