package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
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
   * A type parameter whose bound contains the type parameter nested in a type argument.
   *
   * @param <T> the type parameter
   */
  static class SelfNestedArgumentBound<T extends Map<List<T>, String>> {}

  /**
   * A type parameter whose bound contains the type parameter as the bound of a wildcard.
   *
   * @param <T> the type parameter
   */
  static class SelfWildcardArgumentBound<T extends Comparable<? super T>> {}

  /**
   * A type parameter whose bound contains the type parameter and, nested in a type argument,
   * another type parameter.
   *
   * @param <U> the type parameter that occurs nested in the bound
   * @param <T> the type parameter with the bound
   */
  static class OtherNestedArgumentBound<U, T extends Map<List<U>, T>> {}

  /**
   * A class with a type parameter.
   *
   * @param <V> the type parameter
   */
  static class Unbounded<V> {}

  /**
   * Returns the type parameter of {@link SelfFirstArgumentBound}.
   *
   * @return the type parameter of {@link SelfFirstArgumentBound}
   */
  private static TypeVariable selfFirstArgumentBoundParameter() {
    return GenericClassType.forClass(SelfFirstArgumentBound.class).getTypeParameters().get(0);
  }

  @Test
  public void testSubstituteNestedVariable() {
    TypeVariable t =
        GenericClassType.forClass(SelfNestedArgumentBound.class).getTypeParameters().get(0);
    TypeVariable v = GenericClassType.forClass(Unbounded.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound().substitute(new Substitution(t, v));
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(
                GenericClassType.forClass(List.class).instantiate(v), JavaTypes.STRING_TYPE),
        ((ReferenceBound) bound).getBoundType());
    assertEquals(Collections.singletonList(v), bound.getTypeParameters());
  }

  @Test
  public void testSubstituteWildcardBoundVariable() {
    TypeVariable t =
        GenericClassType.forClass(SelfWildcardArgumentBound.class).getTypeParameters().get(0);
    TypeVariable v = GenericClassType.forClass(Unbounded.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound().substitute(new Substitution(t, v));
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(Collections.singletonList(v), bound.getTypeParameters());
  }

  @Test
  public void testSubstituteTypeContainingVariable() {
    TypeVariable t = selfFirstArgumentBoundParameter();
    TypeVariable v = GenericClassType.forClass(Unbounded.class).getTypeParameters().get(0);
    InstantiatedType listOfV = GenericClassType.forClass(List.class).instantiate(v);
    ParameterBound bound = t.getUpperTypeBound().substitute(new Substitution(t, listOfV));
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class).instantiate(listOfV, JavaTypes.STRING_TYPE),
        ((ReferenceBound) bound).getBoundType());
    assertEquals(Collections.singletonList(v), bound.getTypeParameters());
  }

  @Test
  public void testSubstituteWithoutNestedVariable() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(OtherNestedArgumentBound.class).getTypeParameters();
    TypeVariable t = parameters.get(1);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    // The substitution does not map U, which occurs in the bound.
    assertSame(bound, bound.substitute(new Substitution(t, JavaTypes.STRING_TYPE)));
  }

  /**
   * A type parameter whose self-referential bound contains a wildcard inside an array type.
   *
   * @param <T> the type parameter
   */
  static class WildcardInArrayBound<T extends List<Map<?, T>[]>> {}

  @Test
  public void testHasWildcardInArray() {
    ParameterBound bound =
        GenericClassType.forClass(WildcardInArrayBound.class)
            .getTypeParameters()
            .get(0)
            .getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    assertTrue(bound.hasWildcard());
  }

  /**
   * A type parameter whose self-referential bound contains an array of the type parameter.
   *
   * @param <T> the type parameter
   */
  static class ArrayArgumentBound<T extends List<T[]>> {}

  @Test
  public void testSubstituteNullTypeForArrayComponent() {
    TypeVariable t = GenericClassType.forClass(ArrayArgumentBound.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    // There is no array of the null type, so the substitution does not instantiate the bound.
    assertSame(bound, bound.substitute(new Substitution(t, JavaTypes.NULL_TYPE)));
  }

  @Test
  public void testCompareNullTypeForArrayComponent() {
    TypeVariable t = GenericClassType.forClass(ArrayArgumentBound.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound();
    // There is no array of the null type, so no type satisfies the substituted bound.
    Substitution substitution = new Substitution(t, JavaTypes.NULL_TYPE);
    assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, substitution));
    assertFalse(bound.isLowerBound(JavaTypes.STRING_TYPE, substitution));
  }
}
