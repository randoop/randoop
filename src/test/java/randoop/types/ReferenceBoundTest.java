package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
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

  /**
   * Returns {@code List<? extends String[]>}.
   *
   * @return null
   */
  public static List<? extends String[]> listExtendsStringArray() {
    return null;
  }

  @Test
  public void testArrayClassWildcardBound() throws NoSuchMethodException {
    InstantiatedType listType =
        (InstantiatedType)
            Type.forType(
                ReferenceBoundTest.class
                    .getMethod("listExtendsStringArray")
                    .getGenericReturnType());
    ArrayType stringArray = ArrayType.forClass(String[].class);
    // Reflection represents String[] in the wildcard bound as a Class, which is an array type.
    WildcardArgument wildcard = (WildcardArgument) listType.getTypeArguments().get(0);
    assertEquals(stringArray, ((ReferenceBound) wildcard.getTypeBound()).getBoundType());

    TypeVariable captureVariable =
        (TypeVariable)
            ((ReferenceArgument) listType.applyCaptureConversion().getTypeArguments().get(0))
                .getReferenceType();
    assertNotNull(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(stringArray, captureVariable));
    // Object[] is not a subtype of String[].
    assertNull(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
            ArrayType.forClass(Object[].class), captureVariable));
  }
}
