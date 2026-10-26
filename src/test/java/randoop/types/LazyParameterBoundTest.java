package randoop.types;

import static org.junit.Assert.assertSame;

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
}
