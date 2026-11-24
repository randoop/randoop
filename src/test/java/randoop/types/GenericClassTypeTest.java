package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class GenericClassTypeTest {

  /**
   * A generic class with a generic member class.
   *
   * @param <T> the type parameter
   */
  static class Outer<T> {
    /**
     * A generic member class whose type parameter has a bound.
     *
     * @param <U> the type parameter
     */
    class NumberInner<U extends Number> {}
  }

  /**
   * A generic class whose type parameter has a bound, with a generic member class.
   *
   * @param <T> the type parameter
   */
  static class NumberOuter<T extends Number> {
    /**
     * A generic member class.
     *
     * @param <U> the type parameter
     */
    class Inner<U> {}
  }

  @Test
  public void instantiateMemberClassTest() {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    ReferenceType stringType = JavaTypes.STRING_TYPE;

    // The type arguments are for Outer's T and for NumberInner's U, in that order.
    GenericClassType numberInner =
        (GenericClassType) ClassOrInterfaceType.forClass(Outer.NumberInner.class);
    assertEquals(2, numberInner.getTypeParameters().size());
    InstantiatedType instantiated = numberInner.instantiate(stringType, integerType);
    assertEquals(Arrays.asList(TypeArgument.forType(integerType)), instantiated.getTypeArguments());
    GenericClassType outer = (GenericClassType) ClassOrInterfaceType.forClass(Outer.class);
    assertEquals(outer.instantiate(stringType), instantiated.enclosingType);
    numberInner.instantiate(Arrays.asList(stringType, integerType));
    assertThrows(
        IllegalArgumentException.class, () -> numberInner.instantiate(integerType, stringType));
    assertThrows(
        IllegalArgumentException.class,
        () -> numberInner.instantiate(Arrays.asList(integerType, stringType)));

    // The bound of a type parameter of the enclosing class is checked too.
    GenericClassType inner =
        (GenericClassType) ClassOrInterfaceType.forClass(NumberOuter.Inner.class);
    InstantiatedType instantiatedInner = inner.instantiate(integerType, stringType);
    assertEquals(
        Arrays.asList(TypeArgument.forType(stringType)), instantiatedInner.getTypeArguments());
    GenericClassType numberOuter =
        (GenericClassType) ClassOrInterfaceType.forClass(NumberOuter.class);
    assertEquals(numberOuter.instantiate(integerType), instantiatedInner.enclosingType);
    assertThrows(IllegalArgumentException.class, () -> inner.instantiate(stringType, integerType));
  }

  @Test
  public void areWithinBoundsMemberClassTest() {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    ReferenceType stringType = JavaTypes.STRING_TYPE;

    GenericClassType numberInner =
        (GenericClassType) ClassOrInterfaceType.forClass(Outer.NumberInner.class);
    assertTrue(numberInner.areWithinBounds(Arrays.asList(stringType, integerType)));
    assertFalse(numberInner.areWithinBounds(Arrays.asList(integerType, stringType)));

    GenericClassType inner =
        (GenericClassType) ClassOrInterfaceType.forClass(NumberOuter.Inner.class);
    assertTrue(inner.areWithinBounds(Arrays.asList(integerType, stringType)));
    assertFalse(inner.areWithinBounds(Arrays.asList(JavaTypes.OBJECT_TYPE, stringType)));

    assertThrows(
        IllegalArgumentException.class, () -> inner.areWithinBounds(Arrays.asList(integerType)));
  }
}
