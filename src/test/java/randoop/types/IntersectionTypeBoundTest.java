package randoop.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

/** Tests {@link IntersectionTypeBound}. */
public class IntersectionTypeBoundTest {

  /** An intersection is a subtype of a type if any of its members is. */
  @Test
  public void testIsLowerBound() {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    ParameterBound integerAndComparable =
        new IntersectionTypeBound(
            Arrays.asList(
                ParameterBound.forType(integerType),
                ParameterBound.forType(
                    GenericClassType.forClass(Comparable.class).instantiate(integerType))));

    assertTrue(
        integerAndComparable.isLowerBound(
            ReferenceType.forClass(Number.class), new Substitution()));
    assertTrue(
        integerAndComparable.isLowerBound(
            GenericClassType.forClass(Comparable.class).instantiate(integerType),
            new Substitution()));
    assertFalse(integerAndComparable.isLowerBound(JavaTypes.STRING_TYPE, new Substitution()));
  }

  /**
   * An intersection is not a subtype of a type if no member is, even if a later member is lazy and
   * the substitution does not instantiate it.
   */
  @Test
  public void testIsLowerBoundWithUninstantiatedLazyMember() throws NoSuchMethodException {
    ParameterBound numberAndLazy =
        new IntersectionTypeBound(
            Arrays.asList(
                ParameterBound.forType(ReferenceType.forClass(Number.class)),
                new LazyParameterBound(
                    LazyHolder.class.getDeclaredMethod("comparable").getGenericReturnType())));

    assertFalse(numberAndLazy.isLowerBound(JavaTypes.STRING_TYPE, new Substitution()));
  }

  /**
   * A class with a method whose return type mentions a type parameter.
   *
   * @param <U> the type parameter
   */
  abstract static class LazyHolder<U> {
    /**
     * Returns a comparable.
     *
     * @return a comparable
     */
    abstract Comparable<U> comparable();
  }
}
