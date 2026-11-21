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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
   * A generic class with two type parameters and a non-generic member class.
   *
   * @param <A> the first type parameter
   * @param <B> the second type parameter
   */
  @SuppressWarnings("ClassCanBeStatic")
  static class TwoParameterOuter<A, B> {
    /** A member class whose superclass mentions only the first type parameter. */
    @SuppressWarnings("serial")
    class ListInner extends ArrayList<A> {}
  }

  /** A field whose type is {@code TwoParameterOuter<String, ?>.ListInner}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  TwoParameterOuter<String, ?>.ListInner wildcardOwnerField;

  /**
   * A wildcard type argument of the enclosing type does not prevent the other type arguments from
   * being substituted into the superclass.
   */
  @Test
  public void superclassOfMemberClassWithWildcardInOwner() throws NoSuchFieldException {
    ClassOrInterfaceType type =
        ClassOrInterfaceType.forType(
            ClassOrInterfaceTypeTest.class
                .getDeclaredField("wildcardOwnerField")
                .getGenericType());
    assertEquals(
        GenericClassType.forClass(ArrayList.class).instantiate(JavaTypes.STRING_TYPE),
        type.getSuperclass());
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

  /** The enclosing types of member classes must match for instantiation and for subtyping. */
  @Test
  public void memberClassWithOtherEnclosingTypeIsNotInstantiationOrSubtype() {
    GenericClassType innerType =
        (GenericClassType) ClassOrInterfaceType.forClass(GenericOuter.GenericInner.class);
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    ReferenceType longType = JavaTypes.LONG_TYPE.toBoxedPrimitive();
    // GenericOuter<String>.GenericInner<Integer>
    InstantiatedType stringInteger = innerType.instantiate(JavaTypes.STRING_TYPE, integerType);
    InstantiatedType otherStringInteger = innerType.instantiate(JavaTypes.STRING_TYPE, integerType);
    // GenericOuter<Long>.GenericInner<Integer>
    InstantiatedType longInteger = innerType.instantiate(longType, integerType);
    assertTrue(stringInteger.isInstantiationOf(otherStringInteger));
    assertFalse(longInteger.isInstantiationOf(stringInteger));
    assertTrue(stringInteger.isSubtypeOfOrEqualTo(otherStringInteger));
    assertFalse(longInteger.isSubtypeOfOrEqualTo(stringInteger));
    assertFalse(stringInteger.isSubtypeOfOrEqualTo(longInteger));
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

      /**
       * Another generic member class of a generic member class.
       *
       * @param <V> the type parameter
       */
      class Leaf<V> {}
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

  /** A non-generic class with a generic member class. */
  @SuppressWarnings("ClassCanBeStatic")
  static class BaseOuter {
    /**
     * A generic member class.
     *
     * @param <T> the type parameter
     */
    class Base<T> {}
  }

  /** Another non-generic class, whose member class extends a member class of {@link BaseOuter}. */
  @SuppressWarnings("ClassCanBeStatic")
  static class DerivedOuter {
    /**
     * A member class that extends a member class of another class.
     *
     * @param <U> the type parameter
     */
    class Derived<U> extends BaseOuter.Base<U> {
      /**
       * Creates a Derived.
       *
       * @param outer the enclosing instance of the superclass
       */
      Derived(BaseOuter outer) {
        outer.super();
      }
    }
  }

  /**
   * {@code DerivedOuter.Derived<String>} instantiates {@code BaseOuter.Base<T>}, though their
   * enclosing classes differ.
   */
  @Test
  public void instantiatingSubstitutionForMemberClassInheritedFromOtherClass() {
    GenericClassType baseType =
        (GenericClassType) ClassOrInterfaceType.forClass(BaseOuter.Base.class);
    GenericClassType derivedType =
        (GenericClassType) ClassOrInterfaceType.forClass(DerivedOuter.Derived.class);
    InstantiatedType derivedString = derivedType.instantiate(JavaTypes.STRING_TYPE);
    assertTrue(derivedString.isMemberClass());
    Substitution substitution = derivedString.getInstantiatingSubstitution(baseType);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(baseType.getTypeParameters().get(0)));
  }

  /**
   * A generic class whose member classes extend a member class of another class, with a type
   * argument that is the type parameter of this class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("ClassCanBeStatic")
  static class EnclosingOuter<T> {
    /** A member class that declares no type parameters. */
    class Child extends BaseOuter.Base<T> {
      /**
       * Creates a Child.
       *
       * @param outer the enclosing instance of the superclass
       */
      Child(BaseOuter outer) {
        outer.super();
      }
    }

    /**
     * A generic member class.
     *
     * @param <U> the type parameter
     */
    class GenericChild<U> extends BaseOuter.Base<T> {
      /**
       * Creates a GenericChild.
       *
       * @param outer the enclosing instance of the superclass
       */
      GenericChild(BaseOuter outer) {
        outer.super();
      }
    }
  }

  /**
   * In {@code EnclosingOuter<String>.Child}, which extends {@code BaseOuter.Base<T>}, the type
   * argument of the enclosing type replaces {@code T} in the superclass.
   */
  @Test
  public void instantiatingSubstitutionForSuperclassMentioningEnclosingParameter() {
    GenericClassType baseType =
        (GenericClassType) ClassOrInterfaceType.forClass(BaseOuter.Base.class);
    TypeVariable baseParameter = baseType.getTypeParameters().get(0);
    TypeVariable outerParameter =
        GenericClassType.forClass(EnclosingOuter.class).getTypeParameters().get(0);
    Substitution toString = new Substitution(outerParameter, JavaTypes.STRING_TYPE);

    // EnclosingOuter<String>.Child
    ClassOrInterfaceType stringChild =
        ClassOrInterfaceType.forClass(EnclosingOuter.Child.class).substitute(toString);
    Substitution substitution = stringChild.getInstantiatingSubstitution(baseType);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(baseParameter));

    // EnclosingOuter<String>.GenericChild<Integer>
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    GenericClassType genericChildType =
        (GenericClassType) ClassOrInterfaceType.forClass(EnclosingOuter.GenericChild.class);
    InstantiatedType stringIntegerChild =
        genericChildType.instantiate(JavaTypes.STRING_TYPE, integerType);
    substitution = stringIntegerChild.getInstantiatingSubstitution(baseType);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(baseParameter));
  }

  /**
   * A generic class with member classes, one of which extends the other.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("ClassCanBeStatic")
  static class InheritingOuter<T> {
    /** A member class that declares no type parameters. */
    class Inner {}

    /** A member class that extends the member class of another instantiation of this class. */
    class StringChild extends InheritingOuter<String>.Inner {
      /**
       * Creates a StringChild.
       *
       * @param outer the enclosing instance of the superclass
       */
      StringChild(InheritingOuter<String> outer) {
        outer.super();
      }
    }

    /** A member class that extends the member class of the same instantiation of this class. */
    class SameChild extends Inner {}
  }

  /**
   * The enclosing type that instantiates the goal's enclosing type is that of the receiver's
   * supertype whose class is the goal's class, not that of the receiver.
   */
  @Test
  public void instantiatingSubstitutionForInheritedMemberClass() {
    TypeVariable outerParameter =
        GenericClassType.forClass(InheritingOuter.class).getTypeParameters().get(0);
    TypeVariable x = GenericClassType.forClass(List.class).getTypeParameters().get(0);
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    Substitution toInteger = new Substitution(outerParameter, integerType);
    // InheritingOuter<X>.Inner
    ClassOrInterfaceType xInner =
        ClassOrInterfaceType.forClass(InheritingOuter.Inner.class)
            .substitute(new Substitution(outerParameter, x));

    // InheritingOuter<Integer>.StringChild extends InheritingOuter<String>.Inner.
    ClassOrInterfaceType integerStringChild =
        ClassOrInterfaceType.forClass(InheritingOuter.StringChild.class).substitute(toInteger);
    Substitution substitution = integerStringChild.getInstantiatingSubstitution(xInner);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(x));

    // InheritingOuter<Integer>.SameChild extends InheritingOuter<Integer>.Inner.
    ClassOrInterfaceType integerSameChild =
        ClassOrInterfaceType.forClass(InheritingOuter.SameChild.class).substitute(toInteger);
    substitution = integerSameChild.getInstantiatingSubstitution(xInner);
    assertNotNull(substitution);
    assertEquals(integerType, substitution.get(x));
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

      /** An inner class of an inner class, whose superclass mentions {@code T}. */
      @SuppressWarnings("serial")
      class DeepList extends ArrayList<T> {}
    }

    /** An inner class whose supertypes mention {@code T}. */
    @SuppressWarnings("serial")
    abstract class ListOfT extends ArrayList<T> implements Supplier<T> {}
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

  /** Fields whose types are member classes of parameterized types with wildcard arguments. */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  static class WildcardOwnerFields {
    /** A member class of {@code Outer<? extends Integer>}. */
    Outer<? extends Integer>.Inner integerInner;

    /** A member class of {@code Outer<? extends Number>}. */
    Outer<? extends Number>.Inner numberInner;
  }

  @Test
  public void subtypeOfMemberOfWildcardOwner() throws NoSuchFieldException {
    ClassOrInterfaceType integerInner =
        ClassOrInterfaceType.forType(
            WildcardOwnerFields.class.getDeclaredField("integerInner").getGenericType());
    ClassOrInterfaceType numberInner =
        ClassOrInterfaceType.forType(
            WildcardOwnerFields.class.getDeclaredField("numberInner").getGenericType());
    assertTrue(Subtyping.isSubtypeOrEqual(integerInner, numberInner));
    assertFalse(Subtyping.isSubtypeOrEqual(numberInner, integerInner));
  }

  /**
   * A generic class with a generic member class whose supertypes mention the type parameters of
   * both classes.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("ClassCanBeStatic")
  static class SupertypeOuter<T> {
    /**
     * A generic member class whose supertypes mention {@code T}.
     *
     * @param <U> the type parameter
     */
    @SuppressWarnings("serial")
    class Middle<U> extends HashMap<T, U> implements Comparable<Map<U, T>> {
      @Override
      public int compareTo(Map<U, T> other) {
        return 0;
      }
    }
  }

  /** A field whose type is a generic member class of a parameterized type. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  SupertypeOuter<String>.Middle<Integer> supertypeMiddleField;

  /** The supertypes of a generic member class of a parameterized type use the owner's arguments. */
  @Test
  public void supertypesOfGenericMemberOfParameterizedType() throws NoSuchFieldException {
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    ClassOrInterfaceType middle =
        ClassOrInterfaceType.forType(
            ClassOrInterfaceTypeTest.class
                .getDeclaredField("supertypeMiddleField")
                .getGenericType());
    assertEquals(
        GenericClassType.forClass(HashMap.class).instantiate(JavaTypes.STRING_TYPE, integerType),
        middle.getSuperclass());
    InstantiatedType mapOfIntegerString =
        GenericClassType.forClass(Map.class).instantiate(integerType, JavaTypes.STRING_TYPE);
    assertEquals(
        Collections.singletonList(
            GenericClassType.forClass(Comparable.class).instantiate(mapOfIntegerString)),
        middle.getInterfaces());

    // The same type, built by substitution rather than by reflection.
    TypeVariable t = TypeVariable.forType(SupertypeOuter.class.getTypeParameters()[0]);
    TypeVariable u = TypeVariable.forType(SupertypeOuter.Middle.class.getTypeParameters()[0]);
    ClassOrInterfaceType substituted =
        ClassOrInterfaceType.forClass(SupertypeOuter.Middle.class)
            .substitute(
                new Substitution(
                    Arrays.asList(t, u),
                    Arrays.<ReferenceType>asList(JavaTypes.STRING_TYPE, integerType)));
    assertEquals(middle.getSuperclass(), substituted.getSuperclass());
    assertEquals(middle.getInterfaces(), substituted.getInterfaces());
  }

  @Test
  public void instantiatingSubstitutionComparesWholeEnclosingChain() {
    TypeVariable t = GenericClassType.forClass(ThreeLevelOuter.class).getTypeParameters().get(0);
    TypeVariable u =
        GenericClassType.forClass(ThreeLevelOuter.Middle.class).getTypeParameters().get(0);
    TypeVariable v =
        GenericClassType.forClass(ThreeLevelOuter.Middle.Leaf.class).getTypeParameters().get(0);
    ClassOrInterfaceType leaf = ClassOrInterfaceType.forClass(ThreeLevelOuter.Middle.Leaf.class);
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    ReferenceType longType = JavaTypes.LONG_TYPE.toBoxedPrimitive();
    ReferenceType numberType = ClassOrInterfaceType.forClass(Number.class);
    // ThreeLevelOuter<String>.Middle<Integer>.Leaf<Long>
    ClassOrInterfaceType stringLeaf =
        leaf.substitute(
            new Substitution(
                Arrays.asList(t, u, v),
                Arrays.<ReferenceType>asList(JavaTypes.STRING_TYPE, integerType, longType)));
    // ThreeLevelOuter<Number>.Middle<Integer>.Leaf<V>
    ClassOrInterfaceType numberGoal =
        leaf.substitute(
            new Substitution(
                Arrays.asList(t, u), Arrays.<ReferenceType>asList(numberType, integerType)));
    assertNull(stringLeaf.getInstantiatingSubstitution(numberGoal));
    // ThreeLevelOuter<String>.Middle<Integer>.Leaf<V>
    ClassOrInterfaceType stringGoal =
        leaf.substitute(
            new Substitution(
                Arrays.asList(t, u),
                Arrays.<ReferenceType>asList(JavaTypes.STRING_TYPE, integerType)));
    Substitution substitution = stringLeaf.getInstantiatingSubstitution(stringGoal);
    assertNotNull(substitution);
    assertEquals(longType, substitution.get(v));
  }
}
