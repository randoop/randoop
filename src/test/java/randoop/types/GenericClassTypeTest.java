package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

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
    numberInner.instantiate(Arrays.asList(stringType, integerType));
    assertThrows(
        IllegalArgumentException.class, () -> numberInner.instantiate(integerType, stringType));
    assertThrows(
        IllegalArgumentException.class,
        () -> numberInner.instantiate(Arrays.asList(integerType, stringType)));

    // The bound of a type parameter of the enclosing class is checked too.
    GenericClassType inner =
        (GenericClassType) ClassOrInterfaceType.forClass(NumberOuter.Inner.class);
    inner.instantiate(integerType, stringType);
    assertThrows(IllegalArgumentException.class, () -> inner.instantiate(stringType, integerType));
  }
}
