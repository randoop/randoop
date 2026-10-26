package randoop.types;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.Test;

/** Tests {@link LazyParameterBound}. */
public class LazyParameterBoundTest {

  /**
   * A class with a field whose type has a type parameter as its first type argument.
   *
   * @param <T> the first type argument of the field's type
   */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  static class MapField<T> {
    /** A parameterized type whose first type argument is a type parameter. */
    Map<T, String> map;
  }

  /**
   * A class whose type parameter is used as a substitution value.
   *
   * @param <S> a type parameter
   */
  static class Holder<S> {}

  /**
   * Tests that substituting a type variable for any type argument of a parameterized bound, not
   * just the last one, yields a lazy bound.
   */
  @Test
  public void substituteTypeVariableInFirstArgument() throws NoSuchFieldException {
    java.lang.reflect.Type fieldType = MapField.class.getDeclaredField("map").getGenericType();
    TypeVariable t = TypeVariable.forType(MapField.class.getTypeParameters()[0]);
    TypeVariable s = TypeVariable.forType(Holder.class.getTypeParameters()[0]);
    ParameterBound bound = new LazyParameterBound(fieldType).substitute(new Substitution(t, s));
    assertTrue(bound instanceof LazyReferenceBound);
  }
}
