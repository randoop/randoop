package randoop.types;

import static org.junit.Assert.assertEquals;
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

  /** A field whose type has a wildcard argument for a type parameter with a recursive bound. */
  SelfFirstArgumentBound<?> wildcardField;

  /**
   * Returns the type parameter of {@link SelfFirstArgumentBound}.
   *
   * @return the type parameter of {@link SelfFirstArgumentBound}
   */
  private static TypeVariable selfFirstArgumentBoundParameter() {
    return GenericClassType.forClass(SelfFirstArgumentBound.class).getTypeParameters().get(0);
  }

  @Test
  public void testSubstituteVariable() {
    TypeVariable t = selfFirstArgumentBoundParameter();
    TypeVariable v = GenericClassType.forClass(Unbounded.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound().substitute(new Substitution(t, v));
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class).instantiate(v, JavaTypes.STRING_TYPE),
        ((ReferenceBound) bound).getBoundType());
  }

  @Test
  public void testSubstituteConcreteType() {
    TypeVariable t = selfFirstArgumentBoundParameter();
    ParameterBound bound =
        t.getUpperTypeBound().substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertTrue(
        "bound " + bound + " should be eager since it contains no type variable",
        bound instanceof EagerReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(JavaTypes.STRING_TYPE, JavaTypes.STRING_TYPE),
        ((ReferenceBound) bound).getBoundType());
  }

  @Test
  public void testCaptureConversion() throws NoSuchFieldException {
    InstantiatedType wildcardType =
        (InstantiatedType)
            ClassOrInterfaceType.forType(
                LazyParameterBoundTest.class.getDeclaredField("wildcardField").getGenericType());
    InstantiatedType capturedType = wildcardType.applyCaptureConversion();
    // The capture variable's bound is Map<capture, String>, which refers to the capture variable.
    assertEquals(1, capturedType.getTypeParameters().size());
  }
}
