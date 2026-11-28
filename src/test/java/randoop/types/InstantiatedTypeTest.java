package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
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

    /**
     * A generic member class that has a non-generic member class.
     *
     * @param <M> the type parameter
     */
    class GenericMiddle<M> {
      /** A non-generic member class of a generic member class. */
      class Leaf {}
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

  /** A field whose type is a member class of a generic member class of {@code Outer<String>}. */
  @SuppressWarnings("UnusedVariable")
  private Outer<String>.GenericMiddle<Integer>.Leaf stringOuterLeaf;

  /** A field of the same type as {@link #stringOuterLeaf}. */
  @SuppressWarnings("UnusedVariable")
  private Outer<String>.GenericMiddle<Integer>.Leaf stringOuterLeaf2;

  /**
   * A field whose type differs from that of {@link #stringOuterLeaf} only in the outermost type.
   */
  @SuppressWarnings("UnusedVariable")
  private Outer<Long>.GenericMiddle<Integer>.Leaf longOuterLeaf;

  /**
   * Returns the type of the given field of this class.
   *
   * @param fieldName the name of a field of this class
   * @return the type of the field
   * @throws NoSuchFieldException if there is no such field
   */
  private static ClassOrInterfaceType fieldType(String fieldName) throws NoSuchFieldException {
    return ClassOrInterfaceType.forType(
        InstantiatedTypeTest.class.getDeclaredField(fieldName).getGenericType());
  }

  @Test
  public void ownerOfGenericIntermediateOwnerTest() throws NoSuchFieldException {
    ClassOrInterfaceType stringLeaf = fieldType("stringOuterLeaf");
    ClassOrInterfaceType stringLeaf2 = fieldType("stringOuterLeaf2");
    ClassOrInterfaceType longLeaf = fieldType("longOuterLeaf");
    assertEquals(stringLeaf, stringLeaf2);
    assertEquals(stringLeaf.hashCode(), stringLeaf2.hashCode());
    assertNotEquals(stringLeaf, longLeaf);
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

  /** A field of type {@code Comparator<? super String>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  private static Comparator<? super String> superStringComparator;

  /** A field of type {@code Comparator<? extends String>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  private static Comparator<? extends String> extendsStringComparator;

  /**
   * Tests that {@code isInstantiationOf} and {@code getInstantiatingSubstitution} agree for a type
   * argument that is a wildcard whose bound is a type variable: {@code Comparator<? super String>}
   * instantiates {@code Comparator<? super T>}, but {@code Comparator<? extends String>} does not.
   */
  @Test
  public void wildcardInstantiatesWildcardWithTypeVariableBoundTest()
      throws NoSuchFieldException, NoSuchMethodException {
    InstantiatedType superString =
        (InstantiatedType)
            ClassOrInterfaceType.forType(
                InstantiatedTypeTest.class
                    .getDeclaredField("superStringComparator")
                    .getGenericType());
    InstantiatedType extendsString =
        (InstantiatedType)
            ClassOrInterfaceType.forType(
                InstantiatedTypeTest.class
                    .getDeclaredField("extendsStringComparator")
                    .getGenericType());
    // Comparator<? super T>, the second parameter type of <T> void Arrays.sort(T[], Comparator<?
    // super T>).
    InstantiatedType superT =
        (InstantiatedType)
            ClassOrInterfaceType.forType(
                Arrays.class.getMethod("sort", Object[].class, Comparator.class)
                    .getGenericParameterTypes()[1]);
    TypeVariable t = superT.getTypeParameters().get(0);

    assertTrue(superString.isInstantiationOf(superT));
    Substitution substitution = superString.getInstantiatingSubstitution(superT);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(t));

    assertFalse(extendsString.isInstantiationOf(superT));
    assertNull(extendsString.getInstantiatingSubstitution(superT));
  }
}
