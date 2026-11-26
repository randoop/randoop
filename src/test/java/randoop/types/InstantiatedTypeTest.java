package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
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

  @Test
  public void isInstantiationOfComparesEnclosingTypeTest() {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    GenericClassType inner = (GenericClassType) ClassOrInterfaceType.forClass(Outer.Inner.class);
    List<TypeVariable> innerParameters = inner.getTypeParameters();

    InstantiatedType innerOfStringOuter = inner.instantiate(JavaTypes.STRING_TYPE, integerType);
    InstantiatedType innerOfObjectOuter = inner.instantiate(JavaTypes.OBJECT_TYPE, integerType);
    assertTrue(innerOfStringOuter.isInstantiationOf(innerOfStringOuter));
    assertFalse(innerOfStringOuter.isInstantiationOf(innerOfObjectOuter));

    // Outer<T>.Inner<Integer>, whose enclosing type is generic.
    InstantiatedType innerOfGenericOuter =
        inner.substitute(new Substitution(innerParameters.get(1), integerType));
    assertTrue(innerOfStringOuter.isInstantiationOf(innerOfGenericOuter));
  }

  /** A field whose type is a reflected member type. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"})
  private Outer<String>.Inner<Integer> reflectedInner;

  @Test
  public void reflectedMemberTypeEqualsConstructedTest() throws NoSuchFieldException {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    GenericClassType inner = (GenericClassType) ClassOrInterfaceType.forClass(Outer.Inner.class);
    InstantiatedType constructed = inner.instantiate(JavaTypes.STRING_TYPE, integerType);

    ClassOrInterfaceType reflected =
        ClassOrInterfaceType.forType(
            InstantiatedTypeTest.class.getDeclaredField("reflectedInner").getGenericType());
    assertEquals(constructed, reflected);
    assertEquals(constructed.hashCode(), reflected.hashCode());
  }

  @Test
  public void getInstantiatingSubstitutionUnifiesEnclosingTypeTest() {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    GenericClassType inner = (GenericClassType) ClassOrInterfaceType.forClass(Outer.Inner.class);
    List<TypeVariable> innerParameters = inner.getTypeParameters();
    TypeVariable outerParameter = innerParameters.get(0);

    InstantiatedType innerOfStringOuter = inner.instantiate(JavaTypes.STRING_TYPE, integerType);
    // Outer<T>.Inner<Integer>
    InstantiatedType innerOfGenericOuter =
        inner.substitute(new Substitution(innerParameters.get(1), integerType));

    Substitution substitution =
        innerOfStringOuter.getInstantiatingSubstitution(innerOfGenericOuter);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(outerParameter));

    // Outer<Object>.Inner<U> does not unify with Outer<String>.Inner<Integer>.
    InstantiatedType innerOfObjectOuterGeneric =
        inner.substitute(new Substitution(outerParameter, JavaTypes.OBJECT_TYPE));
    assertNull(innerOfStringOuter.getInstantiatingSubstitution(innerOfObjectOuterGeneric));
  }
}
