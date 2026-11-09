package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
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
    // The substitution does not map U, which occurs in the bound, so U remains in the result.
    ParameterBound result = bound.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertTrue(result instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(
                GenericClassType.forClass(List.class).instantiate(u), JavaTypes.STRING_TYPE),
        ((ReferenceBound) result).getBoundType());
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
  public void testNullTypeForArrayComponentIsNotWithinBound() {
    TypeVariable t = GenericClassType.forClass(ArrayArgumentBound.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound();
    // The comparison rejects the null type, rather than failing an assertion.
    assertFalse(
        bound.isUpperBound(JavaTypes.NULL_TYPE, new Substitution(t, JavaTypes.NULL_TYPE)));
  }

  /** Fields whose types are used in tests. */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  static class FieldTypes {
    /** A wildcard whose upper bound is a generic array type that contains no type variable. */
    List<? extends List<String>[]> arrayOfListWildcard;
  }

  @Test
  public void testGenericArrayBoundWithoutTypeVariable() throws NoSuchFieldException {
    java.lang.reflect.ParameterizedType fieldType =
        (java.lang.reflect.ParameterizedType)
            FieldTypes.class.getDeclaredField("arrayOfListWildcard").getGenericType();
    java.lang.reflect.WildcardType wildcard =
        (java.lang.reflect.WildcardType) fieldType.getActualTypeArguments()[0];
    ParameterBound bound = ParameterBound.forTypes(new HashSet<>(0), wildcard.getUpperBounds());
    assertTrue(bound instanceof LazyParameterBound);
    ArrayType arrayOfList =
        ArrayType.ofComponentType(
            GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE));
    // An empty substitution materializes the bound.
    assertEquals(new EagerReferenceBound(arrayOfList), bound.substitute(new Substitution()));
    assertTrue(bound.isUpperBound(arrayOfList, new Substitution()));
    assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, new Substitution()));
  }
}
