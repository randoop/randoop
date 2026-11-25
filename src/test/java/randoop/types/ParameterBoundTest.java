package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/** Tests {@link ParameterBound}. */
public class ParameterBoundTest {

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

  /**
   * A field whose type is a non-generic member class whose owner type has the type argument String.
   */
  @SuppressWarnings("unused")
  private static GenericOuter<String>.NonGenericInner stringNonGenericInner;

  /**
   * A field whose type is a non-generic member class whose owner type has the type argument
   * Integer.
   */
  @SuppressWarnings("unused")
  private static GenericOuter<Integer>.NonGenericInner integerNonGenericInner;

  @Test
  public void testNonGenericMemberClassBound() {
    TypeVariable t =
        GenericClassType.forClass(NonGenericMemberClassBound.class).getTypeParameters().get(0);
    ReferenceType stringInner =
        (ReferenceType) Type.forType(fieldType(ParameterBoundTest.class, "stringNonGenericInner"));
    ReferenceType integerInner =
        (ReferenceType) Type.forType(fieldType(ParameterBoundTest.class, "integerNonGenericInner"));
    assertTrue(t.canBeInstantiatedBy(stringInner));
    assertFalse(t.canBeInstantiatedBy(integerInner));
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  /**
   * A class whose second type parameter is bounded by a non-generic member class whose owner type
   * has the first type parameter as its type argument.
   *
   * @param <U> the type parameter that is the type argument of the owner type in the bound of
   *     {@code T}
   * @param <T> the type parameter whose bound is a non-generic member class
   */
  @SuppressWarnings("unused")
  private static class OwnerVariableMemberClassBound<
      U, T extends GenericOuter<U>.NonGenericInner> {}

  @Test
  public void testSubstituteIntoOwnerTypeOfMemberClassBound() {
    List<TypeVariable> typeParameters =
        GenericClassType.forClass(OwnerVariableMemberClassBound.class).getTypeParameters();
    ParameterBound bound =
        typeParameters
            .get(1)
            .getUpperTypeBound()
            .substitute(new Substitution(typeParameters.get(0), JavaTypes.STRING_TYPE));
    ReferenceType stringInner =
        (ReferenceType) Type.forType(fieldType(ParameterBoundTest.class, "stringNonGenericInner"));
    assertEquals(ParameterBound.forType(stringInner), bound);
  }

  @Test
  public void testEagerMemberClassBound() {
    ReferenceType stringInner =
        (ReferenceType) Type.forType(fieldType(ParameterBoundTest.class, "stringNonGenericInner"));
    ReferenceType integerInner =
        (ReferenceType) Type.forType(fieldType(ParameterBoundTest.class, "integerNonGenericInner"));
    ParameterBound bound = ParameterBound.forType(stringInner);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(stringInner, empty));
    assertFalse(bound.isUpperBound(integerInner, empty));
    assertFalse(bound.isUpperBound(JDKTypes.LIST_TYPE.instantiate(JavaTypes.STRING_TYPE), empty));
    assertTrue(bound.isLowerBound(stringInner, empty));
    assertFalse(ParameterBound.forType(JavaTypes.STRING_TYPE).isLowerBound(stringInner, empty));
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
   * A class whose second type parameter is bounded by a generic member class whose owner type has
   * the first type parameter as its type argument. The first type parameter occurs only in the
   * owner type.
   *
   * @param <U> the type parameter that is the type argument of the owner type in the bound of
   *     {@code T}
   * @param <T> the type parameter whose bound is a generic member class
   */
  @SuppressWarnings("unused")
  private static class OwnerVariableGenericMemberClassBound<U, T extends Outer<U>.Inner<Integer>> {}

  /** A field whose type is a generic member class whose owner type has the type argument String. */
  @SuppressWarnings("unused")
  private static Outer<String>.Inner<Integer> stringOuterIntegerInner;

  /** A field whose type is a generic member class whose owner type has the type argument Long. */
  @SuppressWarnings("unused")
  private static Outer<Long>.Inner<Integer> longOuterIntegerInner;

  @Test
  public void testLazyGenericMemberClassBoundWithVariableInOwnerType() {
    // The bound of T is eager, because T does not occur in it.  Test a lazy bound for the same
    // type, as a bound that is lazy for another reason would contain.
    java.lang.reflect.Type boundType =
        OwnerVariableGenericMemberClassBound.class.getTypeParameters()[1].getBounds()[0];
    LazyParameterBound bound = new LazyParameterBound(boundType);
    TypeVariable u =
        GenericClassType.forClass(OwnerVariableGenericMemberClassBound.class)
            .getTypeParameters()
            .get(0);
    assertEquals(Collections.singletonList(u), bound.getTypeParameters());
    Substitution substitution = new Substitution(u, JavaTypes.STRING_TYPE);
    ReferenceType stringOuter =
        (ReferenceType)
            Type.forType(fieldType(ParameterBoundTest.class, "stringOuterIntegerInner"));
    ReferenceType longOuter =
        (ReferenceType) Type.forType(fieldType(ParameterBoundTest.class, "longOuterIntegerInner"));
    assertEquals(ParameterBound.forType(stringOuter), bound.substitute(substitution));
    assertTrue(bound.isUpperBound(stringOuter, substitution));
    assertFalse(bound.isUpperBound(longOuter, substitution));
  }

  /**
   * A class whose second type parameter is bounded by a generic member class whose owner type has
   * the first type parameter as its type argument and whose own type argument is the second type
   * parameter.
   *
   * @param <U> the type parameter that is the type argument of the owner type in the bound of
   *     {@code T}
   * @param <T> the type parameter whose bound is a generic member class
   */
  @SuppressWarnings("unused")
  private static class OwnerVariableSelfArgumentBound<U, T extends Outer<U>.Inner<T>> {}

  /**
   * A class that satisfies the bound of {@link OwnerVariableSelfArgumentBound} when {@code U} is
   * String.
   */
  private static class StringOwnerSelfInner extends Outer<String>.Inner<StringOwnerSelfInner> {
    /**
     * Creates a {@code StringOwnerSelfInner}.
     *
     * @param owner the enclosing instance of the superclass
     */
    StringOwnerSelfInner(Outer<String> owner) {
      owner.super();
    }
  }

  @Test
  public void testGenericMemberClassBoundWithSiblingInOwnerType() {
    List<TypeVariable> typeParameters =
        GenericClassType.forClass(OwnerVariableSelfArgumentBound.class).getTypeParameters();
    ParameterBound bound = typeParameters.get(1).getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    ReferenceType argType = ClassOrInterfaceType.forClass(StringOwnerSelfInner.class);
    Substitution stringSubstitution =
        new Substitution(typeParameters, JavaTypes.STRING_TYPE, argType);
    assertTrue(bound.isUpperBound(argType, stringSubstitution));
    Substitution longSubstitution =
        new Substitution(typeParameters, ClassOrInterfaceType.forClass(Long.class), argType);
    assertFalse(bound.isUpperBound(argType, longSubstitution));
  }

  /**
   * A class whose type parameter is bounded by a generic member class whose owner type has the type
   * parameter as its type argument.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("unused")
  private static class SelfOwnerGenericMemberClassBound<T extends Outer<T>.Inner<Integer>> {}

  /** A class that satisfies the bound of {@link SelfOwnerGenericMemberClassBound}. */
  private static class SelfOwnerInner extends Outer<SelfOwnerInner>.Inner<Integer> {
    /**
     * Creates a {@code SelfOwnerInner}.
     *
     * @param owner the enclosing instance of the superclass
     */
    SelfOwnerInner(Outer<SelfOwnerInner> owner) {
      owner.super();
    }
  }

  /**
   * A class that does not satisfy the bound of {@link SelfOwnerGenericMemberClassBound}, because
   * the type argument of its superclass's owner type is not the class itself.
   */
  private static class StringOwnerInner extends Outer<String>.Inner<Integer> {
    /**
     * Creates a {@code StringOwnerInner}.
     *
     * @param owner the enclosing instance of the superclass
     */
    StringOwnerInner(Outer<String> owner) {
      owner.super();
    }
  }

  @Test
  public void testSelfOwnerGenericMemberClassBound() {
    TypeVariable t =
        GenericClassType.forClass(SelfOwnerGenericMemberClassBound.class)
            .getTypeParameters()
            .get(0);
    assertTrue(t.getUpperTypeBound() instanceof LazyParameterBound);
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
    assertTrue(t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(SelfOwnerInner.class)));
    assertFalse(t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(StringOwnerInner.class)));
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
   * A class whose type parameter is bounded by a member class of the class, whose owner type has
   * the type parameter as its type argument.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings({"unused", "ClassCanBeStatic"})
  private static class SelfMemberClassBound<T extends SelfMemberClassBound<T>.Member> {
    /** An inner class whose type has type arguments only through its owner type. */
    class Member {}
  }

  /** A class that satisfies the bound of {@link SelfMemberClassBound}. */
  private static class SelfMemberClassBoundMember
      extends SelfMemberClassBound<SelfMemberClassBoundMember>.Member {
    /**
     * Creates a {@code SelfMemberClassBoundMember}.
     *
     * @param owner the enclosing instance of the superclass
     */
    SelfMemberClassBoundMember(SelfMemberClassBound<SelfMemberClassBoundMember> owner) {
      owner.super();
    }
  }

  @Test
  public void testSelfMemberClassBound() {
    TypeVariable t =
        GenericClassType.forClass(SelfMemberClassBound.class).getTypeParameters().get(0);
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
    assertTrue(
        t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(SelfMemberClassBoundMember.class)));
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
    List<TypeVariable> parameters = GenericClassType.forClass(SelfBound.class).getTypeParameters();
    assertEquals(1, parameters.size());
    ReferenceType boundType =
        ((ReferenceBound) parameters.get(0).getUpperTypeBound()).getBoundType();
    assertEquals(
        "java.util.List<? extends randoop.types.ParameterBoundTest$SelfBound<?>>",
        boundType.toString());
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
}
