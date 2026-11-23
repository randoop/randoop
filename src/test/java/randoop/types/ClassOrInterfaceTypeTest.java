package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    /** An inner class that declares no type parameters and has inner classes. */
    class Middle {
      /** An inner class whose enclosing types are generic only through {@link Outer}. */
      class Inner {}

      /** An inner class of an inner class whose enclosing type is generic. */
      class Deepest {}
    }

    /**
     * A generic inner class that has an inner class.
     *
     * @param <M> the type parameter
     */
    class GenericMiddle<M> {
      /** An inner class whose enclosing types are generic. */
      class Inner {}
    }

    /**
     * A generic inner class.
     *
     * @param <I> the type parameter
     */
    class GenericInner<I> {}

    /** An inner class whose superclass is a generic inner class of the same class. */
    class StringInner extends GenericInner<String> {}
  }

  /** A subclass of {@code Outer<Integer>.StringInner}. */
  static class StringInnerSubclass extends Outer<Integer>.StringInner {
    /**
     * Creates a StringInnerSubclass.
     *
     * @param outer the enclosing instance of the superclass
     */
    StringInnerSubclass(Outer<Integer> outer) {
      outer.super();
    }
  }

  /** A class that is not generic, with generic inner classes. */
  static class NonGenericOuter {
    /**
     * A generic inner class.
     *
     * @param <B> the type parameter
     */
    class Base<B> {}

    /**
     * A generic inner class whose superclass is a generic inner class of the same class.
     *
     * @param <S> the type parameter
     */
    class Sub<S> extends Base<S> {}
  }

  /** Declares methods whose parameter types are member classes of {@link Outer}. */
  static class MemberTypeHolder {
    /**
     * A method whose parameter type is generic only through an enclosing type of its enclosing
     * type.
     *
     * @param <U> the type argument of the outermost enclosing type
     * @param inner an inner object
     */
    static <U> void middleInner(Outer<U>.Middle.Inner inner) {}

    /**
     * A method whose parameter type has an enclosing type with a fixed type argument.
     *
     * @param <U> the type argument of the outermost enclosing type
     * @param inner an inner object
     */
    static <U> void genericMiddleInner(Outer<U>.GenericMiddle<String>.Inner inner) {}

    /**
     * A method whose parameter type has an enclosing type that is not generic.
     *
     * @param <U> the type argument of the parameter type
     * @param inner an inner object
     */
    static <U> void fixedOwner(Outer<String>.GenericInner<U> inner) {}

    /**
     * A method whose parameter type has an enclosing type with a parameterized type argument.
     *
     * @param <U> the type argument of the type argument of the enclosing type
     * @param <V> the type argument of the parameter type
     * @param inner an inner object
     */
    static <U, V> void setOwner(Outer<Set<U>>.GenericInner<V> inner) {}
  }

  /**
   * Returns the type of the parameter of the given method of {@link MemberTypeHolder}.
   *
   * @param name the name of a method of {@link MemberTypeHolder}
   * @param parameterClass the class of the method's parameter
   * @return the type of the parameter
   */
  private static ClassOrInterfaceType memberParameterType(String name, Class<?> parameterClass)
      throws NoSuchMethodException {
    return ClassOrInterfaceType.forType(
        MemberTypeHolder.class.getDeclaredMethod(name, parameterClass)
            .getGenericParameterTypes()[0]);
  }

  /**
   * Returns {@code Outer<outerArgument>.GenericInner<innerArgument>}.
   *
   * @param outerArgument the type argument of {@link Outer}
   * @param innerArgument the type argument of {@link Outer.GenericInner}
   * @return the type {@code Outer<outerArgument>.GenericInner<innerArgument>}
   */
  private static ClassOrInterfaceType genericInnerType(
      ReferenceType outerArgument, ReferenceType innerArgument) {
    ClassOrInterfaceType genericForm = ClassOrInterfaceType.forClass(Outer.GenericInner.class);
    // The type parameters are those of Outer, then that of Outer.GenericInner.
    return genericForm.substitute(
        new Substitution(
            genericForm.getTypeParameters(), Arrays.asList(outerArgument, innerArgument)));
  }

  @Test
  public void nestedMemberClassForTypeTest() throws NoSuchMethodException {
    String outerName = Outer.class.getCanonicalName();

    // Outer<U>.Middle.Inner, where U is the method's type parameter.
    ClassOrInterfaceType middleInner = memberParameterType("middleInner", Outer.Middle.Inner.class);
    assertFalse(middleInner.isGenericForm());
    List<TypeVariable> middleInnerParameters = middleInner.getTypeParameters();
    assertEquals(1, middleInnerParameters.size());
    assertEquals(
        outerName + "<java.lang.Integer>.Middle.Inner",
        middleInner
            .substitute(
                new Substitution(middleInnerParameters, JavaTypes.INT_TYPE.toBoxedPrimitive()))
            .getFqName());

    // Outer<U>.GenericMiddle<String>.Inner, where U is the method's type parameter.
    ClassOrInterfaceType genericMiddleInner =
        memberParameterType("genericMiddleInner", Outer.GenericMiddle.Inner.class);
    assertFalse(genericMiddleInner.isGenericForm());
    List<TypeVariable> genericMiddleInnerParameters = genericMiddleInner.getTypeParameters();
    assertEquals(1, genericMiddleInnerParameters.size());
    assertEquals(
        outerName + "<java.lang.Integer>.GenericMiddle<java.lang.String>.Inner",
        genericMiddleInner
            .substitute(
                new Substitution(
                    genericMiddleInnerParameters, JavaTypes.INT_TYPE.toBoxedPrimitive()))
            .getFqName());
  }

  @Test
  public void memberClassEnclosingTypeSubstitutionTest() throws NoSuchMethodException {
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();

    // Outer<Integer>.GenericInner<String> does not instantiate Outer<String>.GenericInner<U>.
    ClassOrInterfaceType fixedOwner = memberParameterType("fixedOwner", Outer.GenericInner.class);
    assertNull(
        genericInnerType(integerType, JavaTypes.STRING_TYPE)
            .getInstantiatingSubstitution(fixedOwner));
    assertNotNull(
        genericInnerType(JavaTypes.STRING_TYPE, JavaTypes.STRING_TYPE)
            .getInstantiatingSubstitution(fixedOwner));

    // Outer<List<String>>.GenericInner<String> does not instantiate Outer<Set<U>>.GenericInner<V>,
    // even though the type argument of GenericInner instantiates V.
    ClassOrInterfaceType setOwner = memberParameterType("setOwner", Outer.GenericInner.class);
    assertNull(
        genericInnerType(
                JDKTypes.LIST_TYPE.instantiate(JavaTypes.STRING_TYPE), JavaTypes.STRING_TYPE)
            .getInstantiatingSubstitution(setOwner));
    Substitution substitution =
        genericInnerType(JDKTypes.SET_TYPE.instantiate(integerType), JavaTypes.STRING_TYPE)
            .getInstantiatingSubstitution(setOwner);
    assertNotNull(substitution);
    List<TypeVariable> setOwnerParameters = setOwner.getTypeParameters();
    assertEquals(integerType, substitution.get(setOwnerParameters.get(0)));
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(setOwnerParameters.get(1)));
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
  public void lowerBoundedWildcardContainmentTest() {
    WildcardType superString =
        new WildcardType(new EagerReferenceBound(JavaTypes.STRING_TYPE), false);
    WildcardType superObject =
        new WildcardType(new EagerReferenceBound(JavaTypes.OBJECT_TYPE), false);
    WildcardType unbounded = new WildcardType(new EagerReferenceBound(JavaTypes.OBJECT_TYPE), true);
    // JLS 4.5.1: `? super T` contains `? super S` if T is a subtype of S.
    assertTrue(superString.contains(superObject));
    assertFalse(superObject.contains(superString));
    // `? super T` does not contain `?`, which is `? extends Object`.
    assertFalse(superString.contains(unbounded));
    // `List<?>` is not a subtype of `List<? super String>`.
    GenericClassType listType = (GenericClassType) ClassOrInterfaceType.forClass(List.class);
    InstantiatedType listOfUnbounded =
        new InstantiatedType(
            listType, Collections.singletonList((TypeArgument) new WildcardArgument(unbounded)));
    InstantiatedType listOfSuperString =
        new InstantiatedType(
            listType, Collections.singletonList((TypeArgument) new WildcardArgument(superString)));
    assertFalse(listOfUnbounded.isSubtypeOfOrEqualTo(listOfSuperString));
    assertTrue(listOfSuperString.isSubtypeOfOrEqualTo(listOfUnbounded));
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

  /**
   * The supertype of {@code StringInnerSubclass} that matches {@code Outer.GenericInner} is {@code
   * GenericInner<String>}, not its superclass {@code Outer<Integer>.StringInner}, whose enclosing
   * type instantiates that of {@code Outer.GenericInner}.
   */
  @Test
  public void memberClassMatchingSupertypeTest() {
    GenericClassType genericInner =
        (GenericClassType) ClassOrInterfaceType.forClass(Outer.GenericInner.class);
    InstantiatedType supertype =
        ClassOrInterfaceType.forClass(StringInnerSubclass.class).getMatchingSupertype(genericInner);
    assertNotNull(supertype);
    assertTrue(supertype.runtimeClassIs(Outer.GenericInner.class));
    assertEquals(
        Collections.singletonList(TypeArgument.forType(JavaTypes.STRING_TYPE)),
        supertype.getTypeArguments());
  }

  /**
   * The supertype of {@code NonGenericOuter.Sub<String>} that matches {@code NonGenericOuter.Base}
   * is {@code Base<String>}, not {@code Sub<String>} itself, whose enclosing type instantiates that
   * of {@code NonGenericOuter.Base}.
   */
  @Test
  public void genericMemberClassMatchingSupertypeTest() {
    GenericClassType genericBase =
        (GenericClassType) ClassOrInterfaceType.forClass(NonGenericOuter.Base.class);
    ClassOrInterfaceType genericSub = ClassOrInterfaceType.forClass(NonGenericOuter.Sub.class);
    ClassOrInterfaceType stringSub =
        genericSub.substitute(
            new Substitution(genericSub.getTypeParameters(), JavaTypes.STRING_TYPE));
    InstantiatedType supertype = stringSub.getMatchingSupertype(genericBase);
    assertNotNull(supertype);
    assertTrue(supertype.runtimeClassIs(NonGenericOuter.Base.class));
    assertEquals(
        Collections.singletonList(TypeArgument.forType(JavaTypes.STRING_TYPE)),
        supertype.getTypeArguments());
  }

  /**
   * {@code Outer<String>.GenericInner<Integer>} is a subtype of itself, but not of {@code
   * Outer<Long>.GenericInner<Integer>}, whose enclosing type is unrelated.
   */
  @Test
  public void memberClassEnclosingTypeSubtypeTest() {
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    ReferenceType longType = JavaTypes.LONG_TYPE.toBoxedPrimitive();
    ClassOrInterfaceType stringInteger = genericInnerType(JavaTypes.STRING_TYPE, integerType);
    assertTrue(
        stringInteger.isSubtypeOfOrEqualTo(genericInnerType(JavaTypes.STRING_TYPE, integerType)));
    assertFalse(stringInteger.isSubtypeOfOrEqualTo(genericInnerType(longType, integerType)));
  }
}
