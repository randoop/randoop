package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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

    /** A wildcard whose upper bound is an array of a member class of a parameterized type. */
    List<? extends Outer<String>.Inner[]> arrayOfMemberWildcard;
  }

  /**
   * A generic class with an inner class.
   *
   * @param <T> the type parameter
   */
  static class Outer<T> {
    /** An inner class that declares no type parameters. */
    class Inner {}
  }

  @Test
  public void testMemberClassArrayBoundWithEmptySubstitution() throws NoSuchFieldException {
    java.lang.reflect.ParameterizedType fieldType =
        (java.lang.reflect.ParameterizedType)
            FieldTypes.class.getDeclaredField("arrayOfMemberWildcard").getGenericType();
    java.lang.reflect.WildcardType wildcard =
        (java.lang.reflect.WildcardType) fieldType.getActualTypeArguments()[0];
    ParameterBound bound = new LazyParameterBound(wildcard.getUpperBounds()[0]);
    // Converting Outer<String>.Inner does not create a GenericClassType for Inner, which declares
    // no type parameters and for which GenericClassType.forClass would throw.
    ParameterBound substituted = bound.substitute(new Substitution());
    assertTrue(substituted instanceof ReferenceBound);
    ReferenceType boundType = ((ReferenceBound) substituted).getBoundType();
    assertTrue(boundType.isArray());
    ClassOrInterfaceType elementType =
        (ClassOrInterfaceType) ((ArrayType) boundType).getComponentType();
    assertTrue(elementType.isParameterized());
    assertEquals(
        TypeArgument.forType(JavaTypes.STRING_TYPE),
        elementType.enclosingType.getTypeArguments().get(0));
    assertTrue(bound.isUpperBound(boundType, new Substitution()));
    assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, new Substitution()));
  }

  @Test
  public void testGenericArrayBoundWithoutTypeVariable() throws NoSuchFieldException {
    java.lang.reflect.ParameterizedType fieldType =
        (java.lang.reflect.ParameterizedType)
            FieldTypes.class.getDeclaredField("arrayOfListWildcard").getGenericType();
    java.lang.reflect.WildcardType wildcard =
        (java.lang.reflect.WildcardType) fieldType.getActualTypeArguments()[0];
    // The bound contains no type variable, so ParameterBound.forTypes creates an eager bound.
    // Construct the lazy bound directly to test LazyParameterBound.substitute.
    assertTrue(
        ParameterBound.forTypes(new HashSet<>(0), wildcard.getUpperBounds())
            instanceof EagerReferenceBound);
    ParameterBound bound = new LazyParameterBound(wildcard.getUpperBounds()[0]);
    ArrayType arrayOfList =
        ArrayType.ofComponentType(
            GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE));
    assertEquals(new EagerReferenceBound(arrayOfList), bound.substitute(new Substitution()));
    assertTrue(bound.isUpperBound(arrayOfList, new Substitution()));
    assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, new Substitution()));
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
   * A type parameter whose bound has a wildcard whose lower bound is a type parameter whose bound
   * mentions another type parameter.
   *
   * @param <X> the type parameter that occurs in the bound of {@code Y}
   * @param <Y> the type parameter that is the lower bound of the wildcard
   * @param <Z> the type parameter with the bound
   */
  static class WildcardLowerBoundVariableBound<
      X, Y extends List<X>, Z extends Map<Z, Comparable<? super Y>>> {}

  @Test
  public void testSubstituteIntoBoundOfWildcardLowerBound() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(WildcardLowerBoundVariableBound.class).getTypeParameters();
    TypeVariable x = parameters.get(0);
    TypeVariable z = parameters.get(2);
    assertTrue(z.getUpperTypeBound() instanceof LazyParameterBound);
    ParameterBound substituted =
        z.getUpperTypeBound().substitute(new Substitution(x, JavaTypes.STRING_TYPE));
    // The bound is now Map<Z, Comparable<? super Y>>, where Y extends List<String>.
    InstantiatedType mapType = (InstantiatedType) ((ReferenceBound) substituted).getBoundType();
    InstantiatedType comparableType =
        (InstantiatedType)
            ((ReferenceArgument) mapType.getTypeArguments().get(1)).getReferenceType();
    ParameterBound wildcardBound =
        ((WildcardArgument) comparableType.getTypeArguments().get(0)).getTypeBound();
    assertTrue(wildcardBound instanceof ReferenceBound);
    TypeVariable y = (TypeVariable) ((ReferenceBound) wildcardBound).getBoundType();
    assertEquals(
        GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE),
        ((ReferenceBound) y.getUpperTypeBound()).getBoundType());
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
}
