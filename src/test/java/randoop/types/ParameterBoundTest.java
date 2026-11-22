package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
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

  /** The bound {@code T[]}, where {@code T} is the type parameter of {@link ArrayHolder}. */
  private static final ParameterBound arrayBound;

  static {
    try {
      arrayBound =
          new LazyParameterBound(
              ArrayHolder.class.getDeclaredMethod("array").getGenericReturnType());
    } catch (NoSuchMethodException e) {
      throw new Error(e);
    }
  }

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

  /**
   * A lazy bound that the substitution does not instantiate is not a lower bound of anything except
   * itself.
   */
  @Test
  public void testUninstantiatedLazyParameterBoundAsLowerBound() {
    Substitution unrelated = new Substitution(arrayHolderVariable, JavaTypes.STRING_TYPE);
    for (Substitution substitution : Arrays.asList(new Substitution(), unrelated)) {
      assertFalse(selfComparableBound.isLowerBound(nullBound, substitution));
      assertFalse(selfComparableBound.isLowerBound(integerBound, substitution));
      // The substitution leaves T unchanged on both sides, so each side is Comparable<T> for the
      // same T, and a type is a lower bound of itself.
      assertTrue(selfComparableBound.isLowerBound(selfComparableBound, substitution));
    }
    Substitution noArrayVariable =
        new Substitution(
            GenericClassType.forClass(SelfComparable.class).getTypeParameters().get(0),
            JavaTypes.STRING_TYPE);
    assertFalse(arrayBound.isLowerBound(nullBound, new Substitution()));
    assertFalse(arrayBound.isLowerBound(nullBound, noArrayVariable));
  }

  /** A lazy generic array bound can be instantiated by a substitution. */
  @Test
  public void testInstantiatedLazyArrayBoundAsLowerBound() {
    Substitution substitution =
        new Substitution(arrayHolderVariable, ReferenceType.forClass(Integer.class));
    ParameterBound numberArrayBound =
        ParameterBound.forType(ArrayType.ofComponentType(ReferenceType.forClass(Number.class)));
    ParameterBound stringArrayBound =
        ParameterBound.forType(ArrayType.ofComponentType(JavaTypes.STRING_TYPE));
    assertTrue(arrayBound.isLowerBound(numberArrayBound, substitution));
    assertFalse(arrayBound.isLowerBound(stringArrayBound, substitution));
  }

  /**
   * The substitution is applied to the possible lower bound, but not to the bound that it is
   * compared with, even if that bound is an eager bound that contains a type variable.
   */
  @Test
  public void testLowerBoundDoesNotSubstituteTarget() {
    TypeVariable e = GenericClassType.forClass(List.class).getTypeParameters().get(0);
    ParameterBound eArrayBound = new EagerReferenceBound(ArrayType.ofComponentType(e));
    ParameterBound stringArrayBound =
        new EagerReferenceBound(ArrayType.ofComponentType(JavaTypes.STRING_TYPE));
    Substitution substitution = new Substitution(e, JavaTypes.STRING_TYPE);
    // E[] is not substituted when it is the bound compared with.
    assertFalse(stringArrayBound.isLowerBound(eArrayBound, substitution));
    // E[] becomes String[] when it is the possible lower bound.
    assertTrue(eArrayBound.isLowerBound(stringArrayBound, substitution));
    assertTrue(stringArrayBound.isLowerBound(stringArrayBound, substitution));
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

  /**
   * A subtype of the type of a {@link LazyReferenceBound}, with the same type variables, is a lower
   * bound of it.
   */
  @Test
  public void testSubtypeWithSameVariableIsLowerBoundOfLazyReferenceBound() {
    ParameterBound lazyListBound =
        new LazyReferenceBound(
            GenericClassType.forClass(List.class).instantiate(arrayHolderVariable));
    ParameterBound arrayListBound =
        new EagerReferenceBound(
            GenericClassType.forClass(ArrayList.class).instantiate(arrayHolderVariable));
    ParameterBound stringArrayListBound =
        new EagerReferenceBound(
            GenericClassType.forClass(ArrayList.class).instantiate(JavaTypes.STRING_TYPE));
    assertTrue(arrayListBound.isLowerBound(lazyListBound, new Substitution()));
    assertFalse(stringArrayListBound.isLowerBound(lazyListBound, new Substitution()));
    assertFalse(lazyListBound.isLowerBound(arrayListBound, new Substitution()));
  }

  /** A {@link LazyReferenceBound} is a lower bound of itself, but not of another type variable. */
  @Test
  public void testLazyReferenceBoundIsLowerBoundOfItself() {
    GenericClassType listType = GenericClassType.forClass(List.class);
    TypeVariable selfComparableVariable =
        GenericClassType.forClass(SelfComparable.class).getTypeParameters().get(0);
    ParameterBound lazyListBound =
        new LazyReferenceBound(listType.instantiate(arrayHolderVariable));
    ParameterBound otherLazyListBound =
        new LazyReferenceBound(listType.instantiate(selfComparableVariable));
    assertTrue(lazyListBound.isLowerBound(lazyListBound, new Substitution()));
    assertFalse(otherLazyListBound.isLowerBound(lazyListBound, new Substitution()));
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

  /** The substitution is not applied to a lazy bound whose lower bound is being checked. */
  @Test
  public void testSubstitutionNotAppliedToLazyTarget() {
    ParameterBound stringArrayBound =
        new EagerReferenceBound(ArrayType.ofComponentType(JavaTypes.STRING_TYPE));
    ParameterBound variableArrayBound =
        new EagerReferenceBound(ArrayType.ofComponentType(arrayHolderVariable));
    ParameterBound nullArrayBound =
        new EagerReferenceBound(ArrayType.ofComponentType(JavaTypes.NULL_TYPE));
    // In the target T[], T stands for itself, even though the substitution maps it to String.
    assertFalse(
        stringArrayBound.isLowerBound(
            arrayBound, new Substitution(arrayHolderVariable, JavaTypes.STRING_TYPE)));
    assertTrue(variableArrayBound.isLowerBound(arrayBound, new Substitution()));
    assertTrue(nullArrayBound.isLowerBound(arrayBound, new Substitution()));
  }

  /** An intersection is a lower bound of a lazy bound if any of its members is. */
  @Test
  public void testIntersectionAsLowerBoundOfLazyBound() {
    InstantiatedType listOfT =
        GenericClassType.forClass(List.class).instantiate(arrayHolderVariable);
    ParameterBound listAndSerializable =
        new IntersectionTypeBound(
            Arrays.<ParameterBound>asList(
                new EagerReferenceBound(listOfT),
                new EagerReferenceBound(JavaTypes.SERIALIZABLE_TYPE)));
    assertTrue(
        listAndSerializable.isLowerBound(new LazyReferenceBound(listOfT), new Substitution()));
    // List<String> is not a subtype of every instantiation of List<T>.
    ParameterBound listOfStringAndSerializable =
        new IntersectionTypeBound(
            Arrays.<ParameterBound>asList(
                new EagerReferenceBound(
                    GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE)),
                new EagerReferenceBound(JavaTypes.SERIALIZABLE_TYPE)));
    assertFalse(
        listOfStringAndSerializable.isLowerBound(
            new LazyReferenceBound(listOfT), new Substitution()));
  }

  /** An intersection is a subtype of a type if any of its members is. */
  @Test
  public void testIntersectionAsLowerBound() {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    ParameterBound integerAndComparable =
        new IntersectionTypeBound(
            Arrays.asList(
                integerBound,
                ParameterBound.forType(
                    GenericClassType.forClass(Comparable.class).instantiate(integerType))));

    assertTrue(integerAndComparable.isLowerBound(numberBound, new Substitution()));
    assertTrue(
        integerAndComparable.isLowerBound(
            ReferenceType.forClass(Number.class), new Substitution()));
    assertFalse(integerAndComparable.isLowerBound(stringBound, new Substitution()));
    assertFalse(integerAndComparable.isLowerBound(selfComparableBound, new Substitution()));
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
    // The conversion retains the owner type, Outer<T>, so T is a type parameter of the result.
    TypeVariable t = TypeVariable.forType(Outer.class.getTypeParameters()[0]);
    assertTrue(((ReferenceType) argType).getTypeParameters().contains(t));
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

    /** A field whose type is an array of a non-generic member class of a generic class. */
    NonGenericInner[] innerArray;
  }

  /** A field whose type is a non-generic member class of a parameterized type. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"}) // the field is read reflectively
  private GenericOuter<String>.NonGenericInner stringInner;

  /** A field whose type is an array of a non-generic member class of a parameterized type. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"}) // the field is read reflectively
  private GenericOuter<String>.NonGenericInner[] stringInnerArray;

  @Test
  public void testNonGenericMemberClassBoundWithMatchingType() {
    java.lang.reflect.Type type = fieldType(ParameterBoundTest.class, "stringInner");
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), type);
    Type argType = Type.forType(type);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(argType, empty));
    assertTrue(bound.isLowerBound(argType, empty));
    assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, empty));
  }

  @Test
  public void testArrayOfNonGenericMemberClassBound() {
    java.lang.reflect.Type type = fieldType(ParameterBoundTest.class, "stringInnerArray");
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), type);
    Type argType = Type.forType(type);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(argType, empty));
    assertTrue(bound.isLowerBound(argType, empty));
    assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, empty));
    ArrayType stringArray = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);
    assertFalse(bound.isUpperBound(stringArray, empty));
    assertFalse(bound.isLowerBound(stringArray, empty));
  }

  @Test
  public void testArrayOfNonGenericMemberClassWithVariableInOwnerType() {
    // The type is GenericOuter<T>.NonGenericInner[], in which T occurs only in the owner type.
    // ParameterBound.forType makes an eager bound for it, so this test creates the lazy bound
    // directly.  A substitution that does not instantiate T leaves the bound lazy.
    java.lang.reflect.Type type = fieldType(GenericOuter.class, "innerArray");
    ParameterBound bound = new LazyParameterBound(type);
    assertSame(bound, bound.substitute(new Substitution()));
    TypeVariable t = GenericClassType.forClass(GenericOuter.class).getTypeParameters().get(0);
    assertEquals(Collections.singletonList(t), bound.getTypeParameters());
    // With T standing for itself, the bound is the generic form of its class.
    ParameterBound converted = ((LazyParameterBound) bound).toReferenceBound();
    assertNotNull(converted);
    assertTrue(converted.isUpperBound(Type.forType(type), new Substitution()));
    ParameterBound substituted = bound.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertTrue(substituted instanceof ReferenceBound);
    Type stringInnerArray = Type.forType(fieldType(ParameterBoundTest.class, "stringInnerArray"));
    assertEquals(stringInnerArray, ((ReferenceBound) substituted).getBoundType());
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
    java.lang.reflect.Type declaredBound =
        NonGenericMemberClassBound.class.getTypeParameters()[0].getBounds()[0];
    assertTrue(t.canBeInstantiatedBy(ReferenceType.forType(declaredBound)));
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
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
   * A class whose type parameter has a self-referential bound in which the type parameter occurs
   * only in the enclosing type of a non-generic member class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("unused")
  private static class OwnerOnlyRecursiveBound<T extends List<GenericOuter<T>.NonGenericInner>> {}

  /**
   * A type parameter that occurs only in the enclosing type of a member class in its own bound
   * makes the bound lazy, and a substitution reaches the enclosing type.
   */
  @Test
  public void testOwnerOnlyRecursiveBound() {
    TypeVariable t =
        GenericClassType.forClass(OwnerOnlyRecursiveBound.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    assertEquals(Collections.singletonList(t), bound.getTypeParameters());
    ParameterBound substituted = bound.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertTrue(substituted instanceof ReferenceBound);
    InstantiatedType listType = (InstantiatedType) ((ReferenceBound) substituted).getBoundType();
    ClassOrInterfaceType inner =
        (ClassOrInterfaceType)
            ((ReferenceArgument) listType.getTypeArguments().get(0)).getReferenceType();
    assertEquals(
        GenericClassType.forClass(GenericOuter.class).instantiate(JavaTypes.STRING_TYPE),
        inner.enclosingType);
    assertNotNull(((LazyParameterBound) bound).toReferenceBound());
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

  /**
   * A type parameter bounded by its declaring class applied to a wildcard (issue #1889).
   *
   * @param <T> the type parameter
   */
  interface WildcardSelfBound<T extends WildcardSelfBound<?>> {}

  /**
   * A type parameter whose bound refers to {@link MutualB}, which refers back to this class.
   *
   * @param <T> the type parameter
   */
  interface MutualA<T extends MutualB<?>> {}

  /**
   * A type parameter whose bound refers to {@link MutualA}, which refers back to this class.
   *
   * @param <U> the type parameter
   */
  interface MutualB<U extends MutualA<?>> {}

  /**
   * A type parameter whose bound is a class nested in this class.
   *
   * @param <T> the type parameter
   */
  static class EnclosingBound<T extends EnclosingBound.Inner> {
    /** A class nested in {@link EnclosingBound}. */
    static class Inner {}
  }

  @Test
  public void testWildcardSelfBound() {
    assertEquals(1, GenericClassType.forClass(WildcardSelfBound.class).getTypeParameters().size());
  }

  @Test
  public void testMutuallyRecursiveBounds() {
    assertEquals(1, GenericClassType.forClass(MutualA.class).getTypeParameters().size());
    assertEquals(1, GenericClassType.forClass(MutualB.class).getTypeParameters().size());
  }

  @Test
  public void testEnclosingBound() {
    assertEquals(1, GenericClassType.forClass(EnclosingBound.class).getTypeParameters().size());
  }
}
