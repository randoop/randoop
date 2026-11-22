package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class InstantiatedTypeTest {

  /**
   * A generic class with a generic member class.
   *
   * @param <T> the type parameter
   */
  static class Outer<T> {
    /**
     * A generic member class.
     *
     * @param <U> the type parameter
     */
    class Inner<U> {}
  }

  @Test
  public void equalsComparesEnclosingTypeTest() {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    GenericClassType inner = (GenericClassType) ClassOrInterfaceType.forClass(Outer.Inner.class);

    InstantiatedType innerOfStringOuter = inner.instantiate(JavaTypes.STRING_TYPE, integerType);
    InstantiatedType innerOfObjectOuter = inner.instantiate(JavaTypes.OBJECT_TYPE, integerType);
    assertNotEquals(innerOfStringOuter, innerOfObjectOuter);

    InstantiatedType innerOfStringOuter2 = inner.instantiate(JavaTypes.STRING_TYPE, integerType);
    assertEquals(innerOfStringOuter, innerOfStringOuter2);
    assertEquals(innerOfStringOuter.hashCode(), innerOfStringOuter2.hashCode());
  }
}
