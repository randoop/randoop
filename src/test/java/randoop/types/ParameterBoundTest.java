package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/** Tests {@link ParameterBound} and the construction of type parameters and their bounds. */
public class ParameterBoundTest {

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
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), arrayType);
    Type argType = Type.forType(arrayType);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(argType, empty));
    assertTrue(bound.isLowerBound(argType, empty));
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

    assertTrue(stringOuterBound.isUpperBound(memberBound, stringSubstitution));
    assertFalse(stringOuterBound.isUpperBound(memberBound, integerSubstitution));
    assertTrue(stringOuterBound.isLowerBound(memberBound, stringSubstitution));
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
}
