package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import org.junit.Test;

/** Tests the equality contract of {@link ReferenceBound} and its subclasses. */
public class ReferenceBoundTest {

  @Test
  public void testEagerAndLazyEquality() {
    ReferenceType boundType = NonParameterizedType.forClass(Object.class);
    ReferenceBound eager = new EagerReferenceBound(boundType);
    ReferenceBound lazy = new LazyReferenceBound(boundType);

    // Equality is symmetric across the two subclasses.
    assertTrue(eager.equals(lazy));
    assertTrue(lazy.equals(eager));
    assertEquals(eager.hashCode(), lazy.hashCode());
  }

  @Test
  public void testEqualityOfSameSubclass() {
    ReferenceType boundType = NonParameterizedType.forClass(Number.class);
    ReferenceType otherBoundType = NonParameterizedType.forClass(Object.class);

    assertEquals(new EagerReferenceBound(boundType), new EagerReferenceBound(boundType));
    assertEquals(
        new EagerReferenceBound(boundType).hashCode(),
        new EagerReferenceBound(boundType).hashCode());
    assertEquals(new LazyReferenceBound(boundType), new LazyReferenceBound(boundType));
    assertEquals(
        new LazyReferenceBound(boundType).hashCode(), new LazyReferenceBound(boundType).hashCode());

    assertFalse(new EagerReferenceBound(boundType).equals(new LazyReferenceBound(otherBoundType)));
    assertFalse(new LazyReferenceBound(otherBoundType).equals(new EagerReferenceBound(boundType)));
  }

  @Test
  public void testLowerBoundOfOtherBounds() {
    ParameterBound nullBound = new EagerReferenceBound(JavaTypes.NULL_TYPE);
    ParameterBound integerBound = ParameterBound.forType(ReferenceType.forClass(Integer.class));
    ParameterBound stringBound = ParameterBound.forType(JavaTypes.STRING_TYPE);
    ParameterBound intersection =
        new IntersectionTypeBound(
            Arrays.asList(
                ParameterBound.forType(ReferenceType.forClass(Number.class)),
                ParameterBound.forType(ReferenceType.forClass(Serializable.class))));
    // The bound of `T extends Comparable<T>`, which is lazy.
    ParameterBound lazyBound =
        GenericClassType.forClass(SelfComparable.class)
            .getTypeParameters()
            .get(0)
            .getUpperTypeBound();

    assertTrue(nullBound.isLowerBound(intersection, new Substitution()));
    assertTrue(nullBound.isLowerBound(lazyBound, new Substitution()));
    assertTrue(integerBound.isLowerBound(intersection, new Substitution()));
    assertFalse(stringBound.isLowerBound(intersection, new Substitution()));
    assertFalse(integerBound.isLowerBound(lazyBound, new Substitution()));
  }

  /**
   * A class whose type parameter has a self-referential bound.
   *
   * @param <T> the type parameter
   */
  static class SelfComparable<T extends Comparable<T>> {}
}
