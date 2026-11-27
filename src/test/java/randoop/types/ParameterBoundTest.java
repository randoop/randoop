package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/** Tests {@link ParameterBound} and the construction of type parameters and their bounds. */
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

  /**
   * A type parameter bounded by its declaring class applied to a wildcard (issue #1889).
   *
   * @param <T> the type parameter
   */
  interface WildcardSelfBound<T extends WildcardSelfBound<?>> {}

  /** An implementation of {@link WildcardSelfBound}. */
  static class ConcreteWildcardSelfBound implements WildcardSelfBound<ConcreteWildcardSelfBound> {}

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

  /** An implementation of {@link MutualA}, whose type argument implements {@link MutualB}. */
  static class ConcreteMutualA implements MutualA<ConcreteMutualB> {}

  /** An implementation of {@link MutualB}, whose type argument implements {@link MutualA}. */
  static class ConcreteMutualB implements MutualB<ConcreteMutualA> {}

  /**
   * A type parameter whose bound is a class nested in this class.
   *
   * @param <T> the type parameter
   */
  static class EnclosingBound<T extends EnclosingBound.Inner> {
    /** A class nested in {@link EnclosingBound}. */
    static class Inner {}
  }

  /**
   * A class whose type parameter has a bound with a wildcard bounded by the class itself.
   *
   * @param <T> the type parameter
   */
  static class SelfBound<T extends List<? extends SelfBound<?>>> {}

  /**
   * A class whose type parameter has a bound with a wildcard bounded by an array of the class
   * itself.
   *
   * @param <T> the type parameter
   */
  static class SelfArrayBound<T extends List<? extends SelfArrayBound<?>[]>> {}

  @Test
  public void testWildcardSelfBound() {
    assertEquals(1, GenericClassType.forClass(WildcardSelfBound.class).getTypeParameters().size());
  }

  @Test
  public void testWildcardSelfBoundInstantiateArray() {
    GenericClassType genericType = GenericClassType.forClass(WildcardSelfBound.class);
    ReferenceType concrete = ClassOrInterfaceType.forClass(ConcreteWildcardSelfBound.class);
    assertEquals(genericType, genericType.instantiate(concrete).getGenericClassType());
    assertThrows(
        IllegalArgumentException.class, () -> genericType.instantiate(JavaTypes.STRING_TYPE));
  }

  @Test
  public void testWildcardSelfBoundInstantiateList() {
    GenericClassType genericType = GenericClassType.forClass(WildcardSelfBound.class);
    ReferenceType concrete = ClassOrInterfaceType.forClass(ConcreteWildcardSelfBound.class);
    assertEquals(
        genericType,
        genericType.instantiate(Collections.singletonList(concrete)).getGenericClassType());
    assertThrows(
        IllegalArgumentException.class,
        () -> genericType.instantiate(Collections.singletonList(JavaTypes.STRING_TYPE)));
  }

  // Converting the bound must not recurse infinitely by re-creating the class.
  @Test
  public void testSelfArrayBound() {
    assertEquals(1, GenericClassType.forClass(SelfArrayBound.class).getTypeParameters().size());
  }

  // Converting the bound must not recurse infinitely by re-creating the class.
  @Test
  public void testSelfBound() {
    List<TypeVariable> parameters = GenericClassType.forClass(SelfBound.class).getTypeParameters();
    assertEquals(1, parameters.size());
    ReferenceType boundType =
        ((ReferenceBound) parameters.get(0).getUpperTypeBound()).getBoundType();
    assertEquals(
        "java.util.List<? extends randoop.types.ParameterBoundTest$SelfBound<?>>",
        boundType.toString());
  }

  @Test
  public void testMutuallyRecursiveBounds() {
    assertEquals(1, GenericClassType.forClass(MutualA.class).getTypeParameters().size());
    assertEquals(1, GenericClassType.forClass(MutualB.class).getTypeParameters().size());
  }

  @Test
  public void testMutuallyRecursiveBoundsInstantiate() {
    GenericClassType mutualA = GenericClassType.forClass(MutualA.class);
    GenericClassType mutualB = GenericClassType.forClass(MutualB.class);
    ReferenceType concreteA = ClassOrInterfaceType.forClass(ConcreteMutualA.class);
    ReferenceType concreteB = ClassOrInterfaceType.forClass(ConcreteMutualB.class);
    assertEquals(mutualA, mutualA.instantiate(concreteB).getGenericClassType());
    assertEquals(mutualB, mutualB.instantiate(concreteA).getGenericClassType());
    assertThrows(IllegalArgumentException.class, () -> mutualA.instantiate(JavaTypes.STRING_TYPE));
    assertThrows(IllegalArgumentException.class, () -> mutualA.instantiate(concreteA));
  }

  @Test
  public void testEnclosingBound() {
    assertEquals(1, GenericClassType.forClass(EnclosingBound.class).getTypeParameters().size());
  }

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

  /** A field whose type is a generic array type that contains no type variable. */
  @SuppressWarnings("unused")
  private static List<String>[] concreteGenericArray;

  @Test
  public void testConcreteGenericArrayBoundWithEmptySubstitution() {
    java.lang.reflect.Type arrayType = fieldType(ParameterBoundTest.class, "concreteGenericArray");
    Type argType = Type.forType(arrayType);
    Substitution empty = new Substitution();
    // ParameterBound.forType creates an eager bound, so also test a lazy bound directly:  an empty
    // substitution materializes it.
    for (ParameterBound bound :
        Arrays.asList(
            ParameterBound.forType(Collections.emptySet(), arrayType),
            new LazyParameterBound(arrayType))) {
      assertTrue(bound.isUpperBound(argType, empty));
      assertTrue(bound.isLowerBound(argType, empty));
    }
    assertTrue(new LazyParameterBound(arrayType).substitute(empty) instanceof ReferenceBound);
  }

  /**
   * A generic class whose supertype has a nested type argument that mentions the type parameter.
   *
   * @param <T> the type parameter
   */
  static class NestedList<T> extends ArrayList<List<T>> {
    /** ID for serializing this class. */
    private static final long serialVersionUID = 20261122;
  }

  /** A field whose type is {@code NestedList<?>}. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"}) // the field is read reflectively
  private NestedList<?> wildcardNestedList;

  /** A field whose type is {@code List<List<?>>}. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"}) // the field is read reflectively
  private List<List<?>> listOfWildcardLists;

  /**
   * An eager bound captures the type whose supertype it finds. The supertype of {@code
   * NestedList<?>} is {@code List<List<CAP>>}, not {@code List<List<?>>}.
   */
  @Test
  public void testEagerBoundCapturesBeforeSupertypeSearch() {
    ReferenceType wildcardNested =
        (ReferenceType) Type.forType(fieldType(ParameterBoundTest.class, "wildcardNestedList"));
    ReferenceType listOfLists =
        (ReferenceType) Type.forType(fieldType(ParameterBoundTest.class, "listOfWildcardLists"));
    Substitution empty = new Substitution();
    assertFalse(new EagerReferenceBound(listOfLists).isUpperBound(wildcardNested, empty));
    assertFalse(new EagerReferenceBound(wildcardNested).isLowerBound(listOfLists, empty));

    InstantiatedType stringNested =
        GenericClassType.forClass(NestedList.class).instantiate(JavaTypes.STRING_TYPE);
    InstantiatedType listOfStringLists =
        GenericClassType.forClass(List.class)
            .instantiate(GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE));
    assertTrue(new EagerReferenceBound(listOfStringLists).isUpperBound(stringNested, empty));
    assertTrue(new EagerReferenceBound(stringNested).isLowerBound(listOfStringLists, empty));
  }

  /**
   * The type variables of an eager bound stand for themselves when it is compared with a possible
   * lower bound, as for a {@link LazyReferenceBound}.
   */
  @Test
  public void testEagerGenericBoundHasFixedVariables() {
    GenericClassType listType = GenericClassType.forClass(List.class);
    ParameterBound eagerListBound =
        new EagerReferenceBound(listType.instantiate(arrayHolderVariable));
    ParameterBound stringListBound =
        new EagerReferenceBound(listType.instantiate(JavaTypes.STRING_TYPE));
    ParameterBound arrayListBound =
        new EagerReferenceBound(
            GenericClassType.forClass(ArrayList.class).instantiate(arrayHolderVariable));
    assertFalse(stringListBound.isLowerBound(eagerListBound, new Substitution()));
    assertTrue(eagerListBound.isLowerBound(eagerListBound, new Substitution()));
    assertTrue(arrayListBound.isLowerBound(eagerListBound, new Substitution()));
    assertTrue(nullBound.isLowerBound(eagerListBound, new Substitution()));
  }

  /** An intersection is a lower bound of a {@link LazyReferenceBound} if any of its members is. */
  @Test
  public void testIntersectionLowerBoundOfLazyReferenceBound() {
    InstantiatedType listOfT =
        GenericClassType.forClass(List.class).instantiate(arrayHolderVariable);
    ParameterBound lazyListBound = new LazyReferenceBound(listOfT);
    ParameterBound serializableBound =
        ParameterBound.forType(ReferenceType.forClass(Serializable.class));
    ParameterBound listAndSerializable =
        new IntersectionTypeBound(Arrays.asList(lazyListBound, serializableBound));
    ParameterBound integerAndSerializable =
        new IntersectionTypeBound(Arrays.asList(integerBound, serializableBound));
    assertTrue(listAndSerializable.isLowerBound(lazyListBound, new Substitution()));
    assertFalse(integerAndSerializable.isLowerBound(lazyListBound, new Substitution()));
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
   * The substitution is applied to the possible lower bound, but not to a lazy bound that it is
   * compared with.
   */
  @Test
  public void testLowerBoundDoesNotSubstituteLazyTarget() {
    Substitution substitution = new Substitution(arrayHolderVariable, JavaTypes.STRING_TYPE);
    ParameterBound stringArrayBound =
        new EagerReferenceBound(ArrayType.ofComponentType(JavaTypes.STRING_TYPE));
    // T[] is not substituted when it is the bound compared with.
    assertFalse(stringArrayBound.isLowerBound(arrayBound, substitution));
    // T[] becomes String[] when it is the possible lower bound.
    assertTrue(arrayBound.isLowerBound(stringArrayBound, substitution));
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

    /** A field whose type has an owner type in declaration form. */
    Inner<String> inner;

    /**
     * An inner class whose type parameter has a recursive bound that mentions {@link Inner}, whose
     * owner type is {@code Outer<T>}.
     *
     * @param <V> the type parameter
     */
    class SelfInnerBound<V extends List<Inner<V>>> {}

    /**
     * A class whose type parameter has a self-referential bound that contains a generic member
     * class whose owner type is in declaration form, {@code Outer<T>}.
     *
     * @param <V> the type parameter
     */
    class DeclarationOwnerBound<V extends Map<V, Inner<String>>> {}

    /**
     * A class whose type parameter has a self-referential bound that contains a generic member
     * class whose owner type is fixed, {@code Outer<String>}.
     *
     * @param <V> the type parameter
     */
    class FixedOwnerBound<V extends Map<V, Outer<String>.Inner<String>>> {}
  }

  /**
   * A lazy bound for a member class whose owner type is in declaration form keeps the owner type
   * when it is converted, so that a later substitution reaches the owner's type parameter.
   */
  @Test
  public void testConvertedBoundKeepsDeclarationFormOwner() {
    java.lang.reflect.Type innerType = fieldType(Outer.class, "inner");
    ReferenceBound converted = new LazyParameterBound(innerType).toReferenceBound();
    assertNotNull(converted);
    ClassOrInterfaceType convertedType = (ClassOrInterfaceType) converted.getBoundType();
    assertTrue(convertedType.isMemberClass());
    assertEquals(Type.forType(innerType), convertedType);
    GenericClassType outer = GenericClassType.forClass(Outer.class);
    TypeVariable t = outer.getTypeParameters().get(0);
    ClassOrInterfaceType substituted =
        convertedType.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertEquals(outer.instantiate(JavaTypes.STRING_TYPE), substituted.enclosingType);
  }

  /**
   * A generic class whose type parameter has a bound, with an inner class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings({"unused", "ClassCanBeStatic"})
  private static class BoundedOuter<T extends Number> {
    /**
     * An inner class, whose owner type has a type variable.
     *
     * @param <U> the type parameter
     */
    class Inner<U> {}

    /** A field whose type has an owner type in declaration form. */
    Inner<String> inner;
  }

  /**
   * A substitution that changes only the bound of the type parameter of a declaration-form owner
   * changes the owner.
   */
  @Test
  public void testSubstitutionChangingOwnerVariableBound() {
    TypeVariable t = GenericClassType.forClass(BoundedOuter.class).getTypeParameters().get(0);
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    TypeVariable integerBoundedT =
        t.createCopyWithBounds(t.getLowerTypeBound(), ParameterBound.forType(integerType));
    ParameterBound substituted =
        new LazyParameterBound(fieldType(BoundedOuter.class, "inner"))
            .substitute(new Substitution(t, integerBoundedT));
    assertTrue(substituted instanceof ReferenceBound);
    ClassOrInterfaceType innerType =
        (ClassOrInterfaceType) ((ReferenceBound) substituted).getBoundType();
    assertTrue(innerType.isMemberClass());
    InstantiatedType ownerType = (InstantiatedType) innerType.enclosingType;
    TypeVariable ownerArgument =
        (TypeVariable) ((ReferenceArgument) ownerType.getTypeArguments().get(0)).getReferenceType();
    assertEquals(integerType, ((ReferenceBound) ownerArgument.getUpperTypeBound()).getBoundType());
  }

  /** Converting a lazy bound keeps the declaration-form owner type of a member class in it. */
  @Test
  public void testLazyBoundKeepsDeclarationFormOwner() {
    TypeVariable v = TypeVariable.forType(Outer.SelfInnerBound.class.getTypeParameters()[0]);
    ParameterBound bound = v.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    ReferenceBound converted = ((LazyParameterBound) bound).toReferenceBound();
    assertNotNull(converted);
    InstantiatedType listType = (InstantiatedType) converted.getBoundType();
    ClassOrInterfaceType inner =
        (ClassOrInterfaceType)
            ((ReferenceArgument) listType.getTypeArguments().get(0)).getReferenceType();
    assertTrue(inner.isMemberClass());
    assertEquals(Outer.class, inner.enclosingType.getRuntimeClass());
    // The owner type is Outer<T>, not raw Outer.
    assertTrue(inner.enclosingType.isGenericForm());
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

    // The owner type, Outer<T>, is kept and substituted:  with T := String, the bound is
    // Outer<String>.Inner<String>[], which is independent of the field that the bound came from.
    Substitution tIsString = new Substitution(t, JavaTypes.STRING_TYPE);
    Type stringOwnerArray = Type.forType(fieldType(ParameterBoundTest.class, "stringOwnerArray"));
    Type integerOwnerArray = Type.forType(fieldType(ParameterBoundTest.class, "integerOwnerArray"));
    ReferenceType substituted = ((ReferenceBound) bound.substitute(tIsString)).getBoundType();
    assertEquals(stringOwnerArray, substituted);
    assertNotEquals(integerOwnerArray, substituted);
    assertTrue(bound.isUpperBound(stringOwnerArray, tIsString));
  }

  /**
   * A field of a generic array type whose element type has the owner type {@code Outer<String>}.
   */
  @SuppressWarnings("unused")
  private static Outer<String>.Inner<String>[] stringOwnerArray;

  /**
   * A field of a generic array type whose element type has the owner type {@code Outer<Integer>}.
   */
  @SuppressWarnings("unused")
  private static Outer<Integer>.Inner<String>[] integerOwnerArray;

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

  /**
   * A generic class with a generic member class, which has a non-generic member class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings({"unused", "ClassCanBeStatic"})
  private static class TwoLevelOuter<T> {
    /**
     * A generic member class.
     *
     * @param <U> the type parameter
     */
    class Middle<U> {
      /** A member class that has no type parameter of its own. */
      class Leaf {}
    }
  }

  /** A field whose type is {@code TwoLevelOuter<String>.Middle<Integer>}. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"}) // the field is read reflectively
  private TwoLevelOuter<String>.Middle<Integer> stringMiddle;

  /** A field whose type is {@code TwoLevelOuter<Long>.Middle<Integer>}. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"}) // the field is read reflectively
  private TwoLevelOuter<Long>.Middle<Integer> longMiddle;

  /** A field whose type is {@code TwoLevelOuter<String>.Middle<Integer>.Leaf}. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"}) // the field is read reflectively
  private TwoLevelOuter<String>.Middle<Integer>.Leaf stringLeaf;

  /** A field whose type is {@code TwoLevelOuter<Long>.Middle<Integer>.Leaf}. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"}) // the field is read reflectively
  private TwoLevelOuter<Long>.Middle<Integer>.Leaf longLeaf;

  @Test
  public void testMemberClassBoundComparesAllEnclosingTypes() {
    Substitution empty = new Substitution();
    for (String[] fieldNames :
        Arrays.asList(
            new String[] {"stringMiddle", "longMiddle"}, new String[] {"stringLeaf", "longLeaf"})) {
      ReferenceType stringType =
          (ReferenceType) Type.forType(fieldType(ParameterBoundTest.class, fieldNames[0]));
      ReferenceType longType =
          (ReferenceType) Type.forType(fieldType(ParameterBoundTest.class, fieldNames[1]));
      EagerReferenceBound bound = new EagerReferenceBound(stringType);
      assertTrue(bound.isUpperBound(stringType, empty));
      assertTrue(bound.isLowerBound(stringType, empty));
      // The types differ only in the type argument of the outermost enclosing type.
      assertFalse(bound.isUpperBound(longType, empty));
      assertFalse(bound.isLowerBound(longType, empty));
    }
  }

  @Test
  public void testArrayOfNonGenericMemberClassBound() {
    java.lang.reflect.Type type = fieldType(ParameterBoundTest.class, "stringInnerArray");
    Type argType = Type.forType(type);
    Substitution empty = new Substitution();
    ArrayType stringArray = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);
    // The same member class with another owner type is a different type.
    Type otherOwnerArray =
        Type.forType(fieldType(ParameterBoundTest.class, "integerOwnerNonGenericInnerArray"));
    // ParameterBound.forType creates an eager bound, so also test a lazy bound directly:  an empty
    // substitution materializes it.
    for (ParameterBound bound :
        Arrays.asList(
            ParameterBound.forType(Collections.emptySet(), type), new LazyParameterBound(type))) {
      assertTrue(bound.isUpperBound(argType, empty));
      assertTrue(bound.isLowerBound(argType, empty));
      assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, empty));
      assertFalse(bound.isUpperBound(stringArray, empty));
      assertFalse(bound.isLowerBound(stringArray, empty));
      assertFalse(bound.isUpperBound(otherOwnerArray, empty));
      assertFalse(bound.isLowerBound(otherOwnerArray, empty));
    }
    assertTrue(new LazyParameterBound(type).substitute(empty) instanceof ReferenceBound);
  }

  /**
   * A field of a generic array type of a member class whose owner type is {@code
   * GenericOuter<Integer>}.
   */
  @SuppressWarnings("unused")
  private static GenericOuter<Integer>.NonGenericInner[] integerOwnerNonGenericInnerArray;

  /**
   * A generic class with a generic member class that has a non-generic member class.
   *
   * @param <A> the type parameter
   */
  @SuppressWarnings({"unused", "ClassCanBeStatic"})
  private static class ThreeLevelPlain<A> {
    /**
     * A generic member class that has a non-generic member class.
     *
     * @param <B> the type parameter
     */
    class Middle<B> {
      /** A non-generic member class of a generic member class. */
      class Plain {}
    }
  }

  /** A field whose type is a non-generic member class with two parameterized enclosing types. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"}) // the field is read reflectively
  private ThreeLevelPlain<String>.Middle<Integer>.Plain stringPlain;

  /** A field like {@link #stringPlain}, but whose outermost type argument differs. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"}) // the field is read reflectively
  private ThreeLevelPlain<Number>.Middle<Integer>.Plain numberPlain;

  /** A bound that is a non-generic member class is compared with each of its enclosing types. */
  @Test
  public void testNonGenericMemberClassBoundComparesAllEnclosingTypes() {
    java.lang.reflect.Type stringType = fieldType(ParameterBoundTest.class, "stringPlain");
    Type numberType = Type.forType(fieldType(ParameterBoundTest.class, "numberPlain"));
    ParameterBound bound = new LazyParameterBound(stringType);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(Type.forType(stringType), empty));
    assertTrue(bound.isLowerBound(Type.forType(stringType), empty));
    assertFalse(bound.isUpperBound(numberType, empty));
    assertFalse(bound.isLowerBound(numberType, empty));
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

  /**
   * A class whose type parameter's bound contains a non-generic member class whose owner type
   * mentions the type parameter.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("unused")
  private static class OwnerVariableBound<T extends List<GenericOuter<T>.NonGenericInner>> {}

  /** A field whose type is a list of a non-generic member class of a parameterized type. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"}) // the field is read reflectively
  private List<GenericOuter<String>.NonGenericInner> stringInnerList;

  @Test
  public void testBoundWithVariableInOwnerTypeIsLazy() {
    // Converting the bound eagerly would convert T, whose bound is being converted, without end.
    TypeVariable t = GenericClassType.forClass(OwnerVariableBound.class).getTypeParameters().get(0);
    assertTrue(t.getUpperTypeBound() instanceof LazyParameterBound);
  }

  @Test
  public void testBoundWithConcreteOwnerTypeIsEager() {
    java.lang.reflect.Type type = fieldType(ParameterBoundTest.class, "stringInnerList");
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), type);
    assertTrue(bound instanceof EagerReferenceBound);
    assertTrue(bound.isUpperBound(Type.forType(type), new Substitution()));
  }

  /**
   * A class with a field whose type mentions the first of its type parameters.
   *
   * @param <T> a type parameter that occurs in the field type
   * @param <U> a type parameter that does not occur in the field type
   */
  @SuppressWarnings({"unused", "UnusedVariable"}) // the field is read reflectively
  private static class ListOfFirstVariable<T, U> {
    /** A field whose type mentions {@code T}. */
    List<T> list;
  }

  @Test
  public void testLazyLowerBoundAppliesSubstitutionOnce() {
    ParameterBound lazy = new LazyParameterBound(fieldType(ListOfFirstVariable.class, "list"));
    List<TypeVariable> parameters =
        GenericClassType.forClass(ListOfFirstVariable.class).getTypeParameters();
    TypeVariable u = parameters.get(1);
    // The substitution makes the bound List<U>.  Applying it again would make it List<String>.
    Substitution substitution = new Substitution(parameters, u, JavaTypes.STRING_TYPE);
    InstantiatedType listOfString =
        GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE);
    InstantiatedType listOfU = GenericClassType.forClass(List.class).instantiate(u);
    assertFalse(lazy.isLowerBound(new EagerReferenceBound(listOfString), substitution));
    assertTrue(lazy.isLowerBound(new EagerReferenceBound(listOfU), substitution));
  }

  /** A lazy bound is compared with a type after the substitution is applied to it only once. */
  @Test
  public void testLazyBoundComparedWithTypeAppliesSubstitutionOnce() {
    ParameterBound lazy = new LazyParameterBound(fieldType(ArraysOfVariable.class, "array"));
    TypeVariable t = GenericClassType.forClass(ArraysOfVariable.class).getTypeParameters().get(0);
    TypeVariable u =
        GenericClassType.forClass(ListOfFirstVariable.class).getTypeParameters().get(1);
    ArrayType stringArray = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);
    // The substitution makes the bound U[], in which U stands for itself.  Applying the
    // substitution again would make it String[].
    Substitution chained = new Substitution(Arrays.asList(t, u), u, JavaTypes.STRING_TYPE);
    assertFalse(lazy.isUpperBound(stringArray, chained));
    assertFalse(lazy.isLowerBound(stringArray, chained));
    Substitution direct = new Substitution(t, JavaTypes.STRING_TYPE);
    assertTrue(lazy.isUpperBound(stringArray, direct));
    assertTrue(lazy.isLowerBound(stringArray, direct));
  }

  /** The substitution is applied to a possible lower bound, but not to the other bound. */
  @Test
  public void testSubstitutionNotAppliedToReceiverOfHasLowerBound() {
    ParameterBound variableBound = new EagerReferenceBound(arrayHolderVariable);
    Substitution substitution = new Substitution(arrayHolderVariable, JavaTypes.STRING_TYPE);
    // T stands for itself in variableBound, so String is not a lower bound of it.
    assertFalse(stringBound.isLowerBound(variableBound, substitution));
    assertTrue(nullBound.isLowerBound(variableBound, substitution));
  }

  /**
   * A subtype of the type of a {@link LazyReferenceBound}, with the same type variables, is a lower
   * bound of it.
   */
  @Test
  public void testParameterizedSubtypeIsLowerBoundOfLazyReferenceBound() {
    InstantiatedType listOfT =
        GenericClassType.forClass(List.class).instantiate(arrayHolderVariable);
    InstantiatedType arrayListOfT =
        GenericClassType.forClass(ArrayList.class).instantiate(arrayHolderVariable);
    InstantiatedType arrayListOfString =
        GenericClassType.forClass(ArrayList.class).instantiate(JavaTypes.STRING_TYPE);
    ParameterBound lazyListBound = new LazyReferenceBound(listOfT);
    assertTrue(
        new LazyReferenceBound(arrayListOfT).isLowerBound(lazyListBound, new Substitution()));
    assertFalse(
        new EagerReferenceBound(arrayListOfString).isLowerBound(lazyListBound, new Substitution()));
  }

  /**
   * A {@link LazyParameterBound} is a lower bound of the {@link ReferenceBound} that it converts
   * to, though the substitution does not instantiate it.
   */
  @Test
  public void testLazyParameterBoundIsLowerBoundOfItsConversion() {
    LazyParameterBound lazy = new LazyParameterBound(fieldType(ListOfFirstVariable.class, "list"));
    ReferenceBound converted = lazy.toReferenceBound();
    assertNotNull(converted);
    assertTrue(converted.hasLowerBound(lazy, new Substitution()));
  }

  /**
   * A generic class with a generic member class that has a generic member class.
   *
   * @param <A> the type parameter
   */
  @SuppressWarnings({"unused", "ClassCanBeStatic"})
  private static class ThreeLevelOuter<A> {
    /**
     * A generic member class that has a generic member class.
     *
     * @param <B> the type parameter
     */
    class Middle<B> {
      /**
       * A generic member class of a generic member class.
       *
       * @param <U> the type parameter
       */
      class Inner<U> {}

      /** A field whose type has owner types in declaration form. */
      Inner<String> inner;
    }
  }

  /** Substituting only the outermost owner type argument keeps the substituted owner. */
  @Test
  public void testSubstitutionOfOutermostOwnerArgument() {
    ParameterBound bound = new LazyParameterBound(fieldType(ThreeLevelOuter.Middle.class, "inner"));
    GenericClassType outer = GenericClassType.forClass(ThreeLevelOuter.class);
    TypeVariable a = outer.getTypeParameters().get(0);
    ParameterBound substituted = bound.substitute(new Substitution(a, JavaTypes.STRING_TYPE));
    assertTrue(substituted instanceof ReferenceBound);
    // The result is ThreeLevelOuter<String>.Middle<B>.Inner<String>.
    ClassOrInterfaceType innerType =
        (ClassOrInterfaceType) ((ReferenceBound) substituted).getBoundType();
    assertTrue(innerType.isMemberClass());
    ClassOrInterfaceType middleType = innerType.enclosingType;
    assertTrue(middleType.isMemberClass());
    assertEquals(outer.instantiate(JavaTypes.STRING_TYPE), middleType.enclosingType);
  }

  @Test
  public void testNonGenericMemberClassBound() {
    TypeVariable t =
        GenericClassType.forClass(NonGenericMemberClassBound.class).getTypeParameters().get(0);
    java.lang.reflect.Type declaredBound =
        NonGenericMemberClassBound.class.getTypeParameters()[0].getBounds()[0];
    assertTrue(t.canBeInstantiatedBy(ReferenceType.forType(declaredBound)));
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  /** A field of type {@code GenericOuter<Integer>.NonGenericInner}. */
  @SuppressWarnings("unused")
  private static GenericOuter<Integer>.NonGenericInner integerOwnerInner;

  /**
   * A class whose type parameter has a recursive bound that contains a non-generic member class of
   * a generic class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("unused")
  private static class RecursiveMemberClassBound<
      T extends Map<T, GenericOuter<String>.NonGenericInner>> {}

  /** A map whose value type has the same owner type as the bound of RecursiveMemberClassBound. */
  @SuppressWarnings("serial")
  private static class SameOwnerMap
      extends HashMap<SameOwnerMap, GenericOuter<String>.NonGenericInner> {}

  /** A map whose value type has another owner type than the bound of RecursiveMemberClassBound. */
  @SuppressWarnings("serial")
  private static class OtherOwnerMap
      extends HashMap<OtherOwnerMap, GenericOuter<Integer>.NonGenericInner> {}

  /**
   * Tests that a non-generic member class of a generic class keeps the type arguments of its owner
   * type, so that {@code GenericOuter<String>.NonGenericInner} and {@code
   * GenericOuter<Integer>.NonGenericInner} are different types.
   */
  @Test
  public void testNonGenericMemberClassBoundWithOtherOwner() {
    ReferenceType integerOwner =
        ReferenceType.forType(fieldType(ParameterBoundTest.class, "integerOwnerInner"));
    TypeVariable t =
        GenericClassType.forClass(NonGenericMemberClassBound.class).getTypeParameters().get(0);
    assertFalse(t.canBeInstantiatedBy(integerOwner));

    TypeVariable recursive =
        GenericClassType.forClass(RecursiveMemberClassBound.class).getTypeParameters().get(0);
    assertTrue(recursive.canBeInstantiatedBy(ClassOrInterfaceType.forClass(SameOwnerMap.class)));
    assertFalse(recursive.canBeInstantiatedBy(ClassOrInterfaceType.forClass(OtherOwnerMap.class)));
  }

  /** A field of type {@code GenericOuter<String>.NonGenericInner}. */
  @SuppressWarnings("unused")
  private static GenericOuter<String>.NonGenericInner stringOwnerInner;

  /**
   * Tests that a substitution is applied to the owner type of a non-generic member class in a
   * bound, as in {@code GenericOuter<X>.NonGenericInner}.
   */
  @Test
  public void testNonGenericMemberClassBoundWithOwnerVariable() {
    java.lang.reflect.Type declaredBound =
        OwnerVariableMemberBound.class.getTypeParameters()[1].getBounds()[0];
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), declaredBound);
    TypeVariable x = TypeVariable.forType(OwnerVariableMemberBound.class.getTypeParameters()[0]);
    Substitution substitution = new Substitution(x, JavaTypes.STRING_TYPE);

    ReferenceType stringOwner =
        ReferenceType.forType(fieldType(ParameterBoundTest.class, "stringOwnerInner"));
    ReferenceType integerOwner =
        ReferenceType.forType(fieldType(ParameterBoundTest.class, "integerOwnerInner"));

    assertTrue(bound.isUpperBound(stringOwner, substitution));
    assertFalse(bound.isUpperBound(integerOwner, substitution));
    assertTrue(bound.isLowerBound(stringOwner, substitution));
    assertFalse(bound.isLowerBound(integerOwner, substitution));

    ParameterBound substituted = bound.substitute(substitution);
    assertTrue(substituted instanceof ReferenceBound);
    assertEquals(stringOwner, ((ReferenceBound) substituted).getBoundType());
  }

  /**
   * Tests that, when a substitution does not map the type variable in the owner type of a
   * non-generic member class in a bound, as in {@code GenericOuter<X>.NonGenericInner}, that type
   * variable stands for an unknown type. A substitution that maps it still determines the owner
   * type.
   */
  @Test
  public void testNonGenericMemberClassBoundWithUnmappedOwnerVariable() {
    java.lang.reflect.Type declaredBound =
        OwnerVariableMemberBound.class.getTypeParameters()[1].getBounds()[0];
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), declaredBound);
    TypeVariable x = TypeVariable.forType(OwnerVariableMemberBound.class.getTypeParameters()[0]);
    ReferenceType stringOwner =
        ReferenceType.forType(fieldType(ParameterBoundTest.class, "stringOwnerInner"));
    ReferenceType integerOwner =
        ReferenceType.forType(fieldType(ParameterBoundTest.class, "integerOwnerInner"));

    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(stringOwner, empty));
    assertTrue(bound.isUpperBound(integerOwner, empty));
    assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, empty));

    Substitution integerSubstitution =
        new Substitution(x, ClassOrInterfaceType.forClass(Integer.class));
    assertFalse(bound.isUpperBound(stringOwner, integerSubstitution));
    assertTrue(bound.isUpperBound(integerOwner, integerSubstitution));
  }

  /**
   * A class whose type parameter has a bound that refers to the type parameter only through the
   * owner type of a non-generic member class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("unused")
  private static class OwnerComparableRecursiveBound<
      T extends Comparable<GenericOuter<T>.NonGenericInner>> {}

  /**
   * Tests that a bound that refers to its type variable only through the owner type of a
   * non-generic member class is lazy, so that converting the type variable terminates.
   */
  @Test
  public void testBoundRecursiveThroughMemberClassOwner() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(OwnerComparableRecursiveBound.class).getTypeParameters();
    assertEquals(1, parameters.size());
    java.lang.reflect.TypeVariable<?> t =
        OwnerComparableRecursiveBound.class.getTypeParameters()[0];
    Set<java.lang.reflect.TypeVariable<?>> variableSet = new HashSet<>();
    variableSet.add(t);
    assertTrue(ParameterBound.forType(variableSet, t.getBounds()[0]) instanceof LazyParameterBound);

    // A bound whose owner type has no type variable is still eager.
    java.lang.reflect.Type concreteBound =
        NonGenericMemberClassBound.class.getTypeParameters()[0].getBounds()[0];
    java.lang.reflect.Type comparableOfConcrete =
        fieldType(ParameterBoundTest.class, "comparableOfStringOwnerInner");
    assertTrue(
        ParameterBound.forType(Collections.emptySet(), comparableOfConcrete)
            instanceof EagerReferenceBound);
    assertTrue(
        ParameterBound.forType(Collections.emptySet(), concreteBound)
            instanceof LazyParameterBound);
  }

  /** A field of type {@code Comparable<GenericOuter<String>.NonGenericInner>}. */
  @SuppressWarnings("unused")
  private static Comparable<GenericOuter<String>.NonGenericInner> comparableOfStringOwnerInner;

  /**
   * Tests that a reference bound is compared with a lazy bound for a non-generic member class of a
   * generic class, through the overloads of isLowerBound and isUpperBound that take a bound.
   */
  @Test
  public void testReferenceBoundComparedWithNonGenericMemberClassBound() {
    java.lang.reflect.Type innerType = fieldType(ParameterBoundTest.class, "stringOwnerInner");
    ParameterBound lazyBound = ParameterBound.forType(Collections.emptySet(), innerType);
    assertTrue(lazyBound instanceof LazyParameterBound);
    ReferenceBound sameBound = new EagerReferenceBound(ReferenceType.forType(innerType));
    Substitution empty = new Substitution();
    assertTrue(sameBound.isLowerBound(lazyBound, empty));
    assertTrue(sameBound.isUpperBound(lazyBound, empty));

    ReferenceBound otherOwnerBound =
        new EagerReferenceBound(
            ReferenceType.forType(fieldType(ParameterBoundTest.class, "integerOwnerInner")));
    assertFalse(otherOwnerBound.isLowerBound(lazyBound, empty));
    assertFalse(otherOwnerBound.isUpperBound(lazyBound, empty));
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
   * Returns the enclosing type of the second type argument of the upper bound of the type parameter
   * of {@code c}, after substituting {@code String} for that type parameter and {@code Integer} for
   * the type parameter of {@link Outer}.
   *
   * @param c a class declared in {@link Outer}, whose type parameter is bounded by a map whose
   *     second type argument is a member class
   * @return the enclosing type of the member class in the substituted bound
   */
  private static ClassOrInterfaceType substitutedOwner(Class<?> c) {
    TypeVariable v = TypeVariable.forType(c.getTypeParameters()[0]);
    TypeVariable t = TypeVariable.forType(Outer.class.getTypeParameters()[0]);
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    ParameterBound bound = v.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    Substitution substitution =
        new Substitution(v, JavaTypes.STRING_TYPE).extend(new Substitution(t, integerType));
    ParameterBound substituted = bound.substitute(substitution);
    assertTrue(substituted instanceof ReferenceBound);
    InstantiatedType mapType = (InstantiatedType) ((ReferenceBound) substituted).getBoundType();
    ClassOrInterfaceType inner =
        (ClassOrInterfaceType)
            ((ReferenceArgument) mapType.getTypeArguments().get(1)).getReferenceType();
    assertTrue(inner.runtimeClassIs(Outer.Inner.class));
    return inner.enclosingType;
  }

  /**
   * A substitution reaches an owner type in declaration form, {@code Outer<T>}, in a lazy bound,
   * and leaves a fixed owner type, {@code Outer<String>}, unchanged.
   */
  @Test
  public void testSubstituteOwnerTypeInLazyBound() {
    GenericClassType outer = GenericClassType.forClass(Outer.class);
    assertEquals(
        outer.instantiate(ReferenceType.forClass(Integer.class)),
        substitutedOwner(Outer.DeclarationOwnerBound.class));
    assertEquals(
        outer.instantiate(JavaTypes.STRING_TYPE), substitutedOwner(Outer.FixedOwnerBound.class));
  }

  /**
   * An unbounded type variable is not an instantiation of a type variable whose bound is a
   * non-generic member class of a parameterized type. Comparing their upper bounds does not throw
   * an exception.
   */
  @Test
  public void testUnboundedVariableIsNotInstantiationOfMemberClassBoundedVariable() {
    TypeVariable unbounded = TypeVariable.forType(ArraysOfVariable.class.getTypeParameters()[0]);
    TypeVariable memberBounded =
        TypeVariable.forType(NonGenericMemberClassBound.class.getTypeParameters()[0]);
    assertTrue(memberBounded.getUpperTypeBound() instanceof LazyParameterBound);
    assertFalse(unbounded.isInstantiationOf(memberBounded));
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

  /** An array of a subtype of a type with a type variable is a lower bound of an array of it. */
  @Test
  public void testSubtypeArrayLowerBoundOfLazyArrayBound() {
    GenericClassType listType = GenericClassType.forClass(List.class);
    ParameterBound listArrayBound =
        new LazyReferenceBound(
            ArrayType.ofComponentType(listType.instantiate(arrayHolderVariable)));
    ParameterBound arrayListArrayBound =
        new EagerReferenceBound(
            ArrayType.ofComponentType(
                GenericClassType.forClass(ArrayList.class).instantiate(arrayHolderVariable)));
    ParameterBound listOfStringArrayBound =
        new EagerReferenceBound(
            ArrayType.ofComponentType(listType.instantiate(JavaTypes.STRING_TYPE)));
    assertTrue(arrayListArrayBound.isLowerBound(listArrayBound, new Substitution()));
    // List<String>[] is not a subtype of List<T>[] for every T.
    assertFalse(listOfStringArrayBound.isLowerBound(listArrayBound, new Substitution()));
  }

  /**
   * A class whose type parameter occurs in its bound only in an owner type.
   *
   * @param <T> a type parameter bounded by {@code Outer<T>.Inner<String>}
   */
  @SuppressWarnings("unused")
  private static class OwnerRecursiveBound<T extends Outer<T>.Inner<String>> {}

  /**
   * A class whose second type parameter has a bound with a fixed owner type, and with a type
   * argument whose bound is recursive.
   *
   * @param <V> a recursively bounded type parameter
   * @param <T> a type parameter bounded by {@code Outer<String>.Inner<V>}
   */
  @SuppressWarnings("unused")
  private static class FixedOwnerBound<V extends Comparable<V>, T extends Outer<String>.Inner<V>> {}

  /**
   * A class whose second type parameter is bounded by a non-generic member class whose owner type
   * mentions the first.
   *
   * @param <X> a type parameter
   * @param <T> a type parameter bounded by {@code GenericOuter<X>.NonGenericInner}
   */
  @SuppressWarnings("unused")
  private static class OwnerVariableMemberBound<X, T extends GenericOuter<X>.NonGenericInner> {}

  /** A field whose type is a member class whose owner type is {@code Outer<String>}. */
  @SuppressWarnings("unused")
  private static Outer<String>.Inner<String> innerOfStringOuter;

  /** A field whose type is a member class whose owner type is {@code Outer<Integer>}. */
  @SuppressWarnings("unused")
  private static Outer<Integer>.Inner<String> innerOfIntegerOuter;

  /** A field whose type is a non-generic member class of {@code GenericOuter<String>}. */
  @SuppressWarnings("unused")
  private static GenericOuter<String>.NonGenericInner nonGenericInnerOfStringOuter;

  /** A field whose type is a list of a non-generic member class of a parameterized type. */
  @SuppressWarnings("unused")
  private static List<GenericOuter<String>.NonGenericInner> listOfNonGenericInner;

  /** A field whose type is a list of a wildcard bounded by a non-generic member class. */
  @SuppressWarnings("unused")
  private static List<? extends GenericOuter<String>.NonGenericInner> listOfWildcardNonGenericInner;

  @Test
  public void testBoundWithVariableInOwnerType() {
    // Creating the type parameter does not recurse forever through the owner type of its bound.
    TypeVariable t =
        GenericClassType.forClass(OwnerRecursiveBound.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    Substitution stringSubstitution = new Substitution(t, JavaTypes.STRING_TYPE);
    Type stringOuter = Type.forType(fieldType(ParameterBoundTest.class, "innerOfStringOuter"));
    Type integerOuter = Type.forType(fieldType(ParameterBoundTest.class, "innerOfIntegerOuter"));
    assertTrue(bound.isUpperBound(stringOuter, stringSubstitution));
    assertFalse(bound.isUpperBound(integerOuter, stringSubstitution));
  }

  @Test
  public void testLazyBoundKeepsFixedOwnerType() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(FixedOwnerBound.class).getTypeParameters();
    TypeVariable v = parameters.get(0);
    ParameterBound bound = parameters.get(1).getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    Substitution stringSubstitution = new Substitution(v, JavaTypes.STRING_TYPE);
    Type stringOuter = Type.forType(fieldType(ParameterBoundTest.class, "innerOfStringOuter"));
    Type integerOuter = Type.forType(fieldType(ParameterBoundTest.class, "innerOfIntegerOuter"));
    assertTrue(bound.isUpperBound(stringOuter, stringSubstitution));
    // The bound is Outer<String>.Inner<String>, not Inner<String> with any owner type.
    assertFalse(bound.isUpperBound(integerOuter, stringSubstitution));
  }

  @Test
  public void testNonGenericMemberClassBoundWithVariableInOwnerType() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(OwnerVariableMemberBound.class).getTypeParameters();
    TypeVariable x = parameters.get(0);
    ParameterBound bound = parameters.get(1).getUpperTypeBound();
    Type stringOuter =
        Type.forType(fieldType(ParameterBoundTest.class, "nonGenericInnerOfStringOuter"));
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    assertTrue(bound.isUpperBound(stringOuter, new Substitution(x, JavaTypes.STRING_TYPE)));
    assertFalse(bound.isUpperBound(stringOuter, new Substitution(x, integerType)));
  }

  @Test
  public void testCompareWithFixedOwnerMemberClassBound() {
    ParameterBound memberBound =
        ParameterBound.forType(
            Collections.emptySet(),
            fieldType(ParameterBoundTest.class, "nonGenericInnerOfStringOuter"));
    assertTrue(memberBound instanceof LazyParameterBound);
    ReferenceType stringOuter =
        ReferenceType.forType(fieldType(ParameterBoundTest.class, "nonGenericInnerOfStringOuter"));
    ReferenceType integerOuter =
        ReferenceType.forType(fieldType(ParameterBoundTest.class, "integerOwnerInner"));
    ReferenceBound objectBound = new EagerReferenceBound(JavaTypes.OBJECT_TYPE);
    ReferenceBound stringOuterBound = new EagerReferenceBound(stringOuter);
    ReferenceBound integerOuterBound = new EagerReferenceBound(integerOuter);
    Substitution empty = new Substitution();

    assertTrue(objectBound.isUpperBound(memberBound, empty));
    assertTrue(stringOuterBound.isUpperBound(memberBound, empty));
    assertFalse(integerOuterBound.isUpperBound(memberBound, empty));

    assertFalse(objectBound.isLowerBound(memberBound, empty));
    assertTrue(stringOuterBound.isLowerBound(memberBound, empty));
    assertFalse(integerOuterBound.isLowerBound(memberBound, empty));
  }

  @Test
  public void testCompareWithVariableOwnerMemberClassBound() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(OwnerVariableMemberBound.class).getTypeParameters();
    TypeVariable x = parameters.get(0);
    ParameterBound memberBound = parameters.get(1).getUpperTypeBound();
    assertTrue(memberBound instanceof LazyParameterBound);
    ReferenceBound stringOuterBound =
        new EagerReferenceBound(
            ReferenceType.forType(
                fieldType(ParameterBoundTest.class, "nonGenericInnerOfStringOuter")));
    Substitution stringSubstitution = new Substitution(x, JavaTypes.STRING_TYPE);
    Substitution integerSubstitution = new Substitution(x, ReferenceType.forClass(Integer.class));

    // The substitution is applied to the receiver, memberBound, so its owner type is instantiated.
    assertTrue(memberBound.isUpperBound(stringOuterBound, stringSubstitution));
    assertFalse(memberBound.isUpperBound(stringOuterBound, integerSubstitution));
    assertTrue(memberBound.isLowerBound(stringOuterBound, stringSubstitution));
    assertFalse(memberBound.isLowerBound(stringOuterBound, integerSubstitution));

    // The substitution is not applied to memberBound when it is the argument, so X stands for
    // itself and the owner type GenericOuter<X> differs from GenericOuter<String>.
    assertFalse(stringOuterBound.isUpperBound(memberBound, stringSubstitution));
    assertFalse(stringOuterBound.isUpperBound(memberBound, integerSubstitution));
    assertFalse(stringOuterBound.isLowerBound(memberBound, stringSubstitution));
    assertFalse(stringOuterBound.isLowerBound(memberBound, integerSubstitution));
  }

  @Test
  public void testWildcardBoundedByNonGenericMemberClass() {
    InstantiatedType list =
        (InstantiatedType)
            Type.forType(fieldType(ParameterBoundTest.class, "listOfNonGenericInner"));
    InstantiatedType wildcardList =
        (InstantiatedType)
            Type.forType(fieldType(ParameterBoundTest.class, "listOfWildcardNonGenericInner"));
    assertTrue(list.isSubtypeOfOrEqualTo(wildcardList));
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

    /** A field whose wildcard bound is a subtype of that of {@link #first}. */
    List<? extends ArrayList<U[]>> arrayList;
  }

  @Test
  public void testWildcardBoundWithArrayOfVariable() {
    Type firstType = Type.forType(fieldType(WildcardWithArrayOfVariable.class, "first"));
    Type secondType = Type.forType(fieldType(WildcardWithArrayOfVariable.class, "second"));
    assertTrue(firstType.isSubtypeOfOrEqualTo(secondType));
    // The wildcards differ, so the comparison resolves their bounds.
    Type arrayListType = Type.forType(fieldType(WildcardWithArrayOfVariable.class, "arrayList"));
    assertTrue(arrayListType.isSubtypeOfOrEqualTo(firstType));
    assertFalse(firstType.isSubtypeOfOrEqualTo(arrayListType));
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
}
