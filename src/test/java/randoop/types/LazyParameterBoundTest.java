package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/** Tests {@link LazyParameterBound}. */
public class LazyParameterBoundTest {

  /**
   * A type parameter whose bound contains the type parameter and, nested in a type argument,
   * another type parameter.
   *
   * @param <U> the type parameter that occurs nested in the bound
   * @param <T> the type parameter with the bound
   */
  static class OtherNestedArgumentBound<U, T extends Map<List<U>, T>> {}

  @Test
  public void testSubstituteWithoutNestedVariable() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(OtherNestedArgumentBound.class).getTypeParameters();
    TypeVariable u = parameters.get(0);
    TypeVariable t = parameters.get(1);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    // The substitution does not map U, which occurs in the bound.  U remains a type variable in
    // the result, rather than becoming a null type argument.
    ParameterBound substituted = bound.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertTrue(
        "bound " + substituted + " should be lazy since it contains a type variable",
        substituted instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(
                GenericClassType.forClass(List.class).instantiate(u), JavaTypes.STRING_TYPE),
        ((ReferenceBound) substituted).getBoundType());
  }

  /**
   * A generic class with a generic member class, which has a member class.
   *
   * @param <A> the type parameter
   */
  static class MemberOuter<A> {
    /**
     * A generic member class.
     *
     * @param <B> the type parameter
     */
    class Middle<B> {
      /** A member class with no type parameters of its own. */
      class Deep {}
    }
  }

  /**
   * Type parameters whose recursive bounds differ only in the owner type of a generic member class.
   *
   * @param <U> a type parameter bounded by {@code List<MemberOuter<String>.Middle<U>.Deep>}
   * @param <V> a type parameter bounded by {@code List<MemberOuter<Long>.Middle<U>.Deep>}
   */
  static class OwnerOfGenericMemberBound<
      U extends List<MemberOuter<String>.Middle<U>.Deep>,
      V extends List<MemberOuter<Long>.Middle<U>.Deep>> {}

  /**
   * Returns the enclosing type of the enclosing type of the type argument of the given bound, whose
   * type is {@code List<MemberOuter<X>.Middle<U>.Deep>}.
   *
   * @param bound the bound
   * @return the type {@code MemberOuter<X>} in the bound
   */
  private static ClassOrInterfaceType getOuterOfDeep(ReferenceBound bound) {
    InstantiatedType list = (InstantiatedType) bound.getBoundType();
    ClassOrInterfaceType deep =
        (ClassOrInterfaceType)
            ((ReferenceArgument) list.getTypeArguments().get(0)).getReferenceType();
    ClassOrInterfaceType middle = deep.enclosingType;
    assertTrue(middle != null);
    return middle.enclosingType;
  }

  /** Converting a bound preserves the owner type of a generic member class. */
  @Test
  public void testOwnerOfGenericMemberClassPreserved() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(OwnerOfGenericMemberBound.class).getTypeParameters();
    ParameterBound uBound = parameters.get(0).getUpperTypeBound();
    ParameterBound vBound = parameters.get(1).getUpperTypeBound();
    assertTrue(uBound instanceof LazyParameterBound);
    assertTrue(vBound instanceof LazyParameterBound);
    ReferenceBound uConverted = ((LazyParameterBound) uBound).toReferenceBound();
    ReferenceBound vConverted = ((LazyParameterBound) vBound).toReferenceBound();
    GenericClassType outer = GenericClassType.forClass(MemberOuter.class);
    assertEquals(outer.instantiate(JavaTypes.STRING_TYPE), getOuterOfDeep(uConverted));
    assertEquals(outer.instantiate(ReferenceType.forClass(Long.class)), getOuterOfDeep(vConverted));
    assertFalse(Subtyping.isIdentical(uConverted.getBoundType(), vConverted.getBoundType()));
  }

  /**
   * A generic class with a member class, and a member class whose type parameter's bound contains
   * the first member class with a declaration-form owner.
   *
   * @param <A> the type parameter
   */
  static class DeclarationOwner<A> {
    /**
     * A generic member class.
     *
     * @param <B> the type parameter
     */
    class Inner<B> {}

    /**
     * A member class whose type parameter's bound is {@code List<DeclarationOwner<A>.Inner<T>>}.
     *
     * @param <T> the type parameter
     */
    class Holder<T extends List<Inner<T>>> {}
  }

  /**
   * Substituting a copy of a type parameter with other bounds into a declaration-form owner keeps
   * the copy, rather than restoring the declaration.
   */
  @Test
  public void testDeclarationFormOwnerKeepsSubstitutedCopy() {
    TypeVariable a = GenericClassType.forClass(DeclarationOwner.class).getTypeParameters().get(0);
    TypeVariable t =
        GenericClassType.forClass(DeclarationOwner.Holder.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    TypeVariable aCopy =
        a.createCopyWithBounds(
            a.getLowerTypeBound(), new EagerReferenceBound(ReferenceType.forClass(Number.class)));
    List<TypeVariable> variables = new ArrayList<>();
    variables.add(t);
    variables.add(a);
    List<ReferenceType> types = new ArrayList<>();
    types.add(JavaTypes.STRING_TYPE);
    types.add(aCopy);
    ParameterBound substituted = bound.substitute(new Substitution(variables, types));
    InstantiatedType list = (InstantiatedType) ((ReferenceBound) substituted).getBoundType();
    ClassOrInterfaceType inner =
        (ClassOrInterfaceType)
            ((ReferenceArgument) list.getTypeArguments().get(0)).getReferenceType();
    ClassOrInterfaceType owner = inner.enclosingType;
    assertTrue(owner instanceof InstantiatedType);
    ReferenceType ownerArgument =
        ((ReferenceArgument) owner.getTypeArguments().get(0)).getReferenceType();
    assertTrue(TypeVariable.areIdentical(aCopy, ownerArgument));
  }

  @Test
  public void testObjectIsUpperBoundOfLazyBound() {
    TypeVariable t =
        GenericClassType.forClass(OtherNestedArgumentBound.class).getTypeParameters().get(1);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    // Object is an upper bound of every reference type, including the type of a lazy bound.
    assertTrue(
        new EagerReferenceBound(JavaTypes.OBJECT_TYPE).isUpperBound(bound, new Substitution()));
    assertFalse(
        new EagerReferenceBound(JavaTypes.STRING_TYPE).isUpperBound(bound, new Substitution()));
  }

  /**
   * A type parameter whose self-referential bound contains an array of the type parameter.
   *
   * @param <T> the type parameter
   */
  static class ArrayArgumentBound<T extends List<T[]>> {}

  @Test
  public void testSubstituteNullTypeForArrayComponent() {
    TypeVariable t = GenericClassType.forClass(ArrayArgumentBound.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    ParameterBound substituted = bound.substitute(new Substitution(t, JavaTypes.NULL_TYPE));
    assertEquals(
        GenericClassType.forClass(List.class)
            .instantiate(ArrayType.ofComponentType(JavaTypes.NULL_TYPE)),
        ((ReferenceBound) substituted).getBoundType());
  }

  /** Fields whose types are used in tests. */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  static class FieldTypes {
    /** A wildcard whose upper bound is a generic array type that contains no type variable. */
    List<? extends List<String>[]> arrayOfListWildcard;
  }

  @Test
  public void testGenericArrayBoundWithoutTypeVariable() throws NoSuchFieldException {
    java.lang.reflect.ParameterizedType fieldType =
        (java.lang.reflect.ParameterizedType)
            FieldTypes.class.getDeclaredField("arrayOfListWildcard").getGenericType();
    java.lang.reflect.WildcardType wildcard =
        (java.lang.reflect.WildcardType) fieldType.getActualTypeArguments()[0];
    ParameterBound bound = ParameterBound.forTypes(new HashSet<>(0), wildcard.getUpperBounds());
    // The bound contains no type variable, so it is eager, and a substitution does not change it.
    assertTrue(bound instanceof EagerReferenceBound);
    ArrayType arrayOfList =
        ArrayType.ofComponentType(
            GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE));
    assertEquals(new EagerReferenceBound(arrayOfList), bound.substitute(new Substitution()));
    assertTrue(bound.isUpperBound(arrayOfList, new Substitution()));
    assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, new Substitution()));
  }

  /**
   * A generic class with a field whose type is a generic array type.
   *
   * @param <T> the component type of the array
   * @param <U> another type parameter
   */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  static class ArrayField<T, U> {
    /** A field whose type is {@code T[]}. */
    T[] array;
  }

  @Test
  public void testTypeComparisonAppliesSubstitutionOnce() throws NoSuchFieldException {
    List<TypeVariable> parameters = GenericClassType.forClass(ArrayField.class).getTypeParameters();
    TypeVariable t = parameters.get(0);
    TypeVariable u = parameters.get(1);
    LazyParameterBound bound =
        new LazyParameterBound(ArrayField.class.getDeclaredField("array").getGenericType());
    ArrayType stringArray = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);

    Substitution direct = new Substitution(t, JavaTypes.STRING_TYPE);
    assertTrue(bound.isUpperBound(stringArray, direct));
    assertTrue(bound.isLowerBound(stringArray, direct));

    // The substitution replaces T[] by U[], in which U stands for itself rather than for String.
    Substitution chained =
        new Substitution(t, u).extend(new Substitution(u, JavaTypes.STRING_TYPE));
    assertFalse(bound.isUpperBound(stringArray, chained));
    assertFalse(bound.isLowerBound(stringArray, chained));
  }

  @Test
  public void testNullTypeCannotInstantiateArrayComponentBound() {
    TypeVariable t = GenericClassType.forClass(ArrayArgumentBound.class).getTypeParameters().get(0);
    // The null type is not a subtype of the parameterized type List<NullType[]>, just as it is not
    // a subtype of List<NullType> for a bound such as T extends List<T>.
    assertFalse(t.canBeInstantiatedBy(JavaTypes.NULL_TYPE));
  }

  @Test
  public void testReferenceBoundWithNullTypeForArrayComponent() {
    TypeVariable t = GenericClassType.forClass(ArrayArgumentBound.class).getTypeParameters().get(0);
    ReferenceBound bound = ReferenceBound.forTypeLazyIfGeneric(ArrayType.ofComponentType(t));
    assertTrue(bound instanceof LazyReferenceBound);
    Substitution nullSubstitution = new Substitution(t, JavaTypes.NULL_TYPE);
    ArrayType stringArray = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);
    // The substituted bound is NullType[], which is a subtype of String[].  String[] is not a
    // subtype of NullType[].
    assertTrue(bound.isLowerBound(stringArray, nullSubstitution));
    assertFalse(bound.isUpperBound(stringArray, nullSubstitution));
    assertFalse(bound.isUpperBound(new EagerReferenceBound(stringArray), nullSubstitution));
  }

  /**
   * A type parameter whose bound contains, in a type argument, an array of the type parameter, and
   * another type parameter.
   *
   * @param <K> the other type parameter
   * @param <U> the type parameter with the bound
   */
  static class NestedArrayArgumentBound<K, U extends Map<K, U[]>> {}

  @Test
  public void testSubstituteNullTypeForNestedArrayComponent() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(NestedArrayArgumentBound.class).getTypeParameters();
    TypeVariable k = parameters.get(0);
    TypeVariable u = parameters.get(1);
    // Substituting for K leaves the bound lazy:  Map<String, U[]>.
    ParameterBound bound =
        u.getUpperTypeBound().substitute(new Substitution(k, JavaTypes.STRING_TYPE));
    assertTrue(bound instanceof LazyReferenceBound);
    // Substituting the null type for U resolves the bound to Map<String, NullType[]>.
    Substitution nullSubstitution = new Substitution(u, JavaTypes.NULL_TYPE);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(JavaTypes.STRING_TYPE, ArrayType.ofComponentType(JavaTypes.NULL_TYPE)),
        ((ReferenceBound) bound.substitute(nullSubstitution)).getBoundType());
    // As in testNullTypeCannotInstantiateArrayComponentBound, the null type is not within the
    // parameterized bound.  Neither comparison throws an exception.
    assertFalse(bound.isUpperBound(JavaTypes.NULL_TYPE, nullSubstitution));
    assertFalse(bound.isLowerBound(JavaTypes.NULL_TYPE, nullSubstitution));
  }

  /**
   * A type parameter whose bound has a wildcard bounded by an array of another type parameter.
   *
   * @param <T> the component type
   * @param <U> the type parameter with the bound
   */
  static class WildcardArrayOfSiblingBound<T, U extends List<? extends T[]>> {}

  /**
   * A type parameter, to substitute for another one.
   *
   * @param <V> the type parameter
   */
  static class OneParameter<V> {}

  @Test
  public void testSubstituteVariableThenNullTypeForArrayComponent() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(WildcardArrayOfSiblingBound.class).getTypeParameters();
    TypeVariable t = parameters.get(0);
    TypeVariable u = parameters.get(1);
    TypeVariable v = GenericClassType.forClass(OneParameter.class).getTypeParameters().get(0);
    ParameterBound bound = u.getUpperTypeBound().substitute(new Substitution(t, v));
    // Substituting the null type resolves the array bound, as substituting String does.
    ParameterBound nullBound = bound.substitute(new Substitution(v, JavaTypes.NULL_TYPE));
    assertEquals(
        u.getUpperTypeBound().substitute(new Substitution(t, JavaTypes.NULL_TYPE)), nullBound);
    ParameterBound stringBound = bound.substitute(new Substitution(v, JavaTypes.STRING_TYPE));
    assertEquals(
        u.getUpperTypeBound().substitute(new Substitution(t, JavaTypes.STRING_TYPE)), stringBound);
  }

  /**
   * A type parameter whose bound contains an array of a parameterized type with a concrete array
   * type argument.
   *
   * @param <T> the type parameter
   */
  static class ConcreteArrayArgumentBound<T extends List<Map<T, String[]>[]>> {}

  /** A class that satisfies the bound of {@link ConcreteArrayArgumentBound}. */
  @SuppressWarnings("serial")
  static class ConcreteArrayArgumentList
      extends ArrayList<Map<ConcreteArrayArgumentList, String[]>[]> {}

  @Test
  public void testConcreteArrayTypeArgument() {
    TypeVariable t =
        GenericClassType.forClass(ConcreteArrayArgumentBound.class).getTypeParameters().get(0);
    assertTrue(
        t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(ConcreteArrayArgumentList.class)));
  }

  /** A non-generic class with a generic inner class. */
  static class PlainOwner {
    /**
     * A generic inner class.
     *
     * @param <T> the type parameter
     */
    class Inner<T> {}
  }

  /**
   * A type parameter whose bound is a generic inner class of a non-generic class.
   *
   * @param <U> the type parameter
   */
  static class PlainOwnerInnerBound<U extends PlainOwner.Inner<U>> {}

  /** Fields whose types are used in {@link #testNonGenericOwnerOfGenericMemberBound}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  static class PlainOwnerFields {
    /** An instantiation of a generic inner class of a non-generic class. */
    PlainOwner.Inner<String> innerOfString;
  }

  /**
   * A generic class with an inner class that declares no type parameters.
   *
   * @param <A> a type parameter
   * @param <B> a type parameter
   */
  static class Box<A, B> {
    /** An inner class that declares no type parameters. */
    class Inner {}
  }

  /**
   * A generic class with a generic method whose type parameter has a bound that mentions a type
   * parameter of this class.
   *
   * @param <V> a type parameter
   * @param <S> a type parameter
   */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  abstract static class Host<V, S> {
    /** A type whose enclosing type has type arguments {@code V} and {@code Integer}. */
    Box<V, Integer>.Inner vInteger;

    /** A type whose enclosing type has type arguments {@code String} and {@code Integer}. */
    Box<String, Integer>.Inner stringInteger;

    /**
     * A generic method.
     *
     * @param <T> a type parameter
     * @param <U> a type parameter bounded by {@code Box<T, S>.Inner}
     */
    abstract <T, U extends Box<T, S>.Inner> void m();
  }

  /**
   * Applying a substitution to a bound once does not make the siblings of a type parameter that the
   * substitution instantiates stand for themselves.
   */
  @Test
  public void testSiblingStaysUnknownAfterReplacement() throws ReflectiveOperationException {
    List<TypeVariable> hostParameters = GenericClassType.forClass(Host.class).getTypeParameters();
    TypeVariable v = hostParameters.get(0);
    java.lang.reflect.TypeVariable<?>[] methodParameters =
        Host.class.getDeclaredMethod("m").getTypeParameters();
    TypeVariable t = TypeVariable.forType(methodParameters[0]);
    TypeVariable u = TypeVariable.forType(methodParameters[1]);
    ParameterBound bound = u.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    Substitution substitution =
        new Substitution(Arrays.asList(t, v), Arrays.asList(v, JavaTypes.STRING_TYPE));
    // The bound becomes Box<V, S>.Inner.  V stands for itself, but S, a sibling of V, which the
    // substitution instantiates, can be Integer.
    ReferenceType vInteger =
        ReferenceType.forType(Host.class.getDeclaredField("vInteger").getGenericType());
    assertTrue(bound.isUpperBound(vInteger, substitution));
    // V is not substituted a second time.
    ReferenceType stringInteger =
        ReferenceType.forType(Host.class.getDeclaredField("stringInteger").getGenericType());
    assertFalse(bound.isUpperBound(stringInteger, substitution));
  }

  /** Substituting into a bound keeps a non-generic owner of a generic member class. */
  @Test
  public void testNonGenericOwnerOfGenericMemberBound() throws NoSuchFieldException {
    TypeVariable u =
        GenericClassType.forClass(PlainOwnerInnerBound.class).getTypeParameters().get(0);
    ParameterBound bound = u.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    ReferenceType substituted =
        ((ReferenceBound) bound.substitute(new Substitution(u, JavaTypes.STRING_TYPE)))
            .getBoundType();
    ReferenceType reflected =
        ReferenceType.forType(
            PlainOwnerFields.class.getDeclaredField("innerOfString").getGenericType());
    assertTrue(((ClassOrInterfaceType) substituted).isMemberClass());
    assertEquals(reflected, substituted);
    assertTrue(Subtyping.isSubtypeOrEqual(reflected, substituted, new Substitution()));
    assertTrue(Subtyping.isSubtypeOrEqual(substituted, reflected, new Substitution()));
  }
}
