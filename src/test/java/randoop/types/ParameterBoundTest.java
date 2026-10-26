package randoop.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/** Tests {@link ParameterBound#isLowerBound(ParameterBound, Substitution)}. */
public class ParameterBoundTest {

  /** The bound whose type is the null type. */
  private static final ParameterBound nullBound = new EagerReferenceBound(JavaTypes.NULL_TYPE);

  /** The bound whose type is {@code Integer}. */
  private static final ParameterBound integerBound =
      ParameterBound.forType(ReferenceType.forClass(Integer.class));

  /** The bound whose type is {@code Number}. */
  private static final ParameterBound numberBound =
      ParameterBound.forType(ReferenceType.forClass(Number.class));

  /** The bound whose type is {@code String}. */
  private static final ParameterBound stringBound = ParameterBound.forType(JavaTypes.STRING_TYPE);

  /** The bound of the type parameter of {@link SelfComparable}, which is lazy. */
  private static final ParameterBound selfComparableBound =
      GenericClassType.forClass(SelfComparable.class)
          .getTypeParameters()
          .get(0)
          .getUpperTypeBound();

  /** The type parameter {@code T} of {@link ArrayHolder}. */
  private static final TypeVariable arrayHolderVariable =
      TypeVariable.forType(ArrayHolder.class.getTypeParameters()[0]);

  @Test
  public void testLowerBoundOfOtherBounds() {
    ParameterBound intersection =
        new IntersectionTypeBound(
            Arrays.asList(
                numberBound, ParameterBound.forType(ReferenceType.forClass(Serializable.class))));

    assertTrue(selfComparableBound instanceof LazyParameterBound);
    assertTrue(nullBound.isLowerBound(intersection, new Substitution()));
    assertTrue(nullBound.isLowerBound(selfComparableBound, new Substitution()));
    assertTrue(integerBound.isLowerBound(intersection, new Substitution()));
    assertFalse(stringBound.isLowerBound(intersection, new Substitution()));
    assertFalse(integerBound.isLowerBound(selfComparableBound, new Substitution()));
  }

  /** A lazy bound that the substitution does not instantiate is not a lower bound of anything. */
  @Test
  public void testUninstantiatedLazyParameterBoundAsLowerBound() {
    Substitution unrelated = new Substitution(arrayHolderVariable, JavaTypes.STRING_TYPE);
    for (Substitution substitution : Arrays.asList(new Substitution(), unrelated)) {
      assertFalse(selfComparableBound.isLowerBound(nullBound, substitution));
      assertFalse(selfComparableBound.isLowerBound(integerBound, substitution));
      assertFalse(selfComparableBound.isLowerBound(selfComparableBound, substitution));
    }
  }

  /** A {@link LazyReferenceBound} is a type with free type variables, not one instantiation. */
  @Test
  public void testLazyReferenceBoundHasOnlyNullLowerBound() {
    GenericClassType listType = GenericClassType.forClass(List.class);
    ParameterBound lazyListBound =
        new LazyReferenceBound(listType.instantiate(arrayHolderVariable));
    ParameterBound stringListBound =
        ParameterBound.forType(listType.instantiate(JavaTypes.STRING_TYPE));

    assertFalse(stringListBound.isLowerBound(lazyListBound, new Substitution()));
    assertTrue(nullBound.isLowerBound(lazyListBound, new Substitution()));
  }

  /** The substitution is applied to the lower bound when the other bound is lazy. */
  @Test
  public void testSubstitutionAppliedToLowerBoundOfLazyBound() {
    ParameterBound variableBound = new EagerReferenceBound(arrayHolderVariable);
    assertTrue(
        variableBound.isLowerBound(
            selfComparableBound, new Substitution(arrayHolderVariable, JavaTypes.NULL_TYPE)));
    assertFalse(
        variableBound.isLowerBound(
            selfComparableBound, new Substitution(arrayHolderVariable, JavaTypes.STRING_TYPE)));
  }

  /**
   * A class whose type parameter has a self-referential bound.
   *
   * @param <T> the type parameter
   */
  static class SelfComparable<T extends Comparable<T>> {}

  /**
   * A class with a method whose return type is a generic array.
   *
   * @param <T> the type parameter
   */
  abstract static class ArrayHolder<T> {
    /**
     * Returns an array.
     *
     * @return an array
     */
    abstract T[] array();
  }
}
