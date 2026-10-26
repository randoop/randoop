package randoop.types;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.Test;

/** Tests {@link LazyParameterBound}. */
public class LazyParameterBoundTest {

  /**
   * A type parameter whose bound contains another type parameter nested in a type argument.
   *
   * @param <U> the type parameter that occurs in the bound
   * @param <T> the type parameter with the bound
   */
  static class OtherNestedArgumentBound<U, T extends Map<List<U>, String>> {}

  @Test
  public void testSubstituteWithoutNestedVariable() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(OtherNestedArgumentBound.class).getTypeParameters();
    TypeVariable t = parameters.get(1);
    ParameterBound bound = t.getUpperTypeBound();
    // The substitution does not map U, which occurs in the bound.
    assertSame(bound, bound.substitute(new Substitution(t, JavaTypes.STRING_TYPE)));
  }

  /**
   * A type parameter whose bound contains a wildcard inside an array type.
   *
   * @param <U> the type parameter that occurs in the bound
   * @param <T> the type parameter with the bound
   */
  static class WildcardInArrayBound<U, T extends List<Map<?, U>[]>> {}

  @Test
  public void testHasWildcardInArray() {
    ParameterBound bound =
        GenericClassType.forClass(WildcardInArrayBound.class)
            .getTypeParameters()
            .get(1)
            .getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    assertTrue(bound.hasWildcard());
  }

  /**
   * A type parameter whose bound contains an array of another type parameter.
   *
   * @param <U> the type parameter that occurs in the bound
   * @param <T> the type parameter with the bound
   */
  static class ArrayArgumentBound<U, T extends List<U[]>> {}

  @Test
  public void testSubstituteNullTypeForArrayComponent() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(ArrayArgumentBound.class).getTypeParameters();
    ParameterBound bound = parameters.get(1).getUpperTypeBound();
    // There is no array of the null type, so the substitution does not instantiate the bound.
    assertSame(bound, bound.substitute(new Substitution(parameters.get(0), JavaTypes.NULL_TYPE)));
  }
}
