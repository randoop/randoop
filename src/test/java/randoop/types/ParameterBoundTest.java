package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;

/** Tests the construction of type parameters and their bounds. */
public class ParameterBoundTest {

  /**
   * A type parameter bounded by its declaring class applied to a wildcard (issue #1889).
   *
   * @param <T> the type parameter
   */
  interface WildcardSelfBound<T extends WildcardSelfBound<?>> {}

  /**
   * A type parameter whose bound refers to {@link MutualB}, which refers back to this class.
   *
   * @param <T> the type parameter
   */
  interface MutualA<T extends MutualB<?>> {}

  /**
   * A type parameter whose bound refers to {@link MutualA}, which refers back to this class.
   *
   * @param <U> the type parameter
   */
  interface MutualB<U extends MutualA<?>> {}

  /**
   * A type parameter whose bound is a class nested in this class.
   *
   * @param <T> the type parameter
   */
  static class EnclosingBound<T extends EnclosingBound.Inner> {
    /** A class nested in {@link EnclosingBound}. */
    static class Inner {}
  }

  /**
   * A type parameter whose bound has the raw declaring class as a type argument.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("rawtypes")
  static class RawSelfArgumentBound<T extends Comparable<RawSelfArgumentBound>> {}

  /**
   * A type parameter with the same bound as {@link RawSelfArgumentBound}.
   *
   * @param <V> the type parameter
   */
  @SuppressWarnings("rawtypes")
  static class OtherRawSelfArgumentBound<V extends Comparable<RawSelfArgumentBound>> {}

  /**
   * A type parameter whose bound contains a different type parameter.
   *
   * @param <E> the element type
   * @param <C> the collection type
   */
  static class SiblingBound<E, C extends Collection<E>> {}

  /**
   * A type parameter with a parameterized bound that contains no type variables.
   *
   * @param <T> the type parameter
   */
  static class ComparableStringBound<T extends Comparable<String>> {}

  /**
   * A type parameter with the same bound as {@link ComparableStringBound}.
   *
   * @param <V> the type parameter
   */
  static class OtherComparableStringBound<V extends Comparable<String>> {}

  /**
   * Returns the type parameter at the given index of the given generic class.
   *
   * @param c a generic class
   * @param index the index of the type parameter
   * @return the type parameter at {@code index} of {@code c}
   */
  private static TypeVariable typeParameter(Class<?> c, int index) {
    return GenericClassType.forClass(c).getTypeParameters().get(index);
  }

  @Test
  public void testWildcardSelfBound() {
    assertEquals(1, GenericClassType.forClass(WildcardSelfBound.class).getTypeParameters().size());
  }

  @Test
  public void testMutuallyRecursiveBounds() {
    assertEquals(1, GenericClassType.forClass(MutualA.class).getTypeParameters().size());
    assertEquals(1, GenericClassType.forClass(MutualB.class).getTypeParameters().size());
  }

  @Test
  public void testEnclosingBound() {
    assertEquals(1, GenericClassType.forClass(EnclosingBound.class).getTypeParameters().size());
  }

  @Test
  public void testRawSelfArgumentBound() {
    TypeVariable t = typeParameter(RawSelfArgumentBound.class, 0);
    TypeVariable v = typeParameter(OtherRawSelfArgumentBound.class, 0);
    assertTrue(v.isInstantiationOf(t));
  }

  @Test
  public void testSiblingBound() {
    TypeVariable c = typeParameter(SiblingBound.class, 1);
    GenericClassType arrayListType = GenericClassType.forClass(ArrayList.class);
    InstantiatedType arrayListOfString =
        arrayListType.substitute(
            new Substitution(arrayListType.getTypeParameters(), JavaTypes.STRING_TYPE));
    assertTrue(arrayListOfString.isInstantiationOf(c));
    assertNotNull(arrayListOfString.getInstantiatingSubstitution(c));
  }

  @Test
  public void testConcreteParameterizedBounds() {
    TypeVariable t = typeParameter(ComparableStringBound.class, 0);
    TypeVariable v = typeParameter(OtherComparableStringBound.class, 0);
    assertTrue(t.isInstantiationOf(v));

    ReferenceType substituted = t.substitute(new Substitution(v, JavaTypes.STRING_TYPE));
    assertEquals(t, substituted);
  }
}
