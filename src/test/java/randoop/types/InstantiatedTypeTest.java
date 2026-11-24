package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
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

    /**
     * A generic member class whose supertypes refer to type parameters of both classes.
     *
     * @param <U> the type parameter
     */
    class Member<U> extends Base<T> implements Comparable<U> {
      @Override
      public int compareTo(U other) {
        return 0;
      }
    }
  }

  /**
   * A generic superclass.
   *
   * @param <V> the type parameter
   */
  static class Base<V> {}

  /** A field whose type is a reflected generic member type with supertypes. */
  @SuppressWarnings("UnusedVariable")
  private Outer<String>.Member<Integer> reflectedMember;

  /** A field whose type is a reflected generic member type with a wildcard argument. */
  @SuppressWarnings("UnusedVariable")
  private Outer<String>.Member<?> reflectedWildcardMember;

  /**
   * Returns the type of the field of this class with the given name.
   *
   * @param fieldName the name of a field of this class
   * @return the type of the field
   * @throws NoSuchFieldException if there is no such field
   */
  private static InstantiatedType fieldType(String fieldName) throws NoSuchFieldException {
    // Create the generic class type first, which gives the cached generic class type an enclosing
    // type and so makes its getTypeParameters() include the type parameter of Outer.
    ClassOrInterfaceType.forClass(Outer.Member.class);
    return (InstantiatedType)
        ClassOrInterfaceType.forType(
            InstantiatedTypeTest.class.getDeclaredField(fieldName).getGenericType());
  }

  @Test
  public void reflectedMemberTypeSupertypesTest() throws NoSuchFieldException {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    InstantiatedType member = fieldType("reflectedMember");

    GenericClassType base = (GenericClassType) ClassOrInterfaceType.forClass(Base.class);
    assertEquals(base.instantiate(JavaTypes.STRING_TYPE), member.getSuperclass());
    assertEquals(
        Collections.singletonList(JavaTypes.COMPARABLE_TYPE.instantiate(integerType)),
        member.getInterfaces());
    assertTrue(member.isSubtypeOfOrEqualTo(JavaTypes.COMPARABLE_TYPE.instantiate(integerType)));

    Substitution substitution = member.getTypeSubstitution();
    assertNotNull(substitution);
    List<TypeVariable> parameters = member.getGenericClassType().getTypeParameters();
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(parameters.get(0)));
    assertEquals(integerType, substitution.get(parameters.get(1)));
  }

  @Test
  public void reflectedMemberTypeCaptureConversionTest() throws NoSuchFieldException {
    InstantiatedType member = fieldType("reflectedWildcardMember");
    InstantiatedType captured = member.applyCaptureConversion();
    assertTrue(captured.hasCaptureVariable());
    assertEquals(member.getRuntimeClass(), captured.getRuntimeClass());
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
  @SuppressWarnings("UnusedVariable")
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
