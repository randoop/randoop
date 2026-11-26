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
import java.util.function.Supplier;
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

  /** A class with a member class whose superclass is a member class of a parameterized type. */
  static class OtherOuter {
    /** A member class whose superclass is {@code GenericOuter<String>.GenericInner<Integer>}. */
    @SuppressWarnings("ClassCanBeStatic")
    class Derived extends GenericOuter<String>.GenericInner<Integer> {
      /**
       * Creates a Derived.
       *
       * @param outer the enclosing instance of the superclass
       */
      Derived(GenericOuter<String> outer) {
        outer.super();
      }
    }
  }

  @Test
  public void instantiatingSubstitutionOfMemberSubclass() {
    ClassOrInterfaceType derived = ClassOrInterfaceType.forClass(OtherOuter.Derived.class);
    GenericClassType innerType =
        (GenericClassType) ClassOrInterfaceType.forClass(GenericOuter.GenericInner.class);
    TypeVariable x = GenericClassType.forClass(List.class).getTypeParameters().get(0);
    // GenericOuter<String>.GenericInner<X>.  The enclosing type of Derived, OtherOuter, is not
    // compared with GenericOuter<String>.
    InstantiatedType stringX = innerType.instantiate(JavaTypes.STRING_TYPE, x);
    Substitution substitution = derived.getInstantiatingSubstitution(stringX);
    assertNotNull(substitution);
    assertEquals(JavaTypes.INT_TYPE.toBoxedPrimitive(), substitution.get(x));
  }

  /** Containment of a member class of a parameterized type compares the enclosing types. */
  @Test
  public void wildcardContainmentComparesEnclosingTypes() {
    GenericClassType innerType =
        (GenericClassType) ClassOrInterfaceType.forClass(GenericOuter.GenericInner.class);
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    // GenericOuter<Integer>.GenericInner<String>
    InstantiatedType integerString = innerType.instantiate(integerType, JavaTypes.STRING_TYPE);
    // GenericOuter<String>.GenericInner<String>
    InstantiatedType stringString =
        innerType.instantiate(JavaTypes.STRING_TYPE, JavaTypes.STRING_TYPE);
    assertFalse(integerString.isSubtypeOfOrEqualTo(stringString));
    assertFalse(stringString.isSubtypeOfOrEqualTo(integerString));

    for (boolean hasUpperBound : new boolean[] {true, false}) {
      WildcardArgument wildcard =
          WildcardArgument.forType(
              new WildcardType(new EagerReferenceBound(stringString), hasUpperBound));
      assertFalse(wildcard.contains(TypeArgument.forType(integerString)));
      assertTrue(wildcard.contains(TypeArgument.forType(stringString)));
      // A wildcard bounded by a member class with another enclosing type is not contained.
      WildcardArgument integerWildcard =
          WildcardArgument.forType(
              new WildcardType(new EagerReferenceBound(integerString), hasUpperBound));
      assertFalse(wildcard.contains(integerWildcard));
      assertTrue(
          wildcard.contains(
              WildcardArgument.forType(
                  new WildcardType(new EagerReferenceBound(stringString), hasUpperBound))));
    }

    // A bound is a lower bound of another only if their enclosing types match.
    ParameterBound stringBound = new EagerReferenceBound(stringString);
    ParameterBound integerBound = new EagerReferenceBound(integerString);
    assertTrue(stringBound.isLowerBound(stringBound, new Substitution()));
    assertFalse(integerBound.isLowerBound(stringBound, new Substitution()));
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

    /** An inner class that declares no type parameters and has inner classes. */
    class Middle {
      /** An inner class whose enclosing types are generic only through {@link Outer}. */
      class Inner {}

      /** An inner class of an inner class whose enclosing type is generic. */
      class Deepest {}

      /** An inner class of an inner class, whose superclass mentions {@code T}. */
      @SuppressWarnings("serial")
      class DeepList extends ArrayList<T> {}
    }

    /** An inner class whose supertypes mention the type parameter of the enclosing class. */
    @SuppressWarnings("serial")
    class ListInner extends ArrayList<T> implements Supplier<T> {
      @Override
      public T get() {
        return get(0);
      }
    }

    /** An inner class whose supertypes mention {@code T}. */
    @SuppressWarnings("serial")
    abstract class ListOfT extends ArrayList<T> implements Supplier<T> {}

    /**
     * A generic inner class whose supertypes mention {@code T} and its own type parameter.
     *
     * @param <U> the type parameter
     */
    abstract class GenericChild<U> extends Inner implements java.util.Map<T, U> {}

    /**
     * A generic inner class whose enclosing type is generic.
     *
     * @param <U> a type parameter
     */
    class GenericMiddle<U> {
      /** A non-generic inner class of a generic inner class, whose supertype mentions {@code T}. */
      abstract class Leaf implements Supplier<T> {}

      /** An inner class whose enclosing types are generic. */
      class Inner {}
    }

    /** An inner class that is extended by an inner class with a different enclosing type. */
    class Parent {}

    /** An inner class whose superclass has an enclosing type that differs from its own. */
    class Child extends Outer<Integer>.Parent {
      /**
       * Creates a Child.
       *
       * @param enclosing the enclosing instance of the superclass
       */
      Child(Outer<Integer> enclosing) {
        enclosing.super();
      }
    }

    /**
     * A generic inner class whose supertypes mention its type parameter.
     *
     * @param <U> the type parameter
     */
    abstract class GenericInner<U> extends Holder<U> implements Comparable<U> {}

    /**
     * A generic inner class whose supertypes mention the type parameter of the enclosing class.
     *
     * @param <U> the type parameter
     */
    abstract class EnclosingSupertypeInner<U> extends Holder<T> implements Comparable<U> {}

    /**
     * A generic inner class whose type parameter has a bound.
     *
     * @param <U> the type parameter
     */
    class NumberInner<U extends Number> {}

    /**
     * A generic inner class whose type parameter has a bound that mentions the type parameter of
     * the enclosing class.
     *
     * @param <U> the type parameter
     */
    class ComparableInner<U extends Comparable<T>> {}

    /** An inner class whose supertypes mention the type parameter of the enclosing class. */
    abstract class NonGenericSupertypeInner extends Holder<T> implements Comparable<T> {}

    /** An inner class whose supertypes mention an array of the type parameter of the class. */
    abstract class ArraySupertypeInner extends Holder<T[]> implements Comparable<T[]> {}

    /**
     * An inner class whose superclass is a member class of another instantiation of {@link Outer}.
     */
    abstract class OtherOuterSubInner extends Outer<Integer>.GenericInner<String> {
      /** Creates an OtherOuterSubInner. */
      OtherOuterSubInner() {
        new Outer<Integer>().super();
      }
    }

    /** An inner class whose superclass is a generic inner class of the same class. */
    abstract class StringInner extends GenericInner<String> {}

    /** A field whose type, {@code Outer<T>.GenericInner<Integer>}, has a declaration-form owner. */
    @SuppressWarnings("UnusedVariable") // the field is read reflectively
    GenericInner<Integer> declarationFormOwner;

    /**
     * A generic inner class whose superclass mentions {@code T}.
     *
     * @param <V> the type parameter
     */
    @SuppressWarnings("serial")
    abstract class GenericListOfT<V> extends ArrayList<T> implements Supplier<V> {}
  }

  /** Declares a method whose parameter type is a generic member class of a parameterized type. */
  static class GenericMemberHolder {
    /**
     * A method whose parameter type is a generic member class of a parameterized type.
     *
     * @param list an {@code Outer<String>.GenericListOfT<Integer>}
     */
    static void m(Outer<String>.GenericListOfT<Integer> list) {}
  }

  /** A class that is not generic, with a generic inner class. */
  static class Plain {
    /**
     * A generic inner class of a class that is not generic.
     *
     * @param <U> the type parameter
     */
    class Inner<U> {}
  }

  /** A field whose type is {@code Plain.Inner<String>}. */
  public Plain.Inner<String> plainInnerOfString;

  /**
   * A class whose interface has a type argument that mentions the type parameter.
   *
   * @param <T> the type parameter
   */
  abstract static class ListComparable<T> implements Comparable<List<T>> {}

  /** A field whose type is {@code ListComparable<?>}. */
  public ListComparable<?> listComparableOfWildcard;

  /** A field whose type is {@code Comparable<List<?>>}. */
  public Comparable<List<?>> comparableOfListOfWildcard;

  /** A field whose type is {@code Comparable<? extends List<?>>}. */
  public Comparable<? extends List<?>> comparableOfExtendsListOfWildcard;

  /**
   * A top-level class that inherits a non-generic member class of a parameterized type determines
   * the type argument of the enclosing type.
   */
  @Test
  public void topLevelSubclassOfMemberClassSubstitutionTest() {
    ClassOrInterfaceType goal = ClassOrInterfaceType.forClass(Outer.StringInner.class);
    TypeVariable t = GenericClassType.forClass(Outer.class).getTypeParameters().get(0);
    Substitution substitution =
        ClassOrInterfaceType.forClass(StringInnerSubclass.class).getInstantiatingSubstitution(goal);
    assertNotNull(substitution);
    assertEquals(JavaTypes.INT_TYPE.toBoxedPrimitive(), substitution.get(t));
  }

  /** A reflected member class of a non-generic class equals the same type created directly. */
  @Test
  public void reflectedMemberOfNonGenericClassEqualityTest() {
    InstantiatedType reflected = fieldType("plainInnerOfString");
    InstantiatedType instantiated =
        ((GenericClassType) ClassOrInterfaceType.forClass(Plain.Inner.class))
            .instantiate(JavaTypes.STRING_TYPE);
    assertTrue(reflected.isMemberClass());
    assertEquals(instantiated, reflected);
    assertEquals(instantiated.hashCode(), reflected.hashCode());
  }

  /** Each type argument is checked against the bound of its own type parameter. */
  @Test
  public void instantiateMemberClassChecksBoundsTest() {
    GenericClassType numberInner =
        (GenericClassType) ClassOrInterfaceType.forClass(Outer.NumberInner.class);
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    // Outer<String>.NumberInner<Integer> is valid.
    numberInner.instantiate(JavaTypes.STRING_TYPE, integerType);
    numberInner.instantiate(Arrays.asList(JavaTypes.STRING_TYPE, integerType));
    // Outer<Integer>.NumberInner<String> is not.
    try {
      numberInner.instantiate(integerType, JavaTypes.STRING_TYPE);
      throw new AssertionError("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      numberInner.instantiate(Arrays.asList(integerType, JavaTypes.STRING_TYPE));
      throw new AssertionError("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  /** The supertypes of a type with a wildcard argument are those of its capture conversion. */
  @Test
  public void captureBeforeSupertypeTest() {
    InstantiatedType listComparable = fieldType("listComparableOfWildcard");
    Substitution empty = new Substitution();
    assertFalse(
        Subtyping.isSubtypeOrEqual(listComparable, fieldType("comparableOfListOfWildcard"), empty));
    assertTrue(
        Subtyping.isSubtypeOrEqual(
            listComparable, fieldType("comparableOfExtendsListOfWildcard"), empty));
  }

  /** The type parameter in a declaration-form owner stands for itself. */
  @Test
  public void declarationFormOwnerSubtypeTest() throws NoSuchFieldException {
    ReferenceType declarationForm =
        ReferenceType.forType(
            Outer.class.getDeclaredField("declarationFormOwner").getGenericType());
    assertTrue(((ClassOrInterfaceType) declarationForm).isMemberClass());
    ClassOrInterfaceType stringInteger =
        genericInnerType(JavaTypes.STRING_TYPE, JavaTypes.INT_TYPE.toBoxedPrimitive());
    Substitution empty = new Substitution();
    assertTrue(Subtyping.isSubtypeOrEqual(declarationForm, declarationForm, empty));
    assertFalse(Subtyping.isSubtypeOrEqual(stringInteger, declarationForm, empty));
    assertFalse(Subtyping.isSubtypeOrEqual(declarationForm, stringInteger, empty));
  }

  /** Methods whose return types {@link #memberOfWildcardTypeHasWildcardTest} uses. */
  @SuppressWarnings("unused")
  abstract static class WildcardHolder {
    abstract Outer<?> wildcardOuter();

    abstract Outer<String> stringOuter();

    abstract Outer<String>.GenericInner<String> genericInner();
  }

  /**
   * Returns the Randoop type for the return type of the given method of {@link WildcardHolder}.
   *
   * @param methodName a method name of {@link WildcardHolder}, such as {@code "wildcardOuter"}
   * @return the return type of the method
   * @throws NoSuchMethodException if {@link WildcardHolder} has no method named {@code methodName}
   */
  private static ClassOrInterfaceType wildcardHolderReturnType(String methodName)
      throws NoSuchMethodException {
    return (ClassOrInterfaceType)
        Type.forType(WildcardHolder.class.getDeclaredMethod(methodName).getGenericReturnType());
  }

  /** A subclass of {@code Outer<Integer>.StringInner}. */
  abstract static class StringInnerSubclass extends Outer<Integer>.StringInner {
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

  /** Declares methods whose parameter types are member classes of parameterized types. */
  static class OwnerHolder {
    /**
     * A method whose parameter types are member classes of parameterized types.
     *
     * @param leaf an {@code Outer<String>.GenericMiddle<Integer>.Leaf}
     * @param child an {@code Outer<String>.Child}
     * @param inner an {@code Outer<String>.Inner}
     * @param extendsInner a list whose elements are subtypes of {@code Outer<String>.Inner}
     */
    static void m(
        Outer<String>.GenericMiddle<Integer>.Leaf leaf,
        Outer<String>.Child child,
        Outer<String>.Inner inner,
        List<? extends Outer<String>.Inner> extendsInner) {}
  }

  /**
   * Returns the parameter types of {@link OwnerHolder#m}.
   *
   * @return the parameter types of {@link OwnerHolder#m}
   */
  private static java.lang.reflect.Type[] ownerHolderParameterTypes() throws NoSuchMethodException {
    return OwnerHolder.class
        .getDeclaredMethod(
            "m", Outer.GenericMiddle.Leaf.class, Outer.Child.class, Outer.Inner.class, List.class)
        .getGenericParameterTypes();
  }

  /**
   * A non-generic member class of a generic member class keeps the outermost enclosing type, whose
   * type arguments instantiate its supertypes.
   */
  @Test
  public void memberOfGenericMemberClassSupertypesTest() throws NoSuchMethodException {
    ClassOrInterfaceType leaf = ClassOrInterfaceType.forType(ownerHolderParameterTypes()[0]);
    assertEquals(Outer.GenericMiddle.class, leaf.enclosingType.getRuntimeClass());
    assertEquals(Outer.class, leaf.enclosingType.enclosingType.getRuntimeClass());
    assertEquals(
        java.util.Collections.singletonList(
            GenericClassType.forClass(Supplier.class).instantiate(JavaTypes.STRING_TYPE)),
        leaf.getInterfaces());
  }

  /**
   * The substitution that instantiates a member class from a subclass uses the enclosing type of
   * the superclass, not that of the subclass.
   */
  @Test
  public void inheritedMemberClassSubstitutionTest() throws NoSuchMethodException {
    ClassOrInterfaceType child = ClassOrInterfaceType.forType(ownerHolderParameterTypes()[1]);
    ClassOrInterfaceType parent = ClassOrInterfaceType.forClass(Outer.Parent.class);
    TypeVariable t = GenericClassType.forClass(Outer.class).getTypeParameters().get(0);
    Substitution substitution = child.getInstantiatingSubstitution(parent);
    assertNotNull(substitution);
    assertEquals(ClassOrInterfaceType.forClass(Integer.class), substitution.get(t));
  }

  /** A parameterized type is not a subtype of a member class of a parameterized type. */
  @Test
  public void parameterizedTypeNotSubtypeOfMemberClassTest() throws NoSuchMethodException {
    ClassOrInterfaceType inner = ClassOrInterfaceType.forType(ownerHolderParameterTypes()[2]);
    InstantiatedType listOfString = JDKTypes.LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    assertFalse(listOfString.isSubtypeOfOrEqualTo(inner));
  }

  /** A wildcard bounded by a member class of a parameterized type contains that member class. */
  @Test
  public void wildcardBoundedByMemberClassTest() throws NoSuchMethodException {
    java.lang.reflect.Type[] parameterTypes = ownerHolderParameterTypes();
    ClassOrInterfaceType inner = ClassOrInterfaceType.forType(parameterTypes[2]);
    InstantiatedType extendsInner =
        (InstantiatedType) ClassOrInterfaceType.forType(parameterTypes[3]);
    TypeArgument wildcard = extendsInner.getTypeArguments().get(0);
    assertTrue(wildcard.contains(TypeArgument.forType(inner)));
    assertFalse(wildcard.contains(TypeArgument.forType(JavaTypes.STRING_TYPE)));
    assertTrue(JDKTypes.LIST_TYPE.instantiate(inner).isSubtypeOfOrEqualTo(extendsInner));
  }

  @Test
  public void supertypesOfMemberOfParameterizedType() {
    GenericClassType outerType = (GenericClassType) ClassOrInterfaceType.forClass(Outer.class);
    ClassOrInterfaceType listInnerOfString =
        ClassOrInterfaceType.forClass(Outer.ListInner.class)
            .substitute(new Substitution(outerType.getTypeParameters(), JavaTypes.STRING_TYPE));
    // The supertypes of Outer<String>.ListInner are ArrayList<String> and Supplier<String>.
    assertEquals(
        GenericClassType.forClass(ArrayList.class).instantiate(JavaTypes.STRING_TYPE),
        listInnerOfString.getSuperclass());
    assertEquals(
        Collections.singletonList(
            GenericClassType.forClass(Supplier.class).instantiate(JavaTypes.STRING_TYPE)),
        listInnerOfString.getInterfaces());
  }

  /** Declares methods whose parameter types are used in supertype tests. */
  static class SupertypeHolder {
    /**
     * A method whose parameter types are non-generic member classes of parameterized types.
     *
     * @param list an {@code Outer<String>.ListOfT}
     * @param deep an {@code Outer<String>.Middle.DeepList}
     */
    static void m(Outer<String>.ListOfT list, Outer<String>.Middle.DeepList deep) {}

    /**
     * A method whose parameter types are member classes of parameterized types.
     *
     * @param child an {@code Outer<String>.GenericChild<Integer>}
     * @param inner an {@code Outer<String>.Inner}
     */
    static void generic(Outer<String>.GenericChild<Integer> child, Outer<String>.Inner inner) {}
  }

  /**
   * The supertypes of a generic member class of a parameterized type are instantiated by the type
   * arguments of the enclosing type, as well as by those of the member class.
   */
  @Test
  public void genericMemberSubclassSupertypesTest() throws NoSuchMethodException {
    java.lang.reflect.Type[] parameterTypes =
        SupertypeHolder.class
            .getDeclaredMethod("generic", Outer.GenericChild.class, Outer.Inner.class)
            .getGenericParameterTypes();
    ClassOrInterfaceType child = ClassOrInterfaceType.forType(parameterTypes[0]);
    ClassOrInterfaceType innerOfString = ClassOrInterfaceType.forType(parameterTypes[1]);
    assertTrue(child instanceof InstantiatedType);

    assertTrue(Subtyping.isIdentical(innerOfString, child.getSuperclass()));
    assertTrue(child.isSubtypeOfOrEqualTo(innerOfString));

    InstantiatedType mapOfStringInteger =
        JDKTypes.MAP_TYPE.instantiate(JavaTypes.STRING_TYPE, JavaTypes.INT_TYPE.toBoxedPrimitive());
    assertEquals(Collections.singletonList(mapOfStringInteger), child.getInterfaces());
    assertTrue(child.isSubtypeOfOrEqualTo(mapOfStringInteger));
  }

  /**
   * The supertypes of a non-generic member class of a parameterized type are instantiated by the
   * type arguments of the enclosing type.
   */
  @Test
  public void memberClassSupertypesTest() throws NoSuchMethodException {
    java.lang.reflect.Type[] parameterTypes =
        SupertypeHolder.class
            .getDeclaredMethod("m", Outer.ListOfT.class, Outer.Middle.DeepList.class)
            .getGenericParameterTypes();
    ClassOrInterfaceType listOfString = ClassOrInterfaceType.forType(parameterTypes[0]);
    InstantiatedType arrayListOfString =
        JDKTypes.ARRAY_LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    assertEquals(arrayListOfString, listOfString.getSuperclass());
    assertEquals(
        java.util.Collections.singletonList(
            GenericClassType.forClass(Supplier.class).instantiate(JavaTypes.STRING_TYPE)),
        listOfString.getInterfaces());

    ClassOrInterfaceType deep = ClassOrInterfaceType.forType(parameterTypes[1]);
    assertEquals(arrayListOfString, deep.getSuperclass());

    // The generic form keeps the declared type variable.
    ClassOrInterfaceType genericForm = ClassOrInterfaceType.forClass(Outer.ListOfT.class);
    assertTrue(genericForm.getSuperclass().isGeneric());
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

    /**
     * A method whose parameter type uses the same type variable in its enclosing type and as its
     * own type argument.
     *
     * @param <X> the type argument of the enclosing type and of the parameter type
     * @param inner an inner object
     */
    static <X> void sameVariable(Outer<X>.GenericInner<X> inner) {}
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
   * A generic class.
   *
   * @param <V> the type parameter
   */
  static class Holder<V> {}

  /** A field of type {@code Outer<String>.GenericInner<Integer>}. */
  public Outer<String>.GenericInner<Integer> genericInnerOfInteger;

  /** A field of type {@code Outer<String>.NumberInner<?>}. */
  public Outer<String>.NumberInner<?> numberInnerOfWildcard;

  /** A field of type {@code Outer<String>.EnclosingSupertypeInner<Integer>}. */
  public Outer<String>.EnclosingSupertypeInner<Integer> enclosingSupertypeInnerOfInteger;

  /** A field of type {@code Outer<String>.ComparableInner<?>}. */
  public Outer<String>.ComparableInner<?> comparableInnerOfWildcard;

  /** A field of type {@code Outer<?>.GenericInner<Integer>}. */
  public Outer<?>.GenericInner<Integer> genericInnerOfWildcardOuter;

  /** A field of type {@code Outer<String>.NonGenericSupertypeInner}. */
  public Outer<String>.NonGenericSupertypeInner nonGenericSupertypeInner;

  /** A field of type {@code Outer<? extends Number>.ArraySupertypeInner}. */
  public Outer<? extends Number>.ArraySupertypeInner arraySupertypeInnerOfWildcardOuter;

  /** A field of type {@code Outer<String>.Inner}. */
  public Outer<String>.Inner innerOfString;

  /** A field of type {@code Outer<?>.Inner}. */
  public Outer<?>.Inner innerOfWildcard;

  /** A field of type {@code Outer<Long>.Inner}. */
  public Outer<Long>.Inner innerOfLong;

  /** A field of type {@code NonGenericOuter.Base<String>}. */
  public NonGenericOuter.Base<String> baseOfString;

  /** A field of type {@code Outer<String>.OtherOuterSubInner}. */
  public Outer<String>.OtherOuterSubInner otherOuterSubInner;

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

  /**
   * Returns the type of the given field of this class.
   *
   * @param name the name of the field
   * @return the type of the field
   */
  private static InstantiatedType fieldType(String name) {
    try {
      return (InstantiatedType)
          Type.forType(ClassOrInterfaceTypeTest.class.getField(name).getGenericType());
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
  }

  @Test
  public void genericMemberOfGenericClassTest() {
    // ClassOrInterfaceType.forClass gives the generic form of a member class of a generic class
    // an enclosing type, so the generic form has the type parameters of the enclosing class as
    // well as its own, such as [T, U] for Outer<T>.GenericInner<U>.  A type argument, such as
    // Integer in Outer<String>.GenericInner<Integer>, corresponds to a type parameter that the
    // member class declares, such as U.
    ClassOrInterfaceType.forClass(Outer.GenericInner.class);
    ClassOrInterfaceType.forClass(Outer.NumberInner.class);
    ReferenceType integerType = ReferenceType.forClass(Integer.class);

    InstantiatedType genericInnerOfInteger = fieldType("genericInnerOfInteger");
    assertEquals(
        ((GenericClassType) ClassOrInterfaceType.forClass(Holder.class)).instantiate(integerType),
        genericInnerOfInteger.getSuperclass());
    assertEquals(
        Collections.singletonList(JavaTypes.COMPARABLE_TYPE.instantiate(integerType)),
        genericInnerOfInteger.getInterfaces());

    // The capture variable has the bound of the type parameter of NumberInner, not of Outer.
    InstantiatedType captured = fieldType("numberInnerOfWildcard").applyCaptureConversion();
    ReferenceType capture =
        ((ReferenceArgument) captured.getTypeArguments().get(0)).getReferenceType();
    assertTrue(capture.isCaptureVariable());
    ParameterBound upperBound = ((CaptureTypeVariable) capture).getUpperTypeBound();
    Substitution substitution = new Substitution();
    assertTrue(upperBound.isUpperBound(integerType, substitution));
    assertFalse(upperBound.isUpperBound(JavaTypes.STRING_TYPE, substitution));
  }

  @Test
  public void genericMemberOfGenericClassEnclosingTypeArgumentTest() {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    ReferenceType stringType = JavaTypes.STRING_TYPE;
    GenericClassType holderType = (GenericClassType) ClassOrInterfaceType.forClass(Holder.class);

    // The substitution maps the type parameter of the enclosing class, as well as the type
    // parameter that the member class declares.
    GenericClassType genericInnerType =
        (GenericClassType) ClassOrInterfaceType.forClass(Outer.GenericInner.class);
    List<TypeVariable> genericInnerParameters = genericInnerType.getTypeParameters();
    assertEquals(2, genericInnerParameters.size());
    Substitution typeSubstitution = fieldType("genericInnerOfInteger").getTypeSubstitution();
    assertNotNull(typeSubstitution);
    assertEquals(stringType, typeSubstitution.get(genericInnerParameters.get(0)));
    assertEquals(integerType, typeSubstitution.get(genericInnerParameters.get(1)));

    // There is no substitution for a wildcard type argument of the enclosing type.
    assertNull(fieldType("genericInnerOfWildcardOuter").getTypeSubstitution());

    // The supertypes substitute the type argument of the enclosing type.
    InstantiatedType enclosingSupertypeInner = fieldType("enclosingSupertypeInnerOfInteger");
    assertEquals(holderType.instantiate(stringType), enclosingSupertypeInner.getSuperclass());
    assertEquals(
        Collections.singletonList(JavaTypes.COMPARABLE_TYPE.instantiate(integerType)),
        enclosingSupertypeInner.getInterfaces());
    assertTrue(enclosingSupertypeInner.isSubtypeOfOrEqualTo(holderType.instantiate(stringType)));
    assertFalse(enclosingSupertypeInner.isSubtypeOfOrEqualTo(holderType.instantiate(integerType)));

    // The bound of the capture variable substitutes the type argument of the enclosing type:  it
    // is Comparable<String>, not Comparable<T>.
    InstantiatedType captured = fieldType("comparableInnerOfWildcard").applyCaptureConversion();
    ReferenceType capture =
        ((ReferenceArgument) captured.getTypeArguments().get(0)).getReferenceType();
    assertTrue(capture.isCaptureVariable());
    ParameterBound upperBound = ((CaptureTypeVariable) capture).getUpperTypeBound();
    Substitution substitution = new Substitution();
    assertTrue(upperBound.isUpperBound(stringType, substitution));
    assertFalse(upperBound.isUpperBound(integerType, substitution));
  }

  @Test
  public void memberSubclassOfOtherInstantiationTest() throws NoSuchFieldException {
    // Outer<String>.OtherOuterSubInner extends Outer<Integer>.GenericInner<String>, so it
    // instantiates the generic form of GenericInner with Integer for T, not String.
    ClassOrInterfaceType subInner =
        ClassOrInterfaceType.forType(
            ClassOrInterfaceTypeTest.class.getField("otherOuterSubInner").getGenericType());
    GenericClassType genericInnerType =
        (GenericClassType) ClassOrInterfaceType.forClass(Outer.GenericInner.class);
    Substitution substitution = subInner.getInstantiatingSubstitution(genericInnerType);
    assertNotNull(substitution);
    List<TypeVariable> genericInnerParameters = genericInnerType.getTypeParameters();
    assertEquals(
        JavaTypes.INT_TYPE.toBoxedPrimitive(), substitution.get(genericInnerParameters.get(0)));
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(genericInnerParameters.get(1)));
  }

  @Test
  public void nonGenericMemberSupertypesTest() throws NoSuchFieldException {
    // The supertypes of Outer<String>.NonGenericSupertypeInner substitute String for T.
    ClassOrInterfaceType inner =
        ClassOrInterfaceType.forType(
            ClassOrInterfaceTypeTest.class.getField("nonGenericSupertypeInner").getGenericType());
    assertTrue(inner instanceof NonParameterizedType);
    GenericClassType holderType = (GenericClassType) ClassOrInterfaceType.forClass(Holder.class);
    assertEquals(holderType.instantiate(JavaTypes.STRING_TYPE), inner.getSuperclass());
    assertEquals(
        Collections.singletonList(JavaTypes.COMPARABLE_TYPE.instantiate(JavaTypes.STRING_TYPE)),
        inner.getInterfaces());
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

  /**
   * The supertypes of a member class whose enclosing type has a wildcard argument are those of the
   * capture conversion of the enclosing type: the superclass of {@code Outer<? extends
   * Number>.ArraySupertypeInner} is {@code Holder<CAP[]>}, where {@code CAP} is a capture variable.
   */
  @Test
  public void memberOfWildcardTypeSupertypesTest() throws NoSuchFieldException {
    ClassOrInterfaceType inner =
        ClassOrInterfaceType.forType(
            ClassOrInterfaceTypeTest.class
                .getField("arraySupertypeInnerOfWildcardOuter")
                .getGenericType());
    ClassOrInterfaceType superclass = inner.getSuperclass();
    assertTrue(superclass.runtimeClassIs(Holder.class));
    assertCapturedNumberArrayArgument(superclass);
    List<ClassOrInterfaceType> interfaces = inner.getInterfaces();
    assertEquals(1, interfaces.size());
    assertTrue(interfaces.get(0).runtimeClassIs(Comparable.class));
    assertCapturedNumberArrayArgument(interfaces.get(0));
  }

  /**
   * Asserts that the only type argument of the given type is an array whose component type is a
   * capture variable with upper bound {@code Number}.
   *
   * @param type a parameterized type with one type argument
   */
  private static void assertCapturedNumberArrayArgument(ClassOrInterfaceType type) {
    List<TypeArgument> arguments = ((InstantiatedType) type).getTypeArguments();
    assertEquals(1, arguments.size());
    ReferenceType argument = ((ReferenceArgument) arguments.get(0)).getReferenceType();
    assertTrue(argument instanceof ArrayType);
    Type component = ((ArrayType) argument).getComponentType();
    assertTrue(component instanceof CaptureTypeVariable);
    ParameterBound upperBound = ((CaptureTypeVariable) component).getUpperTypeBound();
    Substitution empty = new Substitution();
    assertTrue(upperBound.isUpperBound(ReferenceType.forClass(Integer.class), empty));
    assertTrue(upperBound.isUpperBound(ReferenceType.forClass(Number.class), empty));
    assertFalse(upperBound.isUpperBound(JavaTypes.STRING_TYPE, empty));
    assertFalse(upperBound.isUpperBound(JavaTypes.OBJECT_TYPE, empty));
  }

  /**
   * Returns the type of the given field of this class.
   *
   * @param name the name of the field
   * @return the type of the field
   */
  private static ClassOrInterfaceType classFieldType(String name) {
    try {
      return ClassOrInterfaceType.forType(
          ClassOrInterfaceTypeTest.class.getField(name).getGenericType());
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
  }

  /**
   * {@code Outer<String>.Inner} is a subtype of {@code Outer<?>.Inner}, but not of {@code
   * Outer<Long>.Inner}.
   */
  @Test
  public void nonGenericMemberEnclosingTypeSubtypeTest() {
    ClassOrInterfaceType innerOfString = classFieldType("innerOfString");
    ClassOrInterfaceType innerOfWildcard = classFieldType("innerOfWildcard");
    ClassOrInterfaceType innerOfLong = classFieldType("innerOfLong");
    assertTrue(innerOfString.isSubtypeOfOrEqualTo(innerOfString));
    assertTrue(innerOfString.isSubtypeOfOrEqualTo(innerOfWildcard));
    assertFalse(innerOfString.isSubtypeOfOrEqualTo(innerOfLong));
    assertFalse(innerOfWildcard.isSubtypeOfOrEqualTo(innerOfString));
  }

  /** A top-level class whose superclass is {@code Outer<String>.Inner}. */
  static class StringInnerChild extends Outer<String>.Inner {
    /**
     * Creates a StringInnerChild.
     *
     * @param enclosing the enclosing instance of the superclass
     */
    StringInnerChild(Outer<String> enclosing) {
      enclosing.super();
    }
  }

  /**
   * A class that extends {@code Outer<String>.Inner} is a subtype of {@code Outer<?>.Inner}, but
   * not of {@code Outer<Long>.Inner}.
   */
  @Test
  public void inheritedNonGenericMemberSubtypeTest() {
    ClassOrInterfaceType child = ClassOrInterfaceType.forClass(StringInnerChild.class);
    assertTrue(child.isSubtypeOfOrEqualTo(classFieldType("innerOfString")));
    assertTrue(child.isSubtypeOfOrEqualTo(classFieldType("innerOfWildcard")));
    assertFalse(child.isSubtypeOfOrEqualTo(classFieldType("innerOfLong")));
  }

  /**
   * A parameterized type is not a subtype of an unrelated member class that is parameterized only
   * through its enclosing type, and the check does not throw.
   */
  @Test
  public void subtypeOfNonGenericMemberTest() {
    ClassOrInterfaceType nonGenericSupertypeInner = classFieldType("nonGenericSupertypeInner");
    assertTrue(nonGenericSupertypeInner.isParameterized());
    assertFalse(fieldType("genericInnerOfInteger").isSubtypeOfOrEqualTo(nonGenericSupertypeInner));
    assertFalse(
        JDKTypes.LIST_TYPE
            .instantiate(JavaTypes.STRING_TYPE)
            .isSubtypeOfOrEqualTo(nonGenericSupertypeInner));
  }

  /**
   * The type of a field declared as {@code NonGenericOuter.Base<String>} equals the instantiation
   * of the generic class {@code NonGenericOuter.Base}.
   */
  @Test
  public void memberOfNonGenericClassForTypeTest() {
    GenericClassType genericBase =
        (GenericClassType) ClassOrInterfaceType.forClass(NonGenericOuter.Base.class);
    InstantiatedType instantiated = genericBase.instantiate(JavaTypes.STRING_TYPE);
    ClassOrInterfaceType baseOfString = classFieldType("baseOfString");
    assertEquals(instantiated, baseOfString);
    assertEquals(instantiated.hashCode(), baseOfString.hashCode());
  }

  /**
   * {@code Outer<String>.GenericInner<Integer>} does not instantiate {@code
   * Outer<X>.GenericInner<X>}, because the bindings of {@code X} conflict.
   */
  @Test
  public void conflictingInstantiatingSubstitutionTest() throws NoSuchMethodException {
    ClassOrInterfaceType sameVariable =
        memberParameterType("sameVariable", Outer.GenericInner.class);
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    assertNull(
        genericInnerType(JavaTypes.STRING_TYPE, integerType)
            .getInstantiatingSubstitution(sameVariable));
    assertNotNull(
        genericInnerType(integerType, integerType).getInstantiatingSubstitution(sameVariable));
  }

  /**
   * A generic member class whose owner type is in declaration form, as in {@code
   * Outer<T>.GenericInner<String>}, is a member class whose enclosing type is that owner type.
   */
  @Test
  public void genericMemberClassDeclarationFormOwnerTest() {
    ClassOrInterfaceType type =
        ClassOrInterfaceType.forType(Outer.StringInner.class.getGenericSuperclass());
    assertTrue(type.runtimeClassIs(Outer.GenericInner.class));
    assertTrue(type.isMemberClass());
    assertTrue(type.enclosingType.runtimeClassIs(Outer.class));
    assertTrue(type.enclosingType.isGenericForm());
  }

  /**
   * The supertypes of a generic member class of a parameterized type, as in {@code
   * Outer<String>.GenericListOfT<Integer>}, are instantiated by the type arguments of both the
   * member class and its enclosing type.
   */
  @Test
  public void genericMemberClassSupertypesTest() throws NoSuchMethodException {
    // This gives the cached generic class an enclosing type, so its type parameters include those
    // of the enclosing class.
    ClassOrInterfaceType.forClass(Outer.GenericListOfT.class);
    ClassOrInterfaceType type =
        ClassOrInterfaceType.forType(
            GenericMemberHolder.class.getDeclaredMethod("m", Outer.GenericListOfT.class)
                .getGenericParameterTypes()[0]);
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    InstantiatedType supplierOfInteger =
        GenericClassType.forClass(Supplier.class).instantiate(integerType);
    assertEquals(
        GenericClassType.forClass(ArrayList.class).instantiate(JavaTypes.STRING_TYPE),
        type.getSuperclass());
    assertEquals(Collections.singletonList(supplierOfInteger), type.getInterfaces());
  }

  /** Tests that a member class has a wildcard if its enclosing type does. */
  @Test
  public void memberOfWildcardTypeHasWildcardTest() throws NoSuchMethodException {
    ClassOrInterfaceType wildcardOuter = wildcardHolderReturnType("wildcardOuter");
    ClassOrInterfaceType stringOuter = wildcardHolderReturnType("stringOuter");
    assertTrue(wildcardOuter.hasWildcard());
    assertFalse(stringOuter.hasWildcard());

    // Type.forType does not record the type arguments of an enclosing type, so set them directly.

    // Outer<?>.Inner and Outer<String>.Inner
    NonParameterizedType wildcardInner = new NonParameterizedType(Outer.Inner.class);
    wildcardInner.enclosingType = wildcardOuter;
    assertTrue(wildcardInner.hasWildcard());
    NonParameterizedType stringInner = new NonParameterizedType(Outer.Inner.class);
    stringInner.enclosingType = stringOuter;
    assertFalse(stringInner.hasWildcard());

    // Outer<?>.GenericInner<String> and Outer<String>.GenericInner<String>
    InstantiatedType wildcardGenericInner =
        (InstantiatedType) wildcardHolderReturnType("genericInner");
    wildcardGenericInner.enclosingType = wildcardOuter;
    assertTrue(wildcardGenericInner.hasWildcard());
    InstantiatedType stringGenericInner =
        (InstantiatedType) wildcardHolderReturnType("genericInner");
    stringGenericInner.enclosingType = stringOuter;
    assertFalse(stringGenericInner.hasWildcard());

    // A type that contains Outer<?>.Inner:  List<Outer<?>.Inner> and Outer<?>.Inner[]
    GenericClassType listType = (GenericClassType) ClassOrInterfaceType.forClass(List.class);
    assertTrue(listType.instantiate(wildcardInner).hasWildcard());
    assertFalse(listType.instantiate(stringInner).hasWildcard());
    assertTrue(ArrayType.ofComponentType(wildcardInner).hasWildcard());
    assertFalse(ArrayType.ofComponentType(stringInner).hasWildcard());
  }
}
