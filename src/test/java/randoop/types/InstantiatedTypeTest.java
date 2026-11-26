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

    /** A field whose type has the owner type {@code Outer<T>}, the generic form of Outer. */
    @SuppressWarnings("unused")
    Inner<String> innerOfString;

    /**
     * Another generic member class, unrelated to {@link Inner}.
     *
     * @param <V> the type parameter
     */
    class Other<V> {}

    /** A non-generic member class. */
    class A {}

    /** A non-generic member class, unrelated to {@link A}. */
    class B {}

    /** A non-generic member class that has a generic member class. */
    class Middle {
      /**
       * A generic member class of a non-generic member class.
       *
       * @param <W> the type parameter
       */
      class Deep<W> {}
    }
  }

  @Test
  public void isInstantiationOfSharesEnclosingBindingsTest() {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    GenericClassType inner = (GenericClassType) ClassOrInterfaceType.forClass(Outer.Inner.class);
    List<TypeVariable> innerParameters = inner.getTypeParameters();
    // Outer<T>.Inner<T>
    InstantiatedType innerSharedVariable =
        inner.substitute(new Substitution(innerParameters.get(1), innerParameters.get(0)));

    InstantiatedType innerOfStringString =
        inner.instantiate(JavaTypes.STRING_TYPE, JavaTypes.STRING_TYPE);
    InstantiatedType innerOfStringInteger = inner.instantiate(JavaTypes.STRING_TYPE, integerType);
    assertTrue(innerOfStringString.isInstantiationOf(innerSharedVariable));
    assertFalse(innerOfStringInteger.isInstantiationOf(innerSharedVariable));
  }

  /** A field whose type is a reflected member type, with an owner that has a wildcard. */
  @SuppressWarnings("UnusedVariable")
  private Outer<?>.Inner<Integer> reflectedWildcardOwner;

  @Test
  public void captureConversionOfWildcardOwnerTest() throws NoSuchFieldException {
    // Sets the enclosing type of the cached generic class.
    ClassOrInterfaceType.forClass(Outer.Inner.class);
    ClassOrInterfaceType reflected =
        ClassOrInterfaceType.forType(
            InstantiatedTypeTest.class.getDeclaredField("reflectedWildcardOwner").getGenericType());
    assertTrue(reflected.hasWildcardArgument());
    ClassOrInterfaceType converted = reflected.applyCaptureConversion();
    assertFalse(converted.hasWildcardArgument());
    assertEquals(
        ReferenceType.forClass(Integer.class),
        ((ReferenceArgument) converted.getTypeArguments().get(0)).getReferenceType());
  }

  /** A field whose type is a reflected member type of a different member class. */
  @SuppressWarnings("UnusedVariable")
  private Outer<String>.Other<Integer> reflectedOther;

  @Test
  public void isInstantiationOfChecksMemberClassTest() throws NoSuchFieldException {
    GenericClassType inner = (GenericClassType) ClassOrInterfaceType.forClass(Outer.Inner.class);
    ClassOrInterfaceType reflected =
        ClassOrInterfaceType.forType(
            InstantiatedTypeTest.class.getDeclaredField("reflectedOther").getGenericType());
    assertFalse(reflected.isInstantiationOf(inner));
    assertNull(reflected.getMatchingSupertype(inner));
  }

  @Test
  public void getInstantiatingSubstitutionRejectsUnrelatedMemberTest() {
    ClassOrInterfaceType a = ClassOrInterfaceType.forClass(Outer.A.class);
    ClassOrInterfaceType b = ClassOrInterfaceType.forClass(Outer.B.class);
    TypeVariable outerParameter =
        ((GenericClassType) ClassOrInterfaceType.forClass(Outer.class)).getTypeParameters().get(0);
    // Outer<String>.A
    ClassOrInterfaceType aOfString =
        a.substitute(new Substitution(outerParameter, JavaTypes.STRING_TYPE));
    // b is Outer<T>.B
    assertTrue(b.isGeneric());
    assertNull(aOfString.getInstantiatingSubstitution(b));
    // a is Outer<T>.A
    Substitution substitution = aOfString.getInstantiatingSubstitution(a);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(outerParameter));
  }

  /** A field whose type is a reflected member type, with an owner that has no type parameters. */
  @SuppressWarnings("UnusedVariable")
  private Outer<String>.Middle.Deep<Integer> reflectedDeep;

  @Test
  public void reflectedOwnerWithoutTypeParametersTest() throws NoSuchFieldException {
    ClassOrInterfaceType reflected =
        ClassOrInterfaceType.forType(
            InstantiatedTypeTest.class.getDeclaredField("reflectedDeep").getGenericType());
    GenericClassType deep =
        (GenericClassType) ClassOrInterfaceType.forClass(Outer.Middle.Deep.class);
    InstantiatedType constructed =
        deep.instantiate(JavaTypes.STRING_TYPE, ReferenceType.forClass(Integer.class));
    assertEquals(constructed, reflected);
    assertEquals(constructed.hashCode(), reflected.hashCode());
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

  /** A non-generic class with a generic member class. */
  static class NonGenericOuter {
    /**
     * A generic member class of a non-generic class.
     *
     * @param <E> the type parameter
     */
    class Member<E> {}
  }

  /** Fields and methods whose types are used in the tests. */
  @SuppressWarnings("unused")
  abstract static class TypeHolder {
    /** A member class whose owner type, as written, is a {@code Class}. */
    NonGenericOuter.Member<String> memberOfNonGenericOuter;

    /** A map whose keys and values have different types. */
    java.util.Map<String, Integer> stringToInteger;

    /** A map whose keys and values have the same type. */
    java.util.Map<String, String> stringToString;

    /**
     * A method whose parameter is a map whose keys and values have the same type.
     *
     * @param <K> the type of the keys and values
     * @param map a map
     */
    abstract <K> void sameKeyAndValue(java.util.Map<K, K> map);
  }

  /**
   * Returns the Randoop type of the given field of {@link TypeHolder}.
   *
   * @param fieldName the name of a field of {@link TypeHolder}
   * @return the type of the field
   */
  private static Type fieldType(String fieldName) {
    try {
      return Type.forType(TypeHolder.class.getDeclaredField(fieldName).getGenericType());
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
  }

  @Test
  public void equalsReflectionAndInstantiationOfMemberOfNonGenericClassTest() {
    Type fromReflection = fieldType("memberOfNonGenericOuter");
    InstantiatedType fromInstantiation =
        ((GenericClassType) ClassOrInterfaceType.forClass(NonGenericOuter.Member.class))
            .instantiate(JavaTypes.STRING_TYPE);
    assertEquals(fromReflection, fromInstantiation);
    assertEquals(fromInstantiation, fromReflection);
    assertEquals(fromReflection.hashCode(), fromInstantiation.hashCode());
  }

  @Test
  public void equalsReflectionAndInstantiationWithGenericFormOwnerTest() {
    GenericClassType inner = (GenericClassType) ClassOrInterfaceType.forClass(Outer.Inner.class);
    TypeVariable t = inner.getTypeParameters().get(0);
    // Outer<T>.Inner<String>, where T is the type parameter of Outer.
    InstantiatedType fromInstantiation = inner.instantiate(t, JavaTypes.STRING_TYPE);
    // The owner type of this reflection type is Outer<T>, the generic form of Outer.
    java.lang.reflect.Type reflectionType;
    try {
      reflectionType = Outer.class.getDeclaredField("innerOfString").getGenericType();
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
    Type fromReflection = Type.forType(reflectionType);
    assertEquals(fromReflection, fromInstantiation);
    assertEquals(fromInstantiation, fromReflection);
    assertEquals(fromReflection.hashCode(), fromInstantiation.hashCode());
  }

  @Test
  public void getInstantiatingSubstitutionConflictingArgumentsTest() throws NoSuchMethodException {
    ReferenceType goalType =
        (ReferenceType)
            Type.forType(
                TypeHolder.class.getDeclaredMethod("sameKeyAndValue", java.util.Map.class)
                    .getGenericParameterTypes()[0]);
    InstantiatedType stringToInteger = (InstantiatedType) fieldType("stringToInteger");
    InstantiatedType stringToString = (InstantiatedType) fieldType("stringToString");
    // Map<String, Integer> would map K to both String and Integer.
    assertNull(stringToInteger.getInstantiatingSubstitution(goalType));
    Substitution substitution = stringToString.getInstantiatingSubstitution(goalType);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(goalType.getTypeParameters().get(0)));
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
