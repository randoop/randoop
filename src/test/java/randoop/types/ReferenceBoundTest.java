package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

/** Tests {@link ReferenceBound} and its subclasses. */
public class ReferenceBoundTest {

  /**
   * Returns {@code List<? super U>}, where the bound of {@code U} is {@code Integer}.
   *
   * @param <U> a type parameter whose bound is {@code Integer}
   * @return null
   */
  public static <U extends Integer> List<? super U> listSuperIntegerVariable() {
    return null;
  }

  /**
   * Returns the type variable that is the type parameter of {@link #listSuperIntegerVariable}.
   *
   * @return {@code U extends Integer}
   */
  private static TypeVariable integerVariable() {
    try {
      return TypeVariable.forType(
          ReferenceBoundTest.class.getMethod("listSuperIntegerVariable").getTypeParameters()[0]);
    } catch (NoSuchMethodException e) {
      throw new Error(e);
    }
  }

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
  public void testVariableIsLowerBound() {
    ReferenceBound bound = new EagerReferenceBound(integerVariable());
    Substitution substitution = new Substitution();
    assertTrue(bound.isLowerBound(JavaTypes.OBJECT_TYPE, substitution));
    assertTrue(bound.isLowerBound(NonParameterizedType.forClass(Number.class), substitution));
    // U is a subtype of Integer, which is not a subtype of String.
    assertFalse(bound.isLowerBound(JavaTypes.STRING_TYPE, substitution));
  }

  @Test
  public void testCaptureOfSuperVariable() {
    // The capture variable of List<? super U> has lower bound U, where U extends Integer.
    InstantiatedType listType;
    try {
      listType =
          (InstantiatedType)
              Type.forType(
                  ReferenceBoundTest.class
                      .getMethod("listSuperIntegerVariable")
                      .getGenericReturnType());
    } catch (NoSuchMethodException e) {
      throw new Error(e);
    }
    TypeVariable captureVariable =
        (TypeVariable)
            ((ReferenceArgument) listType.applyCaptureConversion().getTypeArguments().get(0))
                .getReferenceType();
    assertTrue(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
                NonParameterizedType.forClass(Number.class), captureVariable)
            != null);
    // No supertype of U, where U extends Integer, is String.
    assertTrue(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
                JavaTypes.STRING_TYPE, captureVariable)
            == null);
  }
}
