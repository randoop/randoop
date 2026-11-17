package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.plumelib.util.CollectionsPlume;

public class ClassOrInterfaceTypeTest {

  // protected @Nullable ClassOrInterfaceType enclosingType = null;
  // public static ClassOrInterfaceType forClass(Class<?> classType) {
  // public static ClassOrInterfaceType forType(java.lang.reflect.Type type) {
  // public boolean equals(@Nullable Object obj) {
  // public int hashCode() {
  // public abstract ClassOrInterfaceType substitute(Substitution substitution);
  // public abstract ClassOrInterfaceType applyCaptureConversion();
  // public String getSimpleName() {
  // public @Nullable String getCanonicalName() {
  // public @Nullable String getFqName() {
  // public String getBinaryName() {
  // public String getUnqualifiedBinaryName() {
  // public abstract List<ClassOrInterfaceType> getInterfaces();
  // public @Nullable Package getPackage() {
  // public abstract NonParameterizedType getRawtype();
  // public @Nullable InstantiatedType getMatchingSupertype(GenericClassType goalType) {
  // public @Nullable Substitution getInstantiatingSubstitution(ReferenceType goalType) {
  // public abstract ClassOrInterfaceType getSuperclass();

  // public Collection<ClassOrInterfaceType> getSupertypes() {

  @Test
  public void getSupertypesTest() {
    ClassOrInterfaceType cType = ClassOrInterfaceType.forClass(C.class);

    Collection<ClassOrInterfaceType> cSupersStrict = cType.getSupertypesStrict();
    if (cSupersStrict.size() != 6) {
      throw new Error("Expected size 6: " + cSupersStrict);
    }
    assertTrue(CollectionsPlume.hasNoDuplicates(new ArrayList<>(cSupersStrict)));

    Collection<ClassOrInterfaceType> cSupersNonstrict = cType.getSupertypesInclusive();
    if (cSupersNonstrict.size() != 7) {
      throw new Error("Expected size 7: " + cSupersNonstrict);
    }
    assertTrue(CollectionsPlume.hasNoDuplicates(new ArrayList<>(cSupersNonstrict)));

    ClassOrInterfaceType dType = ClassOrInterfaceType.forClass(D.class);

    Collection<ClassOrInterfaceType> dSupersStrict = dType.getSupertypesStrict();
    if (dSupersStrict.size() != 7) {
      throw new Error("Expected size 7: " + dSupersStrict);
    }
    assertTrue(CollectionsPlume.hasNoDuplicates(new ArrayList<>(dSupersStrict)));

    Collection<ClassOrInterfaceType> dSupersNonstrict = dType.getSupertypesInclusive();
    if (dSupersNonstrict.size() != 8) {
      throw new Error("Expected size 8: " + dSupersNonstrict);
    }
    assertTrue(CollectionsPlume.hasNoDuplicates(new ArrayList<>(dSupersNonstrict)));

    ClassOrInterfaceType objectType = ClassOrInterfaceType.forClass(Object.class);

    assertTrue(objectType.getSupertypesStrict().isEmpty());
    assertEquals(1, objectType.getSupertypesInclusive().size());
  }

  // public List<ClassOrInterfaceType> getImmediateSupertypes() {
  // public Collection<ClassOrInterfaceType> getSupertypesInclusive() {
  // public abstract boolean isAbstract();
  // public boolean isGeneric(boolean ignoreWildcards) {
  // public boolean isInstantiationOf(ReferenceType otherType) {
  // public final boolean isNestedClass() {
  // public final boolean isMemberClass() {
  // public boolean isParameterized() {
  // public abstract boolean isStatic();
  // public boolean isSubtypeOf(Type otherType) {
  // public boolean hasWildcard() {
  // public boolean hasCaptureVariable() {
  // public List<TypeArgument> getTypeArguments() {
  // public List<TypeVariable> getTypeParameters() {
  // public boolean isClassOrInterfaceType() {

  // ///////////////////////////////////////////////////////////////////////////
  // Classes used within tests
  //

  interface I {}

  interface J extends I {}

  interface K extends I {}

  class A {}

  class B extends A implements J {}

  class C extends B implements K {}

  class D extends C {}

  /**
   * A generic class with two levels of non-static member classes that declare no type parameters.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("ClassCanBeStatic")
  static class TwoLevelOuter<T> {
    /** A member class that declares no type parameters. */
    class Middle {
      /** A member class of a member class; neither declares type parameters. */
      class Inner {}
    }

    /** A field whose type is the generic form of {@link Middle.Inner}. */
    @SuppressWarnings("UnusedVariable") // the field is read reflectively
    Middle.Inner genericFormField;
  }

  /** A field whose type is {@code TwoLevelOuter<String>.Middle.Inner}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  TwoLevelOuter<String>.Middle.Inner twoLevelField;

  @Test
  public void forTypePreservesParameterizedOwnerOfTwoMemberLevels() throws NoSuchFieldException {
    ClassOrInterfaceType ofString =
        ClassOrInterfaceType.forType(
            ClassOrInterfaceTypeTest.class.getDeclaredField("twoLevelField").getGenericType());
    // The outermost enclosing type is TwoLevelOuter<String>, not the declaration TwoLevelOuter<T>.
    assertFalse(ofString.isGenericForm());
    assertFalse(ofString.isGeneric());
    ClassOrInterfaceType outermost = ofString.enclosingType.enclosingType;
    assertEquals(
        Collections.singletonList(TypeArgument.forType(JavaTypes.STRING_TYPE)),
        outermost.getTypeArguments());

    ClassOrInterfaceType genericForm =
        ClassOrInterfaceType.forType(
            TwoLevelOuter.class.getDeclaredField("genericFormField").getGenericType());
    assertTrue(genericForm.isGenericForm());
  }

  /**
   * A generic class with a generic member class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("ClassCanBeStatic")
  static class GenericOuter<T> {
    /**
     * A generic member class.
     *
     * @param <U> the type parameter
     */
    class GenericInner<U> {}
  }

  @Test
  public void instantiatingSubstitutionChecksNonGenericEnclosingType() {
    GenericClassType innerType =
        (GenericClassType) ClassOrInterfaceType.forClass(GenericOuter.GenericInner.class);
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    ReferenceType longType = JavaTypes.LONG_TYPE.toBoxedPrimitive();
    TypeVariable x = GenericClassType.forClass(List.class).getTypeParameters().get(0);
    InstantiatedType stringInteger = innerType.instantiate(JavaTypes.STRING_TYPE, integerType);
    // GenericOuter<Long>.GenericInner<X>.  No substitution for X unifies the enclosing types.
    InstantiatedType longX = innerType.instantiate(longType, x);
    assertNull(stringInteger.getInstantiatingSubstitution(longX));
    // GenericOuter<String>.GenericInner<X>
    InstantiatedType stringX = innerType.instantiate(JavaTypes.STRING_TYPE, x);
    Substitution substitution = stringInteger.getInstantiatingSubstitution(stringX);
    assertNotNull(substitution);
    assertEquals(integerType, substitution.get(x));
  }

  /**
   * A generic class with a generic member class that has a generic member class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("ClassCanBeStatic")
  static class ThreeLevelOuter<T> {
    /**
     * A generic member class.
     *
     * @param <U> the type parameter
     */
    class Middle<U> {
      /**
       * A generic member class of a generic member class.
       *
       * @param <V> the type parameter
       */
      class Inner<V> {}
    }
  }

  @Test
  public void instantiatingSubstitutionChecksOwnerOfGenericEnclosingType() {
    GenericClassType innerType =
        (GenericClassType) ClassOrInterfaceType.forClass(ThreeLevelOuter.Middle.Inner.class);
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    ReferenceType longType = JavaTypes.LONG_TYPE.toBoxedPrimitive();
    ReferenceType booleanType = JavaTypes.BOOLEAN_TYPE.toBoxedPrimitive();
    TypeVariable x = GenericClassType.forClass(List.class).getTypeParameters().get(0);
    // ThreeLevelOuter<String>.Middle<Integer>.Inner<Long>
    InstantiatedType stringIntegerLong =
        innerType.instantiate(JavaTypes.STRING_TYPE, integerType, longType);
    // ThreeLevelOuter<Boolean>.Middle<Integer>.Inner<X>.  The enclosing types differ only in the
    // type argument of the outermost type.
    InstantiatedType booleanIntegerX = innerType.instantiate(booleanType, integerType, x);
    assertNull(stringIntegerLong.getInstantiatingSubstitution(booleanIntegerX));
    // ThreeLevelOuter<String>.Middle<Integer>.Inner<X>
    InstantiatedType stringIntegerX = innerType.instantiate(JavaTypes.STRING_TYPE, integerType, x);
    Substitution substitution = stringIntegerLong.getInstantiatingSubstitution(stringIntegerX);
    assertNotNull(substitution);
    assertEquals(longType, substitution.get(x));
  }

  @Test
  public void boundOfMemberOfParameterizedType() {
    GenericClassType outerType = (GenericClassType) ClassOrInterfaceType.forClass(Outer.class);
    ClassOrInterfaceType innerOfString =
        ClassOrInterfaceType.forClass(Outer.Inner.class)
            .substitute(
                new Substitution(
                    outerType.getTypeParameters(), ClassOrInterfaceType.forClass(String.class)));
    // Outer<String>.Inner is parameterized, but is not an InstantiatedType.
    assertTrue(innerOfString.isParameterized());
    ParameterBound bound = new EagerReferenceBound(innerOfString);
    assertTrue(bound.isUpperBound(innerOfString, new Substitution()));
    assertTrue(bound.isLowerBound(innerOfString, new Substitution()));
    assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, new Substitution()));
    assertFalse(bound.isLowerBound(JavaTypes.STRING_TYPE, new Substitution()));
  }

  /** Declares methods whose parameter types are used in tests. */
  static class TypeVariableHolder {
    /**
     * A method whose parameter type mentions a type variable.
     *
     * @param <T> the element type
     * @param list a list
     */
    static <T> void m(List<T> list) {}

    /**
     * A method whose parameter types are parameterized types.
     *
     * @param strings a list of strings
     * @param comparables a collection of comparables
     * @param superStrings a list whose elements are supertypes of String
     * @param iterables a collection of iterables
     */
    static void n(
        List<String> strings,
        Collection<Comparable<String>> comparables,
        List<? super String> superStrings,
        Collection<? extends Iterable<Comparable<String>>> iterables) {}

    /**
     * A method whose parameter types are member classes of non-generic member classes of
     * parameterized types.
     *
     * @param <U> a type parameter
     * @param ofString an {@code Outer<String>.Middle.Deepest}
     * @param ofU an {@code Outer<U>.Middle.Deepest}
     */
    static <U> void deep(Outer<String>.Middle.Deepest ofString, Outer<U>.Middle.Deepest ofU) {}
  }

  /**
   * A generic class with an inner class.
   *
   * @param <T> the type parameter
   */
  static class Outer<T> {
    /** An inner class whose enclosing type is generic. */
    class Inner {}

    /** An inner class whose enclosing type is generic, and that has inner classes. */
    class Middle {
      /** An inner class of an inner class whose enclosing type is generic. */
      class Deepest {}

      /** An inner class whose enclosing type is non-generic, but is a member of a generic class. */
      class Innermost {}
    }
  }

  /** Fields whose types are member classes of member classes of parameterized types. */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  static class NestedMemberFields {
    /** A member class of a member class of {@code Outer<String>}. */
    Outer<String>.Middle.Innermost stringInnermost;

    /** A member class of a member class of {@code Outer<Integer>}. */
    Outer<Integer>.Middle.Innermost integerInnermost;
  }

  @Test
  public void testTwoNonGenericMemberLevels() throws NoSuchFieldException {
    ClassOrInterfaceType stringInnermost =
        ClassOrInterfaceType.forType(
            NestedMemberFields.class.getDeclaredField("stringInnermost").getGenericType());
    ClassOrInterfaceType integerInnermost =
        ClassOrInterfaceType.forType(
            NestedMemberFields.class.getDeclaredField("integerInnermost").getGenericType());
    // The type argument String of the outermost type is preserved.
    assertFalse(stringInnermost.isGenericForm());
    assertFalse(stringInnermost.isGeneric());
    assertTrue(stringInnermost.isParameterized());
    assertEquals(
        JavaTypes.STRING_TYPE,
        ((ReferenceArgument) stringInnermost.enclosingType.enclosingType.getTypeArguments().get(0))
            .getReferenceType());
    assertFalse(stringInnermost.equals(integerInnermost));
    // The generic form is still the generic form.
    ClassOrInterfaceType genericInnermost =
        ClassOrInterfaceType.forClass(Outer.Middle.Innermost.class);
    assertTrue(genericInnermost.isGenericForm());
  }

  /**
   * Returns the type of the given parameter of {@link TypeVariableHolder#n}.
   *
   * @param i the index of the parameter
   * @return the type of the parameter
   */
  private static Type nParameterType(int i) throws NoSuchMethodException {
    return Type.forType(
        TypeVariableHolder.class.getDeclaredMethod(
                "n", List.class, Collection.class, List.class, Collection.class)
            .getGenericParameterTypes()[i]);
  }

  /**
   * Returns the type argument of the capture conversion of the given type.
   *
   * @param type a parameterized type with one wildcard argument
   * @return the capture variable that is the argument of the capture conversion of {@code type}
   */
  private static CaptureTypeVariable captureVariable(Type type) {
    InstantiatedType captured = ((InstantiatedType) type).applyCaptureConversion();
    return (CaptureTypeVariable)
        ((ReferenceArgument) captured.getTypeArguments().get(0)).getReferenceType();
  }

  @Test
  public void rawtypeIsNotSubtypeOfParameterizedTypeTest() throws NoSuchMethodException {
    GenericClassType listType = (GenericClassType) ClassOrInterfaceType.forClass(List.class);
    NonParameterizedType rawList = listType.getRawtype();
    assertTrue(rawList.isSubtypeOfOrEqualTo(listType));
    InstantiatedType listOfE =
        listType.instantiate(new ArrayList<ReferenceType>(listType.getTypeParameters()));
    assertTrue(listOfE.isGenericForm());
    assertTrue(rawList.isSubtypeOfOrEqualTo(listOfE));
    // The generic class and the instantiated type are two representations of the same type.
    assertTrue(listType.isSubtypeOfOrEqualTo(listOfE));

    // A raw type is assignable to a parameterized type only by unchecked conversion, and it is not
    // a subtype of it, even if the parameterized type mentions a type variable.
    Type listOfT =
        Type.forType(
            TypeVariableHolder.class.getDeclaredMethod("m", List.class)
                .getGenericParameterTypes()[0]);
    assertTrue(listOfT instanceof InstantiatedType);
    assertFalse(((InstantiatedType) listOfT).isGenericForm());
    assertFalse(rawList.isSubtypeOfOrEqualTo(listOfT));

    // A parameterized type is a subtype of the generic form of its class, but not of another
    // parameterized type that mentions a type variable.
    Type listOfString = nParameterType(0);
    assertTrue(listOfString.isSubtypeOfOrEqualTo(listType));
    assertTrue(listOfString.isSubtypeOfOrEqualTo(listOfE));
    assertFalse(listOfString.isSubtypeOfOrEqualTo(listOfT));

    // `? extends List<T>` does not contain `? extends List<String>`.
    WildcardArgument extendsListOfT =
        new WildcardArgument(
            new WildcardType(new EagerReferenceBound((ReferenceType) listOfT), true));
    WildcardArgument extendsListOfString =
        new WildcardArgument(
            new WildcardType(new EagerReferenceBound((ReferenceType) listOfString), true));
    assertFalse(extendsListOfT.contains(extendsListOfString));
  }

  @Test
  public void subtypeOfCaptureVariableTest() throws NoSuchMethodException {
    ClassOrInterfaceType stringType = ClassOrInterfaceType.forClass(String.class);
    NonParameterizedType rawComparable =
        ClassOrInterfaceType.forClass(Comparable.class).getRawtype();

    // CAP#1 of `? super String` has lower bound String.
    CaptureTypeVariable superString = captureVariable(nParameterType(2));
    assertTrue(stringType.isSubtypeOfOrEqualTo(superString));
    assertFalse(rawComparable.isSubtypeOfOrEqualTo(superString));

    // CAP#1 of `? extends Iterable<Comparable<String>>` has no lower bound.
    Type iterables = nParameterType(3);
    CaptureTypeVariable extendsIterable = captureVariable(iterables);
    assertFalse(stringType.isSubtypeOfOrEqualTo(extendsIterable));
    assertFalse(rawComparable.isSubtypeOfOrEqualTo(extendsIterable));
    Type comparables = nParameterType(1);
    assertFalse(
        comparables.isSubtypeOfOrEqualTo(((InstantiatedType) iterables).applyCaptureConversion()));

    // An explicit type variable has no lower bound.
    TypeVariable listParameter =
        ((GenericClassType) ClassOrInterfaceType.forClass(List.class)).getTypeParameters().get(0);
    assertFalse(stringType.isSubtypeOfOrEqualTo(listParameter));
    assertFalse(rawComparable.isSubtypeOfOrEqualTo(listParameter));
    assertFalse(comparables.isSubtypeOfOrEqualTo(listParameter));
  }

  @Test
  public void memberOfGenericClassSubtypeTest() {
    GenericClassType outerType = (GenericClassType) ClassOrInterfaceType.forClass(Outer.class);
    ClassOrInterfaceType innerGenericForm = ClassOrInterfaceType.forClass(Outer.Inner.class);
    assertTrue(innerGenericForm.isGenericForm());
    ClassOrInterfaceType innerOfString =
        innerGenericForm.substitute(
            new Substitution(
                outerType.getTypeParameters(), ClassOrInterfaceType.forClass(String.class)));
    assertFalse(innerOfString.isGenericForm());
    NonParameterizedType rawInner = innerGenericForm.getRawtype();
    assertFalse(rawInner.isGenericForm());

    assertTrue(innerOfString.isSubtypeOfOrEqualTo(innerGenericForm));
    assertTrue(rawInner.isSubtypeOfOrEqualTo(innerGenericForm));
    assertFalse(rawInner.isSubtypeOfOrEqualTo(innerOfString));
  }

  /**
   * A member class of a non-generic member class of a parameterized type keeps the type arguments
   * of the outermost owner type.
   */
  @Test
  public void twoLevelMemberOfParameterizedTypeTest() throws NoSuchMethodException {
    java.lang.reflect.Type[] parameterTypes =
        TypeVariableHolder.class
            .getDeclaredMethod("deep", Outer.Middle.Deepest.class, Outer.Middle.Deepest.class)
            .getGenericParameterTypes();
    ClassOrInterfaceType ofString = ClassOrInterfaceType.forType(parameterTypes[0]);
    assertFalse(ofString.isGeneric());
    assertTrue(ofString.getTypeParameters().isEmpty());
    assertEquals(Outer.Middle.class, ofString.enclosingType.getRuntimeClass());
    ClassOrInterfaceType outerOfString = ofString.enclosingType.enclosingType;
    assertEquals(Outer.class, outerOfString.getRuntimeClass());
    assertEquals(
        java.util.Collections.singletonList(TypeArgument.forType(JavaTypes.STRING_TYPE)),
        outerOfString.getTypeArguments());

    ClassOrInterfaceType ofU = ClassOrInterfaceType.forType(parameterTypes[1]);
    assertTrue(ofU.isGeneric());
    assertFalse(ofU.isGenericForm());
    assertEquals(1, ofU.getTypeParameters().size());
    assertEquals("U", ofU.getTypeParameters().get(0).getSimpleName());
  }
}
