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
import java.util.function.BiConsumer;
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

  /**
   * A generic class with a generic member class whose superclass has another enclosing type.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("ClassCanBeStatic")
  static class OwnerChangingOuter<T> {
    /**
     * A generic member class.
     *
     * @param <U> the type parameter
     */
    class Base<U> {}

    /**
     * A generic member class whose superclass is a member of {@code OwnerChangingOuter<String>},
     * whatever {@code T} is.
     *
     * @param <V> the type parameter
     */
    class Derived<V> extends OwnerChangingOuter<String>.Base<V> {
      /**
       * Creates a Derived.
       *
       * @param outer the enclosing instance of the superclass
       */
      Derived(OwnerChangingOuter<String> outer) {
        outer.super();
      }
    }
  }

  @Test
  public void instantiatingSubstitutionUsesEnclosingTypeOfSupertype() {
    GenericClassType derivedType =
        (GenericClassType) ClassOrInterfaceType.forClass(OwnerChangingOuter.Derived.class);
    GenericClassType baseType =
        (GenericClassType) ClassOrInterfaceType.forClass(OwnerChangingOuter.Base.class);
    GenericClassType outerType =
        (GenericClassType) ClassOrInterfaceType.forClass(OwnerChangingOuter.class);
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    ReferenceType longType = JavaTypes.LONG_TYPE.toBoxedPrimitive();
    // OwnerChangingOuter<Integer>.Derived<Long>, whose superclass is
    // OwnerChangingOuter<String>.Base<Long>.
    InstantiatedType integerLong = derivedType.instantiate(integerType, longType);
    Substitution substitution = integerLong.getInstantiatingSubstitution(baseType);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(outerType.getTypeParameters().get(0)));
    assertEquals(longType, substitution.get(baseType.getDeclaredTypeParameters().get(0)));
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
    }
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

    /**
     * A generic inner class whose supertypes mention its type parameter.
     *
     * @param <U> the type parameter
     */
    abstract class GenericInner<U> extends Holder<U> implements Comparable<U> {}

    /**
     * A generic inner class whose type parameter has a bound.
     *
     * @param <U> the type parameter
     */
    class NumberInner<U extends Number> {}

    /** An inner class whose supertypes mention the type parameter of its enclosing class. */
    abstract class ListInner extends ArrayList<T> implements Comparable<T> {
      /** ID for serializing this class. */
      private static final long serialVersionUID = 20261117;
    }

    /**
     * A generic inner class whose supertypes mention the type parameters of both it and its
     * enclosing class.
     *
     * @param <U> the type parameter
     */
    abstract class HolderPairInner<U> extends Holder<T> implements BiConsumer<T, U> {}

    /**
     * A generic inner class whose supertypes mention the type parameters of both the enclosing
     * class and the inner class.
     *
     * @param <U> the type parameter
     */
    class PairInner<U> extends Pair<T, U> implements PairInterface<T, U> {}

    /** An inner class whose supertypes mention {@code T}. */
    @SuppressWarnings("serial")
    abstract class ListOfT extends ArrayList<T> implements Supplier<T> {}

    /**
     * A generic inner class whose supertypes mention {@code T} and its own type parameter.
     *
     * @param <U> the type parameter
     */
    abstract class GenericChild<U> extends Inner implements java.util.Map<T, U> {}

    /** An inner class whose supertypes mention an array of {@code T}. */
    @SuppressWarnings("serial")
    abstract class ListOfArrayOfT extends ArrayList<T[]> implements Supplier<T[]> {}

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
     * A generic inner class.
     *
     * @param <I> the type parameter
     */
    class GenericInner<I> {}
  }

  /** Declares methods whose parameter types are member classes of parameterized types. */
  static class OwnerHolder {
    /**
     * A method whose parameter types are member classes of parameterized types.
     *
     * @param leaf an {@code Outer<String>.GenericMiddle<Integer>.Leaf}
     * @param child an {@code Outer<String>.Child}
     * @param arrays an {@code Outer<? extends Number>.ListOfArrayOfT}
     * @param inner an {@code Outer<String>.Inner}
     * @param extendsInner a list whose elements are subtypes of {@code Outer<String>.Inner}
     */
    static void m(
        Outer<String>.GenericMiddle<Integer>.Leaf leaf,
        Outer<String>.Child child,
        Outer<? extends Number>.ListOfArrayOfT arrays,
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
            "m",
            Outer.GenericMiddle.Leaf.class,
            Outer.Child.class,
            Outer.ListOfArrayOfT.class,
            Outer.Inner.class,
            List.class)
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

  /**
   * The supertypes of a member class whose enclosing type has a wildcard argument are those of the
   * capture conversion of the enclosing type.
   */
  @Test
  public void memberOfWildcardTypeSupertypesTest() throws NoSuchMethodException {
    ClassOrInterfaceType arrays = ClassOrInterfaceType.forType(ownerHolderParameterTypes()[2]);
    assertEquals(ArrayList.class, arrays.getSuperclass().getRuntimeClass());
    assertEquals(1, arrays.getInterfaces().size());
  }

  /** A parameterized type is not a subtype of a member class of a parameterized type. */
  @Test
  public void parameterizedTypeNotSubtypeOfMemberClassTest() throws NoSuchMethodException {
    ClassOrInterfaceType inner = ClassOrInterfaceType.forType(ownerHolderParameterTypes()[3]);
    InstantiatedType listOfString = JDKTypes.LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    assertFalse(listOfString.isSubtypeOfOrEqualTo(inner));
  }

  /** A wildcard bounded by a member class of a parameterized type contains that member class. */
  @Test
  public void wildcardBoundedByMemberClassTest() throws NoSuchMethodException {
    java.lang.reflect.Type[] parameterTypes = ownerHolderParameterTypes();
    ClassOrInterfaceType inner = ClassOrInterfaceType.forType(parameterTypes[3]);
    InstantiatedType extendsInner =
        (InstantiatedType) ClassOrInterfaceType.forType(parameterTypes[4]);
    TypeArgument wildcard = extendsInner.getTypeArguments().get(0);
    assertTrue(wildcard.contains(TypeArgument.forType(inner)));
    assertFalse(wildcard.contains(TypeArgument.forType(JavaTypes.STRING_TYPE)));
    assertTrue(JDKTypes.LIST_TYPE.instantiate(inner).isSubtypeOfOrEqualTo(extendsInner));
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
  public void genericMemberClassSupertypesTest() throws NoSuchMethodException {
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

  /**
   * A generic class with two type parameters.
   *
   * @param <A> the first type parameter
   * @param <B> the second type parameter
   */
  static class Pair<A, B> {}

  /**
   * A generic interface with two type parameters.
   *
   * @param <A> the first type parameter
   * @param <B> the second type parameter
   */
  interface PairInterface<A, B> {}

  /** A field of type {@code Outer<String>.PairInner<Integer>}. */
  public Outer<String>.PairInner<Integer> pairInnerOfInteger;

  /** A field of type {@code Outer<String>.NumberInner<Integer>}. */
  public Outer<String>.NumberInner<Integer> stringNumberInnerOfInteger;

  /** A field of type {@code Outer<String>.NumberInner<? extends Number>}. */
  public Outer<String>.NumberInner<? extends Number> stringNumberInnerOfExtendsNumber;

  /** A field of type {@code Outer<Number>.NumberInner<? extends Number>}. */
  public Outer<Number>.NumberInner<? extends Number> numberNumberInnerOfExtendsNumber;

  /** A field of type {@code Outer<String>.GenericInner<Integer>}. */
  public Outer<String>.GenericInner<Integer> genericInnerOfInteger;

  /** A field of type {@code Outer<String>.NumberInner<?>}. */
  public Outer<String>.NumberInner<?> numberInnerOfWildcard;

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

  /**
   * The supertypes of a member class whose enclosing type is parameterized are instantiated by the
   * type arguments of the enclosing type.
   */
  @Test
  public void supertypesOfMemberClassUseEnclosingTypeArguments() {
    GenericClassType outerType = (GenericClassType) ClassOrInterfaceType.forClass(Outer.class);
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    ClassOrInterfaceType listInnerOfString =
        ClassOrInterfaceType.forClass(Outer.ListInner.class)
            .substitute(new Substitution(outerType.getTypeParameters(), JavaTypes.STRING_TYPE));
    assertEquals(
        GenericClassType.forClass(ArrayList.class).instantiate(JavaTypes.STRING_TYPE),
        listInnerOfString.getSuperclass());
    assertEquals(
        Collections.singletonList(
            GenericClassType.forClass(Comparable.class).instantiate(JavaTypes.STRING_TYPE)),
        listInnerOfString.getInterfaces());

    InstantiatedType pairInner =
        ((GenericClassType) ClassOrInterfaceType.forClass(Outer.HolderPairInner.class))
            .instantiate(JavaTypes.STRING_TYPE, integerType);
    assertEquals(
        GenericClassType.forClass(Holder.class).instantiate(JavaTypes.STRING_TYPE),
        pairInner.getSuperclass());
    assertEquals(
        Collections.singletonList(
            GenericClassType.forClass(BiConsumer.class)
                .instantiate(JavaTypes.STRING_TYPE, integerType)),
        pairInner.getInterfaces());
  }

  /**
   * A generic class whose member classes are used to test that the enclosing types of member
   * classes are compared.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("ClassCanBeStatic")
  static class OwnerChain<T> {
    /**
     * A generic member class.
     *
     * @param <U> the type parameter
     */
    class Inner<U> {}

    /** A member class that declares no type parameters, and has a generic member class. */
    class PlainMiddle {
      /**
       * A generic member class of a member class that declares no type parameters.
       *
       * @param <U> the type parameter
       */
      class Inner<U> {}
    }

    /**
     * A generic member class that has a generic member class.
     *
     * @param <M> the type parameter
     */
    class GenericMiddle<M> {
      /**
       * A generic member class of a generic member class.
       *
       * @param <U> the type parameter
       */
      class Inner<U> {}
    }
  }

  /**
   * A class whose superclass is a member class of a parameterized type.
   *
   * @param <X> the type parameter
   */
  static class InnerChild<X> extends OwnerChain<String>.Inner<X> {
    /**
     * Creates an InnerChild.
     *
     * @param outer the enclosing instance of the superclass
     */
    InnerChild(OwnerChain<String> outer) {
      outer.super();
    }
  }

  /** A field of type {@code OwnerChain<String>.Inner<Integer>}. */
  public OwnerChain<String>.Inner<Integer> stringInner;

  /** A field of type {@code OwnerChain<Number>.Inner<Integer>}. */
  public OwnerChain<Number>.Inner<Integer> numberInner;

  /** A field of type {@code InnerChild<Integer>}. */
  public InnerChild<Integer> innerChild;

  /** A field of type {@code OwnerChain<String>.PlainMiddle.Inner<Integer>}. */
  public OwnerChain<String>.PlainMiddle.Inner<Integer> stringPlainMiddleInner;

  /** Another field of type {@code OwnerChain<String>.PlainMiddle.Inner<Integer>}. */
  public OwnerChain<String>.PlainMiddle.Inner<Integer> otherStringPlainMiddleInner;

  /** A field of type {@code OwnerChain<Number>.PlainMiddle.Inner<Integer>}. */
  public OwnerChain<Number>.PlainMiddle.Inner<Integer> numberPlainMiddleInner;

  /** A field of type {@code OwnerChain<String>.GenericMiddle<Integer>.Inner<Long>}. */
  public OwnerChain<String>.GenericMiddle<Integer>.Inner<Long> stringGenericMiddleInner;

  /** A field of type {@code OwnerChain<Number>.GenericMiddle<Integer>.Inner<Long>}. */
  public OwnerChain<Number>.GenericMiddle<Integer>.Inner<Long> numberGenericMiddleInner;

  @Test
  public void subtypeComparesOwnerTypeBeyondNonGenericMiddle() {
    InstantiatedType stringType = fieldType("stringPlainMiddleInner");
    assertTrue(stringType.isSubtypeOfOrEqualTo(fieldType("otherStringPlainMiddleInner")));
    assertFalse(stringType.isSubtypeOfOrEqualTo(fieldType("numberPlainMiddleInner")));
  }

  @Test
  public void superclassComparesOwnerType() {
    InstantiatedType childType = fieldType("innerChild");
    assertTrue(childType.isSubtypeOfOrEqualTo(fieldType("stringInner")));
    assertFalse(childType.isSubtypeOfOrEqualTo(fieldType("numberInner")));
  }

  @Test
  public void instantiatingSubstitutionComparesOwnerOfFixedEnclosingType() {
    InstantiatedType stringLong = fieldType("stringGenericMiddleInner");
    InstantiatedType numberLong = fieldType("numberGenericMiddleInner");
    TypeVariable x = GenericClassType.forClass(Holder.class).getTypeParameters().get(0);
    List<TypeArgument> xArgument = Collections.singletonList(TypeArgument.forType(x));
    // OwnerChain<String>.GenericMiddle<Integer>.Inner<X>
    InstantiatedType stringX = new InstantiatedType(stringLong.getGenericClassType(), xArgument);
    stringX.enclosingType = stringLong.enclosingType;
    // OwnerChain<Number>.GenericMiddle<Integer>.Inner<X>
    InstantiatedType numberX = new InstantiatedType(numberLong.getGenericClassType(), xArgument);
    numberX.enclosingType = numberLong.enclosingType;

    Substitution substitution = stringLong.getInstantiatingSubstitution(stringX);
    assertNotNull(substitution);
    assertEquals(ReferenceType.forClass(Long.class), substitution.get(x));
    // No substitution for X unifies OwnerChain<String> with OwnerChain<Number>.
    assertNull(stringLong.getInstantiatingSubstitution(numberX));
  }

  @Test
  public void instantiatedBoundComparesOwnerType() {
    InstantiatedType stringType = fieldType("stringInner");
    InstantiatedType numberType = fieldType("numberInner");
    ParameterBound bound = new EagerReferenceBound(stringType);
    assertTrue(bound.isUpperBound(stringType, new Substitution()));
    assertFalse(bound.isUpperBound(numberType, new Substitution()));
    assertTrue(bound.isLowerBound(stringType, new Substitution()));
    assertFalse(bound.isLowerBound(numberType, new Substitution()));
  }

  @Test
  public void supertypeOfGenericMemberMentionsEnclosingParameterTest() {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    InstantiatedType pairInnerOfInteger = fieldType("pairInnerOfInteger");

    // Outer<String>.PairInner<Integer> extends Pair<String, Integer>.
    InstantiatedType pairOfStringInteger =
        ((GenericClassType) ClassOrInterfaceType.forClass(Pair.class))
            .instantiate(JavaTypes.STRING_TYPE, integerType);
    assertEquals(pairOfStringInteger, pairInnerOfInteger.getSuperclass());
    assertTrue(pairInnerOfInteger.isSubtypeOfOrEqualTo(pairOfStringInteger));

    // Outer<String>.PairInner<Integer> implements PairInterface<String, Integer>.
    InstantiatedType pairInterfaceOfStringInteger =
        ((GenericClassType) ClassOrInterfaceType.forClass(PairInterface.class))
            .instantiate(JavaTypes.STRING_TYPE, integerType);
    assertEquals(
        Collections.singletonList(pairInterfaceOfStringInteger),
        pairInnerOfInteger.getInterfaces());
    assertTrue(pairInnerOfInteger.isSubtypeOfOrEqualTo(pairInterfaceOfStringInteger));
  }

  @Test
  public void containmentRequiresSubtypeEnclosingTypeTest() {
    InstantiatedType stringNumberInnerOfInteger = fieldType("stringNumberInnerOfInteger");
    InstantiatedType stringNumberInnerOfExtendsNumber =
        fieldType("stringNumberInnerOfExtendsNumber");
    InstantiatedType numberNumberInnerOfExtendsNumber =
        fieldType("numberNumberInnerOfExtendsNumber");

    // Identical enclosing types:  containment of the type arguments suffices.
    assertTrue(stringNumberInnerOfInteger.isSubtypeOfOrEqualTo(stringNumberInnerOfExtendsNumber));

    // Different enclosing types:  Outer<String> is not a subtype of Outer<Number>.
    assertFalse(stringNumberInnerOfInteger.isSubtypeOfOrEqualTo(numberNumberInnerOfExtendsNumber));
    assertFalse(
        stringNumberInnerOfExtendsNumber.isSubtypeOfOrEqualTo(numberNumberInnerOfExtendsNumber));
    assertFalse(
        numberNumberInnerOfExtendsNumber.isSubtypeOfOrEqualTo(stringNumberInnerOfExtendsNumber));
  }
}
