package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import org.junit.Test;

/**
 * Tests a generic member class of a parameterized type, such as {@code
 * Outer<String>.Inner<Integer>}, whose enclosing type or own type arguments are wildcards.
 */
public class GenericMemberClassOfParameterizedTypeTest {

  /**
   * A generic class with generic member classes.
   *
   * @param <T> the type parameter
   */
  static class Outer<T> {
    /**
     * A generic member class whose superclass mentions the type parameter of the enclosing class.
     *
     * @param <U> the type parameter
     */
    @SuppressWarnings("serial")
    class Inner<U> extends ArrayList<T[]> {}

    /**
     * A generic member class whose type parameter's bound is the type parameter of the enclosing
     * class.
     *
     * @param <U> the type parameter
     */
    class Middle<U extends T> {}
  }

  /** Methods whose return types are used in the tests. */
  @SuppressWarnings("unused")
  abstract static class TypeHolder {
    abstract Outer<? extends Number>.Inner<String> wildcardOwnerInner();

    abstract Outer<?>.Middle<Integer> wildcardOwnerMiddle();

    abstract Outer<String>.Middle<?> wildcardArgumentMiddle();
  }

  /**
   * Returns the Randoop type for the return type of the given method of {@link TypeHolder}.
   *
   * @param methodName a method name of {@link TypeHolder}
   * @return the return type of the method
   * @throws NoSuchMethodException if {@link TypeHolder} has no method named {@code methodName}
   */
  private static InstantiatedType returnType(String methodName) throws NoSuchMethodException {
    // Give the cached generic classes their enclosing types, so that the type parameters of a
    // generic member class include those of its enclosing class.
    ClassOrInterfaceType.forClass(Outer.Inner.class);
    ClassOrInterfaceType.forClass(Outer.Middle.class);
    return (InstantiatedType)
        Type.forType(TypeHolder.class.getDeclaredMethod(methodName).getGenericReturnType());
  }

  @Test
  public void testSuperclassOfMemberOfWildcardOwner() throws NoSuchMethodException {
    InstantiatedType inner = returnType("wildcardOwnerInner");
    ClassOrInterfaceType superclass = inner.getSuperclass();
    assertTrue(superclass.runtimeClassIs(ArrayList.class));
    // The element type is an array of the capture of `? extends Number`, not of a wildcard.
    ReferenceType elementType =
        ((ReferenceArgument) ((InstantiatedType) superclass).getTypeArguments().get(0))
            .getReferenceType();
    assertTrue(elementType.isArray());
    Type componentType = ((ArrayType) elementType).getComponentType();
    assertTrue(componentType.isReferenceType());
    assertTrue(((ReferenceType) componentType).isCaptureVariable());
  }

  @Test
  public void testCaptureConversionOfMemberOfWildcardOwner() throws NoSuchMethodException {
    InstantiatedType middle = returnType("wildcardOwnerMiddle");
    InstantiatedType captured = middle.applyCaptureConversion();
    assertTrue(captured.hasCaptureVariable());
    assertFalse(captured.hasWildcardArgument());
    assertEquals(middle.getTypeArguments(), captured.getTypeArguments());
  }

  @Test
  public void testCaptureConversionOfWildcardArgumentOfMember() throws NoSuchMethodException {
    InstantiatedType middle = returnType("wildcardArgumentMiddle");
    InstantiatedType captured = middle.applyCaptureConversion();
    assertFalse(captured.hasWildcardArgument());
    ReferenceType argument =
        ((ReferenceArgument) captured.getTypeArguments().get(0)).getReferenceType();
    assertTrue(argument.isCaptureVariable());
    // The bound of `U` is `T`, which the enclosing type `Outer<String>` maps to `String`.
    assertTrue(
        ((CaptureTypeVariable) argument)
            .getUpperTypeBound()
            .isSubtypeOfOrEqualTo(new EagerReferenceBound(JavaTypes.STRING_TYPE)));
  }
}
