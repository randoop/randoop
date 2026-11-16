package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/** Tests {@link LazyParameterBound}. */
public class LazyParameterBoundTest {

  /**
   * A type parameter whose bound contains the type parameter and, nested in a type argument,
   * another type parameter.
   *
   * @param <U> the type parameter that occurs nested in the bound
   * @param <T> the type parameter with the bound
   */
  static class OtherNestedArgumentBound<U, T extends Map<List<U>, T>> {}

  @Test
  public void testSubstituteWithoutNestedVariable() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(OtherNestedArgumentBound.class).getTypeParameters();
    TypeVariable u = parameters.get(0);
    TypeVariable t = parameters.get(1);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    // The substitution does not map U, which occurs in the bound.  U remains a type variable in
    // the result, rather than becoming a null type argument.
    ParameterBound substituted = bound.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertTrue(
        "bound " + substituted + " should be lazy since it contains a type variable",
        substituted instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(
                GenericClassType.forClass(List.class).instantiate(u), JavaTypes.STRING_TYPE),
        ((ReferenceBound) substituted).getBoundType());
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

  /**
   * A type parameter whose bound contains, in a type argument, an array of the type parameter, and
   * another type parameter.
   *
   * @param <K> the other type parameter
   * @param <U> the type parameter with the bound
   */
  static class NestedArrayArgumentBound<K, U extends Map<K, U[]>> {}

  @Test
  public void testSubstituteNullTypeForNestedArrayComponent() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(NestedArrayArgumentBound.class).getTypeParameters();
    TypeVariable k = parameters.get(0);
    TypeVariable u = parameters.get(1);
    // Substituting for K leaves the bound lazy:  Map<String, U[]>.
    ParameterBound bound =
        u.getUpperTypeBound().substitute(new Substitution(k, JavaTypes.STRING_TYPE));
    assertTrue(bound instanceof LazyReferenceBound);
    // There is no array of the null type, so the array bound remains unresolved, and the null type
    // does not satisfy it.  Neither throws an exception.
    Substitution nullSubstitution = new Substitution(u, JavaTypes.NULL_TYPE);
    assertSame(bound, bound.substitute(nullSubstitution));
    assertFalse(bound.isUpperBound(JavaTypes.NULL_TYPE, nullSubstitution));
    assertFalse(bound.isLowerBound(JavaTypes.NULL_TYPE, nullSubstitution));
  }

  @Test
  public void testNullTypeCannotInstantiateArrayComponentBound() {
    TypeVariable t = GenericClassType.forClass(ArrayArgumentBound.class).getTypeParameters().get(0);
    // There is no array of the null type, so the null type does not satisfy the bound.
    assertFalse(t.canBeInstantiatedBy(JavaTypes.NULL_TYPE));
  }

  /**
   * A type parameter whose bound has a wildcard bounded by an array of another type parameter.
   *
   * @param <T> the component type
   * @param <U> the type parameter with the bound
   */
  static class WildcardArrayOfSiblingBound<T, U extends List<? extends T[]>> {}

  /**
   * A type parameter, to substitute for another one.
   *
   * @param <V> the type parameter
   */
  static class OneParameter<V> {}

  @Test
  public void testSubstituteVariableThenNullTypeForArrayComponent() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(WildcardArrayOfSiblingBound.class).getTypeParameters();
    TypeVariable t = parameters.get(0);
    TypeVariable u = parameters.get(1);
    TypeVariable v = GenericClassType.forClass(OneParameter.class).getTypeParameters().get(0);
    ParameterBound bound = u.getUpperTypeBound().substitute(new Substitution(t, v));
    // There is no array of the null type, so the array bound remains unresolved.
    ParameterBound nullBound = bound.substitute(new Substitution(v, JavaTypes.NULL_TYPE));
    assertEquals(bound, nullBound);
    // Substituting a type that can be an array component resolves the array bound.
    ParameterBound stringBound = bound.substitute(new Substitution(v, JavaTypes.STRING_TYPE));
    assertEquals(
        u.getUpperTypeBound().substitute(new Substitution(t, JavaTypes.STRING_TYPE)), stringBound);
  }

  /**
   * A type parameter whose bound contains an array of a parameterized type with a concrete array
   * type argument.
   *
   * @param <T> the type parameter
   */
  static class ConcreteArrayArgumentBound<T extends List<Map<T, String[]>[]>> {}

  /** A class that satisfies the bound of {@link ConcreteArrayArgumentBound}. */
  @SuppressWarnings("serial")
  static class ConcreteArrayArgumentList
      extends ArrayList<Map<ConcreteArrayArgumentList, String[]>[]> {}

  @Test
  public void testConcreteArrayTypeArgument() {
    TypeVariable t =
        GenericClassType.forClass(ConcreteArrayArgumentBound.class).getTypeParameters().get(0);
    assertTrue(
        t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(ConcreteArrayArgumentList.class)));
  }
}
