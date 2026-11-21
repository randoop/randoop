package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/** Tests {@link ParameterBound}. */
public class ParameterBoundTest {

  /** The bound whose type is the null type. */
  private static final ParameterBound nullBound = new EagerReferenceBound(JavaTypes.NULL_TYPE);

  /** The bound whose type is {@code Integer}. */
  private static final ParameterBound integerBound =
      ParameterBound.forType(ReferenceType.forClass(Integer.class));

  /** The bound whose type is {@code Number}. */
  private static final ParameterBound numberBound =
      ParameterBound.forType(ReferenceType.forClass(Number.class));

  /** The bound whose type is {@code String}. */
  private static final ParameterBound stringBound = ParameterBound.forType(JavaTypes.STRING_TYPE);

  /** The bound of the type parameter of {@link SelfComparable}, which is lazy. */
  private static final ParameterBound selfComparableBound =
      GenericClassType.forClass(SelfComparable.class)
          .getTypeParameters()
          .get(0)
          .getUpperTypeBound();

  /** The type parameter {@code T} of {@link ArrayHolder}. */
  private static final TypeVariable arrayHolderVariable =
      TypeVariable.forType(ArrayHolder.class.getTypeParameters()[0]);

  /** A field whose type is a generic array type that contains no type variable. */
  @SuppressWarnings("unused")
  private static List<String>[] concreteGenericArray;

  /**
   * Returns the generic type of the given field of the given class.
   *
   * @param c a class
   * @param fieldName the name of a field declared in {@code c}
   * @return the generic type of the field
   */
  private static java.lang.reflect.Type fieldType(Class<?> c, String fieldName) {
    try {
      return c.getDeclaredField(fieldName).getGenericType();
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
  }

  @Test
  public void testConcreteGenericArrayBoundWithEmptySubstitution() {
    java.lang.reflect.Type arrayType = fieldType(ParameterBoundTest.class, "concreteGenericArray");
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), arrayType);
    Type argType = Type.forType(arrayType);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(argType, empty));
    assertTrue(bound.isLowerBound(argType, empty));
  }

  @Test
  public void testLowerBoundOfOtherBounds() {
    ParameterBound intersection =
        new IntersectionTypeBound(
            Arrays.asList(
                numberBound, ParameterBound.forType(ReferenceType.forClass(Serializable.class))));

    assertTrue(selfComparableBound instanceof LazyParameterBound);
    assertTrue(nullBound.isLowerBound(intersection, new Substitution()));
    assertTrue(nullBound.isLowerBound(selfComparableBound, new Substitution()));
    assertTrue(integerBound.isLowerBound(intersection, new Substitution()));
    assertFalse(stringBound.isLowerBound(intersection, new Substitution()));
    assertFalse(integerBound.isLowerBound(selfComparableBound, new Substitution()));
    // An intersection is a lower bound of a type if any of its members is.
    assertTrue(intersection.isLowerBound(numberBound, new Substitution()));
    assertFalse(intersection.isLowerBound(integerBound, new Substitution()));
    assertFalse(intersection.isLowerBound(stringBound, new Substitution()));
  }

  /** A lazy bound that the substitution does not instantiate is not a lower bound of anything. */
  @Test
  public void testUninstantiatedLazyParameterBoundAsLowerBound() {
    Substitution unrelated = new Substitution(arrayHolderVariable, JavaTypes.STRING_TYPE);
    for (Substitution substitution : Arrays.asList(new Substitution(), unrelated)) {
      assertFalse(selfComparableBound.isLowerBound(nullBound, substitution));
      assertFalse(selfComparableBound.isLowerBound(integerBound, substitution));
      assertFalse(selfComparableBound.isLowerBound(selfComparableBound, substitution));
    }
  }

  /** A {@link LazyReferenceBound} is a type with free type variables, not one instantiation. */
  @Test
  public void testLazyReferenceBoundHasOnlyNullLowerBound() {
    GenericClassType listType = GenericClassType.forClass(List.class);
    ParameterBound lazyListBound =
        new LazyReferenceBound(listType.instantiate(arrayHolderVariable));
    ParameterBound stringListBound =
        ParameterBound.forType(listType.instantiate(JavaTypes.STRING_TYPE));

    assertFalse(stringListBound.isLowerBound(lazyListBound, new Substitution()));
    assertTrue(nullBound.isLowerBound(lazyListBound, new Substitution()));
  }

  /** The substitution is applied to the lower bound when the other bound is lazy. */
  @Test
  public void testSubstitutionAppliedToLowerBoundOfLazyBound() {
    ParameterBound variableBound = new EagerReferenceBound(arrayHolderVariable);
    assertTrue(
        variableBound.isLowerBound(
            selfComparableBound, new Substitution(arrayHolderVariable, JavaTypes.NULL_TYPE)));
    assertFalse(
        variableBound.isLowerBound(
            selfComparableBound, new Substitution(arrayHolderVariable, JavaTypes.STRING_TYPE)));
  }

  /**
   * A class whose type parameter has a self-referential bound.
   *
   * @param <T> the type parameter
   */
  static class SelfComparable<T extends Comparable<T>> {}

  /**
   * A class with a method whose return type is a generic array.
   *
   * @param <T> the type parameter
   */
  abstract static class ArrayHolder<T> {
    /**
     * Returns an array.
     *
     * @return an array
     */
    abstract T[] array();
  }

  /**
   * A class whose fields have types that contain an array of its type parameter.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("unused")
  private static class ArraysOfVariable<T> {
    /** A field whose type is an array of the type parameter. */
    T[] array;

    /** A field whose type has an array of the type parameter as a type argument. */
    List<T[]> listOfArrays;
  }

  /**
   * Returns true if the bound for the given field's type, created with no type variables bound, is
   * an upper bound of {@code Object} after the type parameter of {@link ArraysOfVariable} is
   * replaced by the null type.
   *
   * @param fieldName a simple name, such as {@code array}, of a field declared in {@link
   *     ArraysOfVariable}
   * @return true if the substituted bound is an upper bound of {@code Object}
   */
  private static boolean isUpperBoundWithNullComponent(String fieldName) {
    java.lang.reflect.Type type = fieldType(ArraysOfVariable.class, fieldName);
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), type);
    TypeVariable t = GenericClassType.forClass(ArraysOfVariable.class).getTypeParameters().get(0);
    return bound.isUpperBound(JavaTypes.OBJECT_TYPE, new Substitution(t, JavaTypes.NULL_TYPE));
  }

  @Test
  public void testArrayOfUnboundVariableWithNullComponent() {
    // Object is not a subtype of the array type NullType[].
    assertFalse(isUpperBoundWithNullComponent("array"));
  }

  @Test
  public void testNestedArrayOfUnboundVariableWithNullComponent() {
    // Object is not a subtype of List<NullType[]>.
    assertFalse(isUpperBoundWithNullComponent("listOfArrays"));
  }

  @Test
  public void testArrayOfUnboundVariableWithNullComponentAsLowerBound() {
    java.lang.reflect.Type type = fieldType(ArraysOfVariable.class, "array");
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), type);
    TypeVariable t = GenericClassType.forClass(ArraysOfVariable.class).getTypeParameters().get(0);
    Substitution nullSubstitution = new Substitution(t, JavaTypes.NULL_TYPE);
    ArrayType stringArray = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);
    // NullType[] is a subtype of String[], and String[] is not a subtype of NullType[].
    assertTrue(bound.isLowerBound(stringArray, nullSubstitution));
    assertFalse(bound.isUpperBound(stringArray, nullSubstitution));
  }

  /**
   * A generic class with an inner class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings({"unused", "ClassCanBeStatic"})
  private static class Outer<T> {
    /**
     * An inner class, whose owner type has a type variable.
     *
     * @param <U> the type parameter
     */
    class Inner<U> {}

    /**
     * A field whose type is a generic array type in which a type variable occurs only in an owner
     * type.
     */
    Inner<String>[] innerArray;
  }

  @Test
  public void testGenericArrayWithVariableInOwnerType() {
    java.lang.reflect.Type arrayType = fieldType(Outer.class, "innerArray");
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), arrayType);
    Type argType = Type.forType(arrayType);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(argType, empty));
    assertTrue(bound.isLowerBound(argType, empty));
  }

  /**
   * A generic class with a non-generic inner class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings({"unused", "ClassCanBeStatic"})
  private static class GenericOuter<T> {
    /** An inner class that has no type parameter of its own. */
    class NonGenericInner {}
  }

  /**
   * A class whose type parameter is bounded by a non-generic member class of a generic class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("unused")
  private static class NonGenericMemberClassBound<T extends GenericOuter<String>.NonGenericInner> {}

  @Test
  public void testNonGenericMemberClassBound() {
    TypeVariable t =
        GenericClassType.forClass(NonGenericMemberClassBound.class).getTypeParameters().get(0);
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  /**
   * A field whose type is a generic array type of a non-generic member class of a generic class.
   */
  @SuppressWarnings("unused")
  private static GenericOuter<String>.NonGenericInner[] nonGenericInnerArray;

  @Test
  public void testArrayOfNonGenericMemberClassBound() {
    java.lang.reflect.Type arrayType = fieldType(ParameterBoundTest.class, "nonGenericInnerArray");
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), arrayType);
    Type argType = Type.forType(arrayType);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(argType, empty));
    assertTrue(bound.isLowerBound(argType, empty));
    ArrayType stringArray = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);
    assertFalse(bound.isUpperBound(stringArray, empty));
    assertFalse(bound.isLowerBound(stringArray, empty));
  }

  /**
   * A class whose type parameter has a self-referential bound that contains, as a type argument, a
   * non-generic member class of a generic class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("unused")
  private static class MemberClassArgument<
      T extends Map<T, GenericOuter<String>.NonGenericInner>> {}

  /**
   * A class whose type parameter has a self-referential bound that contains, as a type argument, an
   * array of a non-generic member class of a generic class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("unused")
  private static class MemberClassArrayArgument<
      T extends Map<T, GenericOuter<String>.NonGenericInner[]>> {}

  /** A lazy bound converts a non-generic member class that occurs in a type argument. */
  @Test
  public void testNonGenericMemberClassInTypeArgument() {
    for (Class<?> c : Arrays.asList(MemberClassArgument.class, MemberClassArrayArgument.class)) {
      TypeVariable t = GenericClassType.forClass(c).getTypeParameters().get(0);
      ParameterBound bound = t.getUpperTypeBound();
      assertTrue(bound instanceof LazyParameterBound);
      ParameterBound substituted = bound.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
      assertTrue(substituted instanceof ReferenceBound);
      assertNotNull(((LazyParameterBound) bound).toReferenceBound());
    }
  }

  /**
   * A class in which a wildcard lower bound in the bound of {@code V} is a type variable whose
   * bound contains another type variable.
   *
   * @param <K> the type parameter that occurs in the bound of {@code U}
   * @param <U> the type parameter that is the wildcard lower bound
   * @param <V> the type parameter whose bound contains the wildcard
   */
  @SuppressWarnings("unused")
  private static class WildcardLower<K, U extends Map<K, List<U>>, V extends List<? super U>> {}

  /** A substitution is applied to the bounds of an unmapped wildcard lower bound. */
  @Test
  public void testSubstitutionAppliedToWildcardLowerBoundVariable() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(WildcardLower.class).getTypeParameters();
    TypeVariable k = parameters.get(0);
    ParameterBound bound = parameters.get(2).getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    ParameterBound substituted = bound.substitute(new Substitution(k, JavaTypes.STRING_TYPE));
    assertTrue(substituted instanceof ReferenceBound);
    InstantiatedType listType = (InstantiatedType) ((ReferenceBound) substituted).getBoundType();
    WildcardType wildcard =
        ((WildcardArgument) listType.getTypeArguments().get(0)).getWildcardType();
    TypeVariable lowerBoundVariable =
        (TypeVariable) ((ReferenceBound) wildcard.getTypeBound()).getBoundType();
    ParameterBound uBound = lowerBoundVariable.getUpperTypeBound();
    assertTrue(uBound.toString(), uBound.toString().contains("java.lang.String"));
  }

  /**
   * A non-generic member class of a generic class, as a lazy bound, has the null type as a lower
   * bound.
   */
  @Test
  public void testNullLowerBoundOfNonGenericMemberClassBound() {
    TypeVariable t =
        GenericClassType.forClass(NonGenericMemberClassBound.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    assertTrue(nullBound.isLowerBound(bound, new Substitution()));
    assertFalse(stringBound.isLowerBound(bound, new Substitution()));
  }

  /** An array of the null type is a lower bound of an array of a type variable. */
  @Test
  public void testNullArrayLowerBoundOfLazyArrayBound() {
    ParameterBound lazyArrayBound =
        new LazyReferenceBound(ArrayType.ofComponentType(arrayHolderVariable));
    ParameterBound nullArrayBound =
        new EagerReferenceBound(ArrayType.ofComponentType(JavaTypes.NULL_TYPE));
    ParameterBound stringArrayBound =
        new EagerReferenceBound(ArrayType.ofComponentType(JavaTypes.STRING_TYPE));
    assertTrue(nullArrayBound.isLowerBound(lazyArrayBound, new Substitution()));
    assertFalse(stringArrayBound.isLowerBound(lazyArrayBound, new Substitution()));
    // NullType[][] is not a subtype of T[] for every T, because NullType[] is not a subtype of T
    // if T is String.
    ParameterBound nullArray2Bound =
        new EagerReferenceBound(
            ArrayType.ofComponentType(ArrayType.ofComponentType(JavaTypes.NULL_TYPE)));
    assertFalse(nullArray2Bound.isLowerBound(lazyArrayBound, new Substitution()));
  }

  /**
   * A class whose second type parameter has a bound that contains an array of the first.
   *
   * @param <U> the type parameter whose array occurs in the bound of {@code T}
   * @param <T> the type parameter whose bound contains an array of {@code U}
   */
  @SuppressWarnings("unused")
  private static class ArrayOfOtherVariableBound<U, T extends List<U[]>> {}

  @Test
  public void testBoundWithArrayOfOtherVariable() {
    TypeVariable t =
        GenericClassType.forClass(ArrayOfOtherVariableBound.class).getTypeParameters().get(1);
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  /**
   * A class whose third type parameter has a bound that mentions the second, whose bound contains
   * an array of the first.
   *
   * @param <V> the type parameter whose array occurs in the bound of {@code U}
   * @param <U> the type parameter whose bound contains an array of {@code V}
   * @param <T> the type parameter whose bound mentions {@code U}
   */
  @SuppressWarnings("unused")
  private static class BoundMentioningArrayBound<V, U extends List<V[]>, T extends Collection<U>> {}

  @Test
  public void testBoundMentioningVariableWithArrayBound() {
    TypeVariable t =
        GenericClassType.forClass(BoundMentioningArrayBound.class).getTypeParameters().get(2);
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  /**
   * A class whose fields have the same type: a list of a wildcard bounded by a parameterized type
   * that contains an array of the type parameter.
   *
   * @param <U> the type parameter
   */
  @SuppressWarnings("unused")
  private static class WildcardWithArrayOfVariable<U> {
    /** A field whose type is a list of a wildcard bounded by a list of arrays of {@code U}. */
    List<? extends List<U[]>> first;

    /** A field whose type is the same as that of {@link #first}. */
    List<? extends List<U[]>> second;
  }

  @Test
  public void testWildcardBoundWithArrayOfVariable() {
    Type firstType = Type.forType(fieldType(WildcardWithArrayOfVariable.class, "first"));
    Type secondType = Type.forType(fieldType(WildcardWithArrayOfVariable.class, "second"));
    assertTrue(firstType.isSubtypeOfOrEqualTo(secondType));
  }

  /**
   * A type parameter whose bound has a wildcard bounded by an array of a parameterized type in
   * which no type variable occurs.
   *
   * @param <T> the type parameter
   */
  static class WildcardConcreteGenericArrayBound<T extends List<? extends List<String>[]>> {}

  /** A class that satisfies the bound of {@link WildcardConcreteGenericArrayBound}. */
  @SuppressWarnings("serial")
  static class ConcreteGenericArrayList extends ArrayList<List<String>[]> {}

  @Test
  public void testWildcardConcreteGenericArrayBound() {
    TypeVariable t =
        GenericClassType.forClass(WildcardConcreteGenericArrayBound.class)
            .getTypeParameters()
            .get(0);
    assertTrue(
        t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(ConcreteGenericArrayList.class)));
  }

  /**
   * A class whose type parameter has a bound with a wildcard bounded by an array of the class
   * itself.
   *
   * @param <T> the type parameter
   */
  static class SelfArrayBound<T extends List<? extends SelfArrayBound<?>[]>> {}

  // Converting the bound must not recurse infinitely by re-creating the class.
  @Test
  public void testSelfArrayBound() {
    assertEquals(1, GenericClassType.forClass(SelfArrayBound.class).getTypeParameters().size());
  }

  /**
   * A class whose type parameter has a bound with a wildcard bounded by the class itself.
   *
   * @param <T> the type parameter
   */
  static class SelfBound<T extends List<? extends SelfBound<?>>> {}

  // Converting the bound must not recurse infinitely by re-creating the class.
  @Test
  public void testSelfBound() {
    assertEquals(1, GenericClassType.forClass(SelfBound.class).getTypeParameters().size());
  }
}
