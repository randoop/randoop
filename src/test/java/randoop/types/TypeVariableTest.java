package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import randoop.condition.RandoopSpecificationError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.test.VariablesInput;

public class TypeVariableTest {
  @Test
  public void testInstantiationPredicate() throws RandoopSpecificationError {
    Class<?> c = VariablesInput.class;
    Set<Method> methods =
        new TreeSet<>(
            new Comparator<Method>() {
              @Override
              public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
              }
            });
    Collections.addAll(methods, c.getDeclaredMethods());
    for (Method m : methods) {
      if (!m.getName().equals("$jacocoInit")) {
        TypedClassOperation operation = TypedOperation.forMethod(m).applyCaptureConversion();

        ReferenceType parameterType = (ReferenceType) operation.getInputTypes().get(1);
        TypeVariable variable;
        if (!parameterType.isVariable()) {
          ReferenceArgument argument =
              (ReferenceArgument) ((InstantiatedType) parameterType).getTypeArguments().get(0);
          parameterType = argument.getReferenceType();
        }
        assertTrue(parameterType.isVariable());
        variable = (TypeVariable) parameterType;
        if (!(m.getName().equals("m05")
            || m.getName().equals("m06")
            || m.getName().equals("m08"))) {
          assertTrue(
              "variable " + variable + " from " + m.getName() + " should be instantiable by String",
              variable.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
        }
        if (m.getName().equals("m05") || m.getName().equals("m06") || m.getName().equals("m08")) {
          assertFalse(
              "variable "
                  + variable
                  + " from "
                  + m.getName()
                  + " should not be instantiable by String",
              variable.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
        }
        assertTrue(
            "variable " + variable + " from " + m.getName() + " should be instantiable by Integer",
            variable.canBeInstantiatedBy(JavaTypes.INT_TYPE.toBoxedPrimitive()));
      }
    }
  }

  /**
   * A type parameter whose self-referential bound contains an array type.
   *
   * @param <T> the type parameter
   */
  static class SelfArrayBound<T extends Map<T, T[]>> {}

  /** A class that satisfies the bound of {@link SelfArrayBound}. */
  @SuppressWarnings("serial")
  static class SelfArrayMap extends HashMap<SelfArrayMap, SelfArrayMap[]> {}

  /**
   * A type parameter whose bound has a wildcard bounded by an array of the type parameter.
   *
   * @param <T> the type parameter
   */
  static class ArrayWildcardBound<T extends Collection<? extends T[]>> {}

  /** A class that satisfies the bound of {@link ArrayWildcardBound}. */
  @SuppressWarnings("serial")
  static class ArrayCollection extends ArrayList<ArrayCollection[]> {}

  @Test
  public void testArrayInLazyBound() {
    TypeVariable t = GenericClassType.forClass(SelfArrayBound.class).getTypeParameters().get(0);
    assertTrue(t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(SelfArrayMap.class)));
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  @Test
  public void testArrayInWildcardInLazyBound() {
    TypeVariable t = GenericClassType.forClass(ArrayWildcardBound.class).getTypeParameters().get(0);
    assertTrue(t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(ArrayCollection.class)));
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  /**
   * A type parameter whose bound refers to an array of the type parameter.
   *
   * @param <T> the type parameter
   */
  static class ComparableToArrayBound<T extends Comparable<T[]>> {}

  /** A class that satisfies the bound of {@link ComparableToArrayBound}. */
  abstract static class ComparableToArray implements Comparable<ComparableToArray[]> {}

  /**
   * A type parameter {@code U} whose bound has a wildcard bounded by an array of a sibling.
   *
   * @param <T> the component type
   * @param <U> the type parameter
   */
  static class ArrayOfSiblingBound<T, U extends Collection<? extends T[]>> {}

  /**
   * A type parameter, to substitute for another one.
   *
   * @param <V> the type parameter
   */
  static class OneParameter<V> {}

  @Test
  public void testSelfArrayInBound() {
    TypeVariable t =
        GenericClassType.forClass(ComparableToArrayBound.class).getTypeParameters().get(0);
    assertTrue(t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(ComparableToArray.class)));
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  @Test
  public void testSubstituteArrayInBoundTwice() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(ArrayOfSiblingBound.class).getTypeParameters();
    TypeVariable t = parameters.get(0);
    TypeVariable u = parameters.get(1);
    TypeVariable v = GenericClassType.forClass(OneParameter.class).getTypeParameters().get(0);
    TypeVariable throughV =
        (TypeVariable)
            ((TypeVariable) u.substitute(new Substitution(t, v)))
                .substitute(new Substitution(v, JavaTypes.STRING_TYPE));
    TypeVariable direct = (TypeVariable) u.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertEquals(direct.getUpperTypeBound(), throughV.getUpperTypeBound());
  }

  /**
   * Mutually recursive type parameters.
   *
   * @param <S> a type parameter whose bound refers to {@code T}
   * @param <T> a type parameter whose bound refers to {@code S}
   */
  static class MutualBound<S extends Comparable<T>, T extends List<S>> {}

  /**
   * Mutually recursive type parameters that {@code String} satisfies.
   *
   * @param <S> a type parameter whose bound refers to {@code T}
   * @param <T> a type parameter whose bound refers to {@code S}
   */
  static class MutualComparableBound<S extends Comparable<T>, T extends Comparable<S>> {}

  /**
   * Mutually recursive type parameters.
   *
   * @param <X> a type parameter whose bound refers to {@code Y}
   * @param <Y> a type parameter whose bound refers to {@code X}
   */
  static class ComparableToListBound<X extends Comparable<Y>, Y extends List<X>> {}

  /** A class that satisfies the bounds of {@link ComparableToListBound}, with Y := ArrayList. */
  abstract static class ComparableToListOfSelf
      implements Comparable<ArrayList<ComparableToListOfSelf>> {}

  /**
   * A type parameter {@code Y} whose bound refers to a sibling with a self-referential bound.
   *
   * @param <X> a type parameter with a self-referential bound
   * @param <Y> a type parameter bounded by a list of {@code X}
   */
  static class ListOfSelfComparableBound<X extends Comparable<X>, Y extends List<X>> {}

  /** A class that is comparable to a different class. */
  abstract static class ComparableToOther implements Comparable<String> {}

  /**
   * A type parameter with a self-referential bound.
   *
   * @param <T> the type parameter
   */
  static class SelfComparableBound<T extends Comparable<T>> {}

  /**
   * A type parameter {@code Y} bounded by a list of a sibling.
   *
   * @param <X> the element type
   * @param <Y> a type parameter bounded by {@code List<X>}
   */
  static class ListBound<X, Y extends List<X>> {}

  /**
   * A class in the style of a fluent assertion library, whose type parameter {@code SELF} refers to
   * its sibling {@code ACTUAL}.
   *
   * @param <SELF> the type of this assertion
   * @param <ACTUAL> the type of the value under test
   */
  abstract static class AbstractAssert<SELF extends AbstractAssert<SELF, ACTUAL>, ACTUAL> {}

  /** A concrete instantiation of {@link AbstractAssert}. */
  static class ConcreteAssert extends AbstractAssert<ConcreteAssert, String> {}

  /**
   * Returns a type parameter of the given generic class.
   *
   * @param c a generic class
   * @param index the index of the type parameter
   * @return the type parameter of {@code c} at {@code index}
   */
  private static TypeVariable typeParameter(Class<?> c, int index) {
    return GenericClassType.forClass(c).getTypeParameters().get(index);
  }

  /** Fields whose types are used in tests. */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  static class FieldTypes {
    /** A wildcard type whose type parameter has a self-referential bound. */
    SelfComparableBound<?> selfComparableWildcard;

    /** A wildcard type with a lower bound. */
    Comparable<? super String> superStringWildcard;
  }

  @Test
  public void testLazyBoundOfCaptureWithLowerBound() {
    TypeVariable capture = captureVariable("superStringWildcard");
    ParameterBound bound = new LazyReferenceBound(capture);
    // String is the lower bound of the capture of `? super String`.
    assertTrue(
        new EagerReferenceBound(JavaTypes.STRING_TYPE).isLowerBound(bound, new Substitution()));
    assertFalse(
        new EagerReferenceBound(JavaTypes.OBJECT_TYPE).isLowerBound(bound, new Substitution()));
  }

  @Test
  public void testUpperBoundOfBound() {
    ParameterBound numberBound =
        new EagerReferenceBound(ClassOrInterfaceType.forClass(Number.class));
    // The supplied bound is compared, not the receiver.
    assertFalse(
        numberBound.isUpperBound(
            new EagerReferenceBound(JavaTypes.OBJECT_TYPE), new Substitution()));
    assertTrue(
        numberBound.isUpperBound(
            new EagerReferenceBound(JavaTypes.INT_TYPE.toBoxedPrimitive()), new Substitution()));
  }

  /**
   * A type parameter bounded by a sibling.
   *
   * @param <K> a type parameter
   * @param <V> a type parameter bounded by {@code K}
   */
  static class SiblingBound<K, V extends K> {}

  @Test
  public void testVariableBoundResolvedToConcreteType() {
    TypeVariable k = typeParameter(SiblingBound.class, 0);
    TypeVariable v = typeParameter(SiblingBound.class, 1);
    ReferenceType number = ClassOrInterfaceType.forClass(Number.class);
    ReferenceType integer = JavaTypes.INT_TYPE.toBoxedPrimitive();
    assertTrue(v.canBeInstantiatedBy(integer, new Substitution(k, number)));
    assertFalse(v.canBeInstantiatedBy(JavaTypes.STRING_TYPE, new Substitution(k, number)));
  }

  @Test
  public void testLazyReferenceBoundUsesSubstitutionForUnknownVariable() {
    TypeVariable k = typeParameter(PartiallySubstitutedRecursiveBound.class, 0);
    TypeVariable v = typeParameter(PartiallySubstitutedRecursiveBound.class, 1);
    ReferenceType integerKeyed = ClassOrInterfaceType.forClass(IntegerKeyedMapOfSelf.class);
    ReferenceType stringKeyed = ClassOrInterfaceType.forClass(StringKeyedMapOfSelf.class);
    Substitution kIsString = new Substitution(k, JavaTypes.STRING_TYPE);
    // V is unknown, but with K := String its bound is Map<String, List<V>>, which an
    // Integer-keyed map violates.
    LazyReferenceBound bound = new LazyReferenceBound(v);
    assertFalse(bound.isUpperBound(integerKeyed, kIsString));
    assertTrue(bound.isUpperBound(stringKeyed, kIsString));
    assertFalse(bound.isLowerBound(integerKeyed, kIsString));
    assertTrue(bound.isLowerBound(stringKeyed, kIsString));
  }

  /**
   * A type parameter whose bound contains an array class.
   *
   * @param <T> a type parameter bounded by {@code Pair<T, String[]>}
   */
  static class ArrayClassPairBound<T extends Pair<T, String[]>> {}

  @Test
  public void testArrayClassInLazyBound() {
    TypeVariable t = typeParameter(ArrayClassPairBound.class, 0);
    assertTrue(t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(SelfStringArrayPair.class)));
    assertFalse(t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(SelfArrayPair.class)));
  }

  /**
   * Returns the type of a field of {@link FieldTypes}.
   *
   * @param name the name of the field
   * @return the type of the field
   */
  private static ReferenceType fieldType(String name) {
    try {
      return (ReferenceType) Type.forType(FieldTypes.class.getDeclaredField(name).getGenericType());
    } catch (NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Returns the capture variable for the single wildcard argument of the type of a field of {@link
   * FieldTypes}.
   *
   * @param name the name of the field
   * @return the capture variable for the wildcard argument of the field's type
   */
  private static TypeVariable captureVariable(String name) {
    InstantiatedType captured = ((InstantiatedType) fieldType(name)).applyCaptureConversion();
    return (TypeVariable)
        ((ReferenceArgument) captured.getTypeArguments().get(0)).getReferenceType();
  }

  @Test
  public void testCaptureVariableCopy() {
    TypeVariable capture = captureVariable("selfComparableWildcard");
    TypeVariable copy =
        capture.createCopyWithBounds(
            capture.getLowerTypeBound(), new EagerReferenceBound(JavaTypes.OBJECT_TYPE));
    assertEquals(capture, copy);
    assertEquals(capture.hashCode(), copy.hashCode());
  }

  /**
   * A class with a field whose type has a wildcard with a type parameter as its lower bound.
   *
   * @param <U> a type parameter with no bound
   */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  static class SuperWildcardField<U> {
    /** A wildcard type whose lower bound is a type parameter with no bound. */
    Comparable<? super U> superU;
  }

  @Test
  public void testSubstitutedCaptureVariableCopy() throws NoSuchFieldException {
    InstantiatedType captured =
        ((InstantiatedType)
                Type.forType(SuperWildcardField.class.getDeclaredField("superU").getGenericType()))
            .applyCaptureConversion();
    TypeVariable capture =
        (TypeVariable) ((ReferenceArgument) captured.getTypeArguments().get(0)).getReferenceType();
    // Substitution changes the lower bound of the capture of `? super U`, and its wildcard.
    ReferenceType copy =
        capture.substitute(
            new Substitution(typeParameter(SuperWildcardField.class, 0), JavaTypes.STRING_TYPE));
    assertTrue(copy instanceof CaptureTypeVariable);
    assertEquals(
        new EagerReferenceBound(JavaTypes.STRING_TYPE), ((TypeVariable) copy).getLowerTypeBound());
    assertEquals(capture, copy);
    assertEquals(capture.hashCode(), copy.hashCode());
  }

  @Test
  public void testSubstitutionOfCaptureVariableCopy() {
    TypeVariable capture = captureVariable("selfComparableWildcard");
    TypeVariable copy =
        capture.createCopyWithBounds(
            capture.getLowerTypeBound(), new EagerReferenceBound(JavaTypes.OBJECT_TYPE));
    Substitution substitution = new Substitution(capture, JavaTypes.STRING_TYPE);
    // A substitution for a capture variable applies to a copy of it with different bounds.
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(copy));
    assertEquals(JavaTypes.STRING_TYPE, copy.substitute(substitution));
  }

  /**
   * A type parameter {@code W} bounded by a sibling {@code V} whose recursive bound mentions
   * another sibling {@code K}.
   *
   * @param <K> a type parameter
   * @param <V> a type parameter bounded by {@code Map<K, List<V>>}
   * @param <W> a type parameter bounded by {@code V}
   */
  static class PartiallySubstitutedRecursiveBound<K, V extends Map<K, List<V>>, W extends V> {}

  /** A map from {@code Integer} to lists of itself. */
  abstract static class IntegerKeyedMapOfSelf
      implements Map<Integer, List<IntegerKeyedMapOfSelf>> {}

  /** A map from {@code String} to lists of itself. */
  abstract static class StringKeyedMapOfSelf implements Map<String, List<StringKeyedMapOfSelf>> {}

  @Test
  public void testVariableBoundWithPartialSubstitution() {
    TypeVariable k = typeParameter(PartiallySubstitutedRecursiveBound.class, 0);
    TypeVariable w = typeParameter(PartiallySubstitutedRecursiveBound.class, 2);
    ReferenceType integerKeyed = ClassOrInterfaceType.forClass(IntegerKeyedMapOfSelf.class);
    ReferenceType stringKeyed = ClassOrInterfaceType.forClass(StringKeyedMapOfSelf.class);
    Substitution kIsString = new Substitution(k, JavaTypes.STRING_TYPE);
    // With K := String, V's bound is Map<String, List<V>>, which IntegerKeyedMapOfSelf violates.
    assertFalse(
        w.getUpperTypeBound()
            .isUpperBound(integerKeyed, kIsString.extend(new Substitution(w, integerKeyed))));
    assertTrue(
        w.getUpperTypeBound()
            .isUpperBound(stringKeyed, kIsString.extend(new Substitution(w, stringKeyed))));
  }

  /**
   * A pair.
   *
   * @param <A> the type of the first element
   * @param <B> the type of the second element
   */
  interface Pair<A, B> {}

  /**
   * A type parameter whose bound contains a generic array type.
   *
   * @param <T> a type parameter bounded by {@code Pair<T, T[]>}
   */
  static class ArrayPairBound<T extends Pair<T, T[]>> {}

  /** A class that satisfies the bound of {@link ArrayPairBound}. */
  abstract static class SelfArrayPair implements Pair<SelfArrayPair, SelfArrayPair[]> {}

  /** A class that does not satisfy the bound of {@link ArrayPairBound}. */
  abstract static class SelfStringArrayPair implements Pair<SelfStringArrayPair, String[]> {}

  @Test
  public void testGenericArrayInLazyBound() {
    TypeVariable t = typeParameter(ArrayPairBound.class, 0);
    assertTrue(t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(SelfArrayPair.class)));
    assertFalse(t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(SelfStringArrayPair.class)));
  }

  /**
   * A type parameter whose bound contains a fixed array type.
   *
   * @param <T> a type parameter bounded by {@code Pair<T[], String[]>}
   */
  static class FixedArrayPairBound<T extends Pair<T[], String[]>> {}

  /** A class that satisfies the bound of {@link FixedArrayPairBound}. */
  abstract static class SelfArrayStringArrayPair
      implements Pair<SelfArrayStringArrayPair[], String[]> {}

  @Test
  public void testFixedArrayInLazyBound() {
    TypeVariable t = typeParameter(FixedArrayPairBound.class, 0);
    assertTrue(
        t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(SelfArrayStringArrayPair.class)));
  }

  /**
   * A type parameter bounded by another type parameter that has no bound.
   *
   * @param <K> a type parameter
   * @param <V> a type parameter bounded by {@code K}
   */
  static class UnboundedVariableBound<K, V extends K> {}

  @Test
  public void testInstantiatedVariableBound() {
    TypeVariable k = typeParameter(UnboundedVariableBound.class, 0);
    TypeVariable v = typeParameter(UnboundedVariableBound.class, 1);
    assertTrue(
        v.canBeInstantiatedBy(JavaTypes.STRING_TYPE, new Substitution(k, JavaTypes.OBJECT_TYPE)));
    assertFalse(
        v.canBeInstantiatedBy(JavaTypes.OBJECT_TYPE, new Substitution(k, JavaTypes.STRING_TYPE)));
  }

  @Test
  public void testSubstitutionOfTypeParameterCopy() {
    TypeVariable x = typeParameter(ListBound.class, 0);
    TypeVariable y = typeParameter(ListBound.class, 1);
    // Substituting into the bound of Y creates a copy of Y whose bound is List<String>.
    TypeVariable copy = (TypeVariable) y.substitute(new Substitution(x, JavaTypes.STRING_TYPE));
    assertEquals(y, copy);
    assertEquals(y.hashCode(), copy.hashCode());
    InstantiatedType listOfString =
        GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE);
    // A substitution for a type parameter applies to a copy of it with different bounds.
    Substitution substitution = new Substitution(y, listOfString);
    assertEquals(listOfString, substitution.get(copy));
    assertEquals(listOfString, copy.substitute(substitution));
  }

  /** Bounds whose types are copies of a type variable are equal and have equal hash codes. */
  @Test
  public void testBoundsOfTypeVariableCopiesAreEqual() {
    TypeVariable x = typeParameter(ListBound.class, 0);
    TypeVariable y = typeParameter(ListBound.class, 1);
    TypeVariable copy = (TypeVariable) y.substitute(new Substitution(x, JavaTypes.STRING_TYPE));
    ParameterBound bound = new EagerReferenceBound(y);
    ParameterBound copyBound = new LazyReferenceBound(copy);
    assertEquals(bound, copyBound);
    assertEquals(bound.hashCode(), copyBound.hashCode());
  }

  @Test
  public void testMutuallyRecursiveBound() {
    TypeVariable s = typeParameter(MutualBound.class, 0);
    // S := String would need a T that is both a List<String> and the T in Comparable<T>.
    assertFalse(s.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
    assertNull(ReferenceType.getInstantiatingSubstitutionforTypeVariable(JavaTypes.STRING_TYPE, s));

    TypeVariable comparableS = typeParameter(MutualComparableBound.class, 0);
    // S := String and T := String.
    assertTrue(comparableS.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
    assertNotNull(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
            JavaTypes.STRING_TYPE, comparableS));
  }

  @Test
  public void testMutuallyRecursiveInstantiation() {
    TypeVariable x = typeParameter(ComparableToListBound.class, 0);
    // X := ComparableToListOfSelf and Y := ArrayList<ComparableToListOfSelf>.
    assertTrue(x.canBeInstantiatedBy(ClassOrInterfaceType.forClass(ComparableToListOfSelf.class)));
    assertFalse(x.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  @Test
  public void testSiblingInBoundOfCandidate() {
    TypeVariable x = typeParameter(ListOfSelfComparableBound.class, 0);
    TypeVariable y = typeParameter(ListOfSelfComparableBound.class, 1);
    GenericClassType list = GenericClassType.forClass(List.class);
    // X := ComparableToOther does not satisfy X's bound Comparable<X>.
    assertFalse(
        y.canBeInstantiatedBy(
            list.instantiate(ClassOrInterfaceType.forClass(ComparableToOther.class))));
    assertTrue(y.canBeInstantiatedBy(list.instantiate(JavaTypes.STRING_TYPE)));
    // List<X> satisfies Y's bound, although X occurs in the replacement for Y.
    assertNotNull(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(list.instantiate(x), y));
  }

  @Test
  public void testSiblingTypeParameter() {
    TypeVariable self = typeParameter(AbstractAssert.class, 0);
    assertTrue(self.canBeInstantiatedBy(ClassOrInterfaceType.forClass(ConcreteAssert.class)));
    assertFalse(self.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  @Test
  public void testLazyReferenceBoundToUnmappedVariable() {
    LazyReferenceBound bound = new LazyReferenceBound(typeParameter(SelfComparableBound.class, 0));
    assertTrue(bound.isUpperBound(JavaTypes.STRING_TYPE, new Substitution()));
    assertFalse(bound.isUpperBound(JavaTypes.OBJECT_TYPE, new Substitution()));
  }

  @Test
  public void testLazyReferenceLowerBoundToUnmappedVariable() {
    // The bound is the unknown type variable T, with T extends Comparable<T>.  The argument is
    // within the bound if it can instantiate T.  Object cannot, because Object is not a
    // Comparable<Object>.
    LazyReferenceBound bound = new LazyReferenceBound(typeParameter(SelfComparableBound.class, 0));
    assertTrue(bound.isLowerBound(JavaTypes.STRING_TYPE, new Substitution()));
    assertFalse(bound.isLowerBound(JavaTypes.OBJECT_TYPE, new Substitution()));
  }

  @Test
  public void testCopyWithResolvedBound() {
    TypeVariable t = typeParameter(SelfComparableBound.class, 0);
    TypeVariable copy = (TypeVariable) t.substitute(new Substitution(t, t));
    assertEquals(t, copy);
    assertTrue(copy.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
    assertFalse(copy.canBeInstantiatedBy(JavaTypes.OBJECT_TYPE));
    // The copy's bound refers to the copy itself, not to an unknown.
    assertFalse(copy.canBeInstantiatedBy(ClassOrInterfaceType.forClass(ComparableToOther.class)));
  }

  @Test
  public void testIdentitySubstitutionKeepsCopy() {
    TypeVariable x = typeParameter(ListBound.class, 0);
    TypeVariable y = typeParameter(ListBound.class, 1);
    TypeVariable copy = (TypeVariable) y.substitute(new Substitution(x, JavaTypes.STRING_TYPE));
    InstantiatedType listOfString =
        GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE);
    assertEquals(listOfString, ((ReferenceBound) copy.getUpperTypeBound()).getBoundType());
    // A copy is the same type variable as the original.
    assertEquals(y, copy);
    assertEquals(y.hashCode(), copy.hashCode());
    // Mapping the type variable to itself keeps the copy's bound.
    TypeVariable result = (TypeVariable) copy.substitute(new Substitution(y, y));
    assertEquals(listOfString, ((ReferenceBound) result.getUpperTypeBound()).getBoundType());
  }

  /**
   * A class with a generic method whose type parameter's bound refers to the class's type
   * parameter.
   *
   * @param <E> the class's type parameter
   */
  static class Outer<E> {
    /**
     * A generic method.
     *
     * @param <T> a type parameter bounded by a list of the class's type parameter
     */
    <T extends List<E>> void m() {}

    /**
     * A generic inner class.
     *
     * @param <T> a type parameter bounded by {@code E}
     */
    class Inner<T extends E> {}
  }

  /**
   * A type parameter whose lazy bound has a sibling in a nested type argument, before the type
   * parameter.
   *
   * @param <S> a type parameter
   * @param <T> a type parameter bounded by {@code Map<List<S>, T>}
   */
  static class NestedSiblingFirst<S, T extends Map<List<S>, T>> {}

  /**
   * A type parameter whose lazy bound has a sibling in a nested type argument, after the type
   * parameter.
   *
   * @param <S> a type parameter
   * @param <T> a type parameter bounded by {@code Map<T, List<S>>}
   */
  static class NestedSiblingLast<S, T extends Map<T, List<S>>> {}

  @Test
  public void testSubstituteCopyForOriginal() {
    TypeVariable e = typeParameter(Outer.class, 0);
    // Not typeParameter(Outer.Inner.class, 0), which is E if the cached GenericClassType for Inner
    // has its enclosing type set.
    TypeVariable t = TypeVariable.forType(Outer.Inner.class.getTypeParameters()[0]);
    TypeVariable copy = (TypeVariable) t.substitute(new Substitution(e, JavaTypes.STRING_TYPE));
    // Substituting a copy with other bounds yields that copy.
    TypeVariable result = (TypeVariable) t.substitute(new Substitution(t, copy));
    assertEquals(copy.getUpperTypeBound(), result.getUpperTypeBound());
  }

  @Test
  public void testLazyTypeParametersOfNestedSibling() {
    for (Class<?> c : Arrays.asList(NestedSiblingFirst.class, NestedSiblingLast.class)) {
      List<TypeVariable> parameters = GenericClassType.forClass(c).getTypeParameters();
      TypeVariable s = parameters.get(0);
      TypeVariable t = parameters.get(1);
      ParameterBound bound = t.getUpperTypeBound().substitute(new Substitution(parameters, s, t));
      // The result does not depend on the order of the type arguments.
      assertTrue(bound instanceof LazyReferenceBound);
      assertTrue(((ReferenceBound) bound).getBoundType().getTypeParameters().contains(s));
    }
  }

  // Tests of isInstantiationOf for type variables with bounds.

  /**
   * A type parameter bounded by {@code Comparable<String>}.
   *
   * @param <T> the type parameter
   */
  static class ComparableStringBound<T extends Comparable<String>> {}

  /**
   * A type parameter bounded by {@code Comparable<String>}.
   *
   * @param <T> the type parameter
   */
  static class OtherComparableStringBound<T extends Comparable<String>> {}

  /**
   * A type parameter bounded by {@code Comparable<Integer>}.
   *
   * @param <T> the type parameter
   */
  static class ComparableIntegerBound<T extends Comparable<Integer>> {}

  /**
   * A type parameter bounded by {@code Comparable<?>}.
   *
   * @param <T> the type parameter
   */
  static class ComparableWildcardBound<T extends Comparable<?>> {}

  /**
   * A type parameter bounded by {@code Collection<?>}.
   *
   * @param <T> the type parameter
   */
  static class CollectionWildcardBound<T extends Collection<?>> {}

  /**
   * A type parameter bounded by {@code Number}.
   *
   * @param <T> the type parameter
   */
  static class NumberBound<T extends Number> {}

  /**
   * A type parameter bounded by an intersection type.
   *
   * @param <T> the type parameter
   */
  static class IntersectionBound<T extends Number & Comparable<Integer>> {}

  /**
   * A type parameter bounded by an intersection type.
   *
   * @param <T> the type parameter
   */
  static class OtherIntersectionBound<T extends Number & Comparable<Integer>> {}

  /**
   * A type parameter with a self-referential bound.
   *
   * @param <T> the type parameter
   */
  static class OtherSelfComparableBound<T extends Comparable<T>> {}

  /**
   * A type parameter with a self-referential bound.
   *
   * @param <T> the type parameter
   */
  static class SelfListBound<T extends List<T>> {}

  /**
   * A type parameter bounded by an intersection type with a self-referential member.
   *
   * @param <T> the type parameter
   */
  static class NumberSelfComparableBound<T extends Number & Comparable<T>> {}

  /**
   * A type parameter bounded by another type parameter.
   *
   * @param <S> the bound of {@code T}
   * @param <T> the type parameter
   */
  static class VariableBound<S extends Comparable<Integer>, T extends S> {}

  /**
   * A type parameter bounded by {@code Integer}.
   *
   * @param <T> the type parameter
   */
  static class IntegerBound<T extends Integer> {}

  /**
   * A type parameter bounded by {@code Comparable<? super Integer>}.
   *
   * @param <T> the type parameter
   */
  static class ComparableSuperIntegerBound<T extends Comparable<? super Integer>> {}

  /**
   * A type parameter with a self-referential bound.
   *
   * @param <E> the type parameter
   */
  static class SelfEnumBound<E extends Enum<E>> {}

  /**
   * A type parameter bounded by {@code Enum<?>}.
   *
   * @param <E> the type parameter
   */
  static class EnumWildcardBound<E extends Enum<?>> {}

  /**
   * A type parameter bounded by {@code List<String>}.
   *
   * @param <T> the type parameter
   */
  static class ListStringBound<T extends List<String>> {}

  /**
   * A type parameter bounded by {@code List<?>}.
   *
   * @param <T> the type parameter
   */
  static class ListWildcardBound<T extends List<?>> {}

  /**
   * A type parameter whose self-reference occurs in a wildcard bound.
   *
   * @param <T> the type parameter
   */
  static class SelfInWildcardBound<T extends Collection<? extends Comparable<T>>> {}

  /**
   * A type parameter whose self-reference occurs in a wildcard bound.
   *
   * @param <T> the type parameter
   */
  static class OtherSelfInWildcardBound<T extends Collection<? extends Comparable<T>>> {}

  /**
   * A type parameter bounded by another type parameter that has a bound.
   *
   * @param <W> the bound of {@code V}
   * @param <V> the type parameter
   */
  static class BoundedVariableBound<W extends Number, V extends W> {}

  /**
   * A type parameter bounded by a parameterized type with a nested type argument.
   *
   * @param <T> the type parameter
   */
  static class CollectionListStringBound<T extends Collection<List<String>>> {}

  /**
   * A type parameter whose bound contains a wildcard whose bound refers to the type parameter.
   *
   * @param <T> the type parameter
   */
  static class CollectionListSelfBound<T extends Collection<? extends List<T>>> {}

  /**
   * A type parameter whose self-referential bound contains an array type.
   *
   * @param <T> the type parameter
   */
  static class OtherSelfArrayBound<T extends Map<T, T[]>> {}

  /**
   * A type parameter bounded by {@code Map<?, ?>}.
   *
   * @param <T> the type parameter
   */
  static class MapWildcardBound<T extends Map<?, ?>> {}

  /**
   * A subclass of {@link AbstractAssert} that passes its own type parameters to it.
   *
   * @param <SELF> the type of this assertion
   * @param <ACTUAL> the type of the value under test
   */
  abstract static class AbstractObjectAssert<
          SELF extends AbstractObjectAssert<SELF, ACTUAL>, ACTUAL>
      extends AbstractAssert<SELF, ACTUAL> {}

  /**
   * A type parameter bounded by an {@link AbstractAssert} whose value type is {@code String}.
   *
   * @param <SELF> the type parameter
   */
  static class StringAssertBound<SELF extends AbstractAssert<SELF, String>> {}

  /**
   * A generic interface with two type parameters.
   *
   * @param <X> the first type parameter
   * @param <Y> the second type parameter
   */
  interface Related<X, Y> {}

  /** A class that is related to itself and to {@code String}. */
  static class RelatedToString implements Related<RelatedToString, String> {}

  /**
   * A type parameter bounded by {@link RelatedToString}.
   *
   * @param <T> the type parameter
   */
  static class RelatedToStringBound<T extends RelatedToString> {}

  /**
   * A type parameter {@code V} bounded by its sibling {@code S}, whose bound refers to a sibling
   * {@code B} that does not occur in the bound of {@code V}.
   *
   * @param <S> a type parameter whose bound refers to {@code B}
   * @param <B> a type parameter
   * @param <V> a type parameter bounded by {@code S}
   */
  static class SiblingOfSiblingBound<S extends Related<S, B>, B, V extends S> {}

  /** A class that is related to itself and to {@code Integer}. */
  static class RelatedToInteger implements Related<RelatedToInteger, Integer> {}

  /**
   * A type parameter bounded by {@link RelatedToInteger}.
   *
   * @param <T> the type parameter
   */
  static class RelatedToIntegerBound<T extends RelatedToInteger> {}

  /**
   * Like {@link SiblingOfSiblingBound}, but {@code B} has a bound.
   *
   * @param <S> a type parameter whose bound refers to {@code B}
   * @param <B> a type parameter bounded by {@code Number}
   * @param <V> a type parameter bounded by {@code S}
   */
  static class SiblingOfBoundedSiblingBound<
      S extends Related<S, B>, B extends Number, V extends S> {}

  /**
   * A type parameter bounded by {@code List<? extends Integer>}.
   *
   * @param <T> the type parameter
   */
  static class ListExtendsIntegerBound<T extends List<? extends Integer>> {}

  /**
   * A type parameter bounded by {@code List<? extends String>}.
   *
   * @param <T> the type parameter
   */
  static class ListExtendsStringBound<T extends List<? extends String>> {}

  /**
   * A type parameter bounded by {@code List<? super Integer>}.
   *
   * @param <T> the type parameter
   */
  static class ListSuperIntegerBound<T extends List<? super Integer>> {}

  /**
   * A type parameter bounded by {@code List<? super Number>}.
   *
   * @param <T> the type parameter
   */
  static class ListSuperNumberBound<T extends List<? super Number>> {}

  /**
   * A type parameter bounded by {@code List<? super String>}.
   *
   * @param <T> the type parameter
   */
  static class ListSuperStringBound<T extends List<? super String>> {}

  /**
   * A type parameter bounded by {@code List<Object>}.
   *
   * @param <T> the type parameter
   */
  static class ListObjectBound<T extends List<Object>> {}

  /**
   * A type parameter bounded by {@code List<String[]>}.
   *
   * @param <T> the type parameter
   */
  static class ListStringArrayBound<T extends List<String[]>> {}

  /**
   * A type parameter bounded by {@code List<Number[]>}.
   *
   * @param <T> the type parameter
   */
  static class ListNumberArrayBound<T extends List<Number[]>> {}

  /**
   * A type parameter {@code T} whose bound has a wildcard bounded by a sibling.
   *
   * @param <B> the bound of the wildcard
   * @param <T> the type parameter
   */
  static class ExtendsSiblingBound<B extends Number, T extends List<? extends B>> {}

  /**
   * A type parameter {@code T} whose bound has a wildcard with a sibling as its lower bound.
   *
   * @param <B> the lower bound of the wildcard
   * @param <T> the type parameter
   */
  static class SuperSiblingBound<B extends Number, T extends List<? super B>> {}

  /**
   * A type parameter {@code T} whose bound has a sibling as a type argument.
   *
   * @param <S> the type argument
   * @param <T> the type parameter
   */
  static class SiblingArgumentBound<S extends Number, T extends List<S>> {}

  /**
   * A type parameter {@code T} whose bound has an array of a sibling as a type argument.
   *
   * @param <S> the component type
   * @param <T> the type parameter
   */
  static class SiblingArrayBound<S extends Comparable<S>, T extends List<S[]>> {}

  /**
   * A type parameter {@code T} whose bound has as a type argument a sibling with a self-referential
   * bound.
   *
   * @param <S> the type argument
   * @param <T> the type parameter
   */
  static class SelfComparableSiblingArgumentBound<S extends Comparable<S>, T extends List<S>> {}

  /**
   * A type parameter {@code U} bounded by a sibling with a self-referential bound.
   *
   * @param <S> the bound of {@code U}
   * @param <U> the type parameter
   */
  static class SelfComparableSiblingBound<S extends Comparable<S>, U extends S> {}

  /**
   * A type parameter whose bound has a wildcard bounded by an array of the type parameter.
   *
   * @param <T> the type parameter
   */
  static class OtherArrayWildcardBound<T extends Collection<? extends T[]>> {}

  /**
   * Returns the first type parameter of the given generic class.
   *
   * @param c a generic class
   * @return the first type parameter of {@code c}
   */
  private static TypeVariable typeParameter(Class<?> c) {
    return typeParameter(c, 0);
  }

  @Test
  public void testSameBound() {
    assertTrue(
        typeParameter(ComparableStringBound.class)
            .isInstantiationOf(typeParameter(OtherComparableStringBound.class)));
  }

  @Test
  public void testIncompatibleBound() {
    assertFalse(
        typeParameter(ComparableStringBound.class)
            .isInstantiationOf(typeParameter(ComparableIntegerBound.class)));
  }

  @Test
  public void testIntersectionBound() {
    TypeVariable t = typeParameter(IntersectionBound.class);
    assertTrue(t.isInstantiationOf(typeParameter(NumberBound.class)));
    assertTrue(t.isInstantiationOf(typeParameter(ComparableIntegerBound.class)));
    assertTrue(t.isInstantiationOf(typeParameter(OtherIntersectionBound.class)));
    assertFalse(t.isInstantiationOf(typeParameter(ComparableStringBound.class)));
    assertFalse(typeParameter(NumberBound.class).isInstantiationOf(t));
  }

  @Test
  public void testSelfReferentialBound() {
    TypeVariable t = typeParameter(SelfComparableBound.class);
    assertTrue(t.isInstantiationOf(typeParameter(OtherSelfComparableBound.class)));
    assertTrue(t.isInstantiationOf(typeParameter(ComparableWildcardBound.class)));
    assertFalse(t.isInstantiationOf(typeParameter(ComparableStringBound.class)));
    assertFalse(t.isInstantiationOf(typeParameter(NumberBound.class)));
    assertFalse(typeParameter(ComparableStringBound.class).isInstantiationOf(t));

    TypeVariable list = typeParameter(SelfListBound.class);
    assertTrue(list.isInstantiationOf(typeParameter(CollectionWildcardBound.class)));
    assertFalse(list.isInstantiationOf(typeParameter(ComparableWildcardBound.class)));
  }

  @Test
  public void testIntersectionWithSelfReferentialMember() {
    TypeVariable t = typeParameter(NumberSelfComparableBound.class);
    assertTrue(t.isInstantiationOf(typeParameter(NumberBound.class)));
    assertTrue(t.isInstantiationOf(typeParameter(ComparableWildcardBound.class)));
    assertFalse(t.isInstantiationOf(typeParameter(ComparableStringBound.class)));
  }

  @Test
  public void testVariableBound() {
    TypeVariable t = typeParameter(VariableBound.class, 1);
    assertTrue(t.isInstantiationOf(typeParameter(ComparableIntegerBound.class)));
    assertFalse(t.isInstantiationOf(typeParameter(ComparableStringBound.class)));
  }

  @Test
  public void testWildcardInBound() {
    TypeVariable comparableWildcard = typeParameter(ComparableWildcardBound.class);
    assertTrue(typeParameter(ComparableStringBound.class).isInstantiationOf(comparableWildcard));
    assertTrue(
        typeParameter(SelfEnumBound.class)
            .isInstantiationOf(typeParameter(EnumWildcardBound.class)));
    assertTrue(
        typeParameter(IntegerBound.class)
            .isInstantiationOf(typeParameter(ComparableSuperIntegerBound.class)));
    assertFalse(
        typeParameter(ComparableStringBound.class)
            .isInstantiationOf(typeParameter(ComparableSuperIntegerBound.class)));
    assertTrue(
        typeParameter(ListStringBound.class)
            .isInstantiationOf(typeParameter(CollectionWildcardBound.class)));
    assertFalse(typeParameter(NumberBound.class).isInstantiationOf(comparableWildcard));
    assertTrue(
        typeParameter(ComparableSuperIntegerBound.class).isInstantiationOf(comparableWildcard));
  }

  @Test
  public void testSelfReferenceInWildcardBound() {
    TypeVariable t = typeParameter(SelfInWildcardBound.class);
    assertTrue(t.isInstantiationOf(typeParameter(OtherSelfInWildcardBound.class)));
    assertTrue(t.isInstantiationOf(typeParameter(CollectionWildcardBound.class)));
    assertFalse(t.isInstantiationOf(typeParameter(ListWildcardBound.class)));
  }

  @Test
  public void testInstantiationOfMutuallyRecursiveBound() {
    TypeVariable s = typeParameter(MutualBound.class, 0);
    TypeVariable t = typeParameter(MutualBound.class, 1);
    assertTrue(s.isInstantiationOf(typeParameter(ComparableWildcardBound.class)));
    assertTrue(t.isInstantiationOf(typeParameter(ListWildcardBound.class)));
    assertTrue(t.isInstantiationOf(typeParameter(CollectionWildcardBound.class)));
    assertFalse(t.isInstantiationOf(typeParameter(ComparableWildcardBound.class)));
    // MutualBound.T's bound is List<S>.  S stands for an unknown type that must satisfy its bound
    // Comparable<T>, with ListStringBound.T for T.  S would have to be String, and String is not a
    // Comparable<ListStringBound.T>.
    assertFalse(typeParameter(ListStringBound.class).isInstantiationOf(t));
  }

  @Test
  public void testBoundedVariableBound() {
    TypeVariable v = typeParameter(BoundedVariableBound.class, 1);
    assertTrue(typeParameter(NumberBound.class).isInstantiationOf(v));
    assertTrue(typeParameter(IntegerBound.class).isInstantiationOf(v));
    assertFalse(typeParameter(ComparableIntegerBound.class).isInstantiationOf(v));
    assertFalse(typeParameter(ListStringBound.class).isInstantiationOf(v));
  }

  @Test
  public void testNestedTypeArgument() {
    TypeVariable t = typeParameter(CollectionListSelfBound.class);
    assertFalse(typeParameter(CollectionListStringBound.class).isInstantiationOf(t));
    assertTrue(
        typeParameter(CollectionListStringBound.class)
            .isInstantiationOf(typeParameter(CollectionWildcardBound.class)));
  }

  @Test
  public void testArrayInBound() {
    TypeVariable t = typeParameter(SelfArrayBound.class);
    assertTrue(t.isInstantiationOf(typeParameter(OtherSelfArrayBound.class)));
    assertTrue(t.isInstantiationOf(typeParameter(MapWildcardBound.class)));
    assertFalse(t.isInstantiationOf(typeParameter(ListWildcardBound.class)));
  }

  @Test
  public void testInstantiationOfSiblingTypeParameter() {
    TypeVariable self = typeParameter(AbstractAssert.class, 0);
    assertTrue(typeParameter(AbstractObjectAssert.class, 0).isInstantiationOf(self));
    assertTrue(typeParameter(StringAssertBound.class).isInstantiationOf(self));
    assertFalse(typeParameter(SelfComparableBound.class).isInstantiationOf(self));
  }

  @Test
  public void testSiblingOfSiblingTypeParameter() {
    TypeVariable v = typeParameter(SiblingOfSiblingBound.class, 2);
    // S := RelatedToString and B := String satisfy the bounds.
    assertTrue(typeParameter(RelatedToStringBound.class).isInstantiationOf(v));
    assertFalse(typeParameter(NumberBound.class).isInstantiationOf(v));

    // B := String does not satisfy B's bound Number, but B := Integer does.
    TypeVariable bounded = typeParameter(SiblingOfBoundedSiblingBound.class, 2);
    assertFalse(typeParameter(RelatedToStringBound.class).isInstantiationOf(bounded));
    assertTrue(typeParameter(RelatedToIntegerBound.class).isInstantiationOf(bounded));
  }

  @Test
  public void testWildcardBoundedBySibling() {
    TypeVariable t = typeParameter(ExtendsSiblingBound.class, 1);
    assertTrue(typeParameter(ListExtendsIntegerBound.class).isInstantiationOf(t));
    assertFalse(typeParameter(ListExtendsStringBound.class).isInstantiationOf(t));
  }

  @Test
  public void testSuperWildcardContainment() {
    TypeVariable t = typeParameter(ListSuperIntegerBound.class);
    // List<?> may be a List<String>, which is not a List<? super Integer>.
    assertFalse(typeParameter(ListWildcardBound.class).isInstantiationOf(t));
    assertTrue(typeParameter(ListSuperNumberBound.class).isInstantiationOf(t));
  }

  @Test
  public void testWildcardInSiblingArgument() {
    TypeVariable t = typeParameter(SiblingArgumentBound.class, 1);
    // S may be the capture of ? extends Integer, but not of ? extends String.
    assertTrue(typeParameter(ListExtendsIntegerBound.class).isInstantiationOf(t));
    assertFalse(typeParameter(ListExtendsStringBound.class).isInstantiationOf(t));

    // The capture of ? extends String is a Comparable<String>, not a Comparable of itself.
    TypeVariable selfComparable = typeParameter(SelfComparableSiblingArgumentBound.class, 1);
    assertTrue(typeParameter(ListStringBound.class).isInstantiationOf(selfComparable));
    assertFalse(typeParameter(ListExtendsStringBound.class).isInstantiationOf(selfComparable));
  }

  @Test
  public void testSuperWildcardOfSibling() {
    TypeVariable t = typeParameter(SuperSiblingBound.class, 1);
    // B := Number makes ? super B contain Object.
    assertTrue(typeParameter(ListObjectBound.class).isInstantiationOf(t));
    // No B that is a Number is a subtype of String.
    assertFalse(typeParameter(ListSuperStringBound.class).isInstantiationOf(t));
    // B := Integer makes ? super B contain the capture of ? super Integer.
    assertTrue(typeParameter(ListSuperIntegerBound.class).isInstantiationOf(t));
  }

  @Test
  public void testArrayOfSibling() {
    TypeVariable t = typeParameter(SiblingArrayBound.class, 1);
    assertTrue(typeParameter(ListStringArrayBound.class).isInstantiationOf(t));
    assertFalse(typeParameter(ListNumberArrayBound.class).isInstantiationOf(t));
  }

  @Test
  public void testSiblingIsThisVariable() {
    TypeVariable u = typeParameter(SelfComparableSiblingBound.class, 1);
    // S := SelfComparableBound.T satisfies S's bound Comparable<S>.
    assertTrue(typeParameter(SelfComparableBound.class).isInstantiationOf(u));
    assertFalse(typeParameter(ComparableStringBound.class).isInstantiationOf(u));
    // U is a Comparable<S>, not a Comparable<U>.
    assertFalse(u.isInstantiationOf(typeParameter(SelfComparableBound.class)));
  }

  @Test
  public void testInstantiationOfCopyWithResolvedBound() {
    TypeVariable t = typeParameter(SelfComparableBound.class);
    TypeVariable copy = (TypeVariable) t.substitute(new Substitution(t, t));
    assertTrue(copy.isInstantiationOf(t));
    assertTrue(t.isInstantiationOf(copy));
    assertFalse(typeParameter(ComparableStringBound.class).isInstantiationOf(copy));
  }

  @Test
  public void testInstantiationOfArrayInWildcardInLazyBound() {
    TypeVariable t = typeParameter(ArrayWildcardBound.class);
    assertTrue(t.isInstantiationOf(typeParameter(OtherArrayWildcardBound.class)));
    assertFalse(
        t.canBeInstantiatedBy(
            GenericClassType.forClass(ArrayList.class).instantiate(JavaTypes.OBJECT_TYPE)));
  }

  @Test
  public void testEnclosingTypeParameterStandsForItself() throws NoSuchMethodException {
    TypeVariable t =
        TypeVariable.forType(Outer.class.getDeclaredMethod("m").getTypeParameters()[0]);
    // E is fixed in Outer, so List<String> is not known to be a List<E>.
    assertFalse(typeParameter(ListStringBound.class).isInstantiationOf(t));
  }

  @Test
  public void testPartialSubstitutionIntoNestedSiblingBound() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(NestedSiblingLast.class).getTypeParameters();
    TypeVariable s = parameters.get(0);
    TypeVariable t = parameters.get(1);
    // The bound of t is Map<t, List<s>>.  The substitution instantiates s but not t.
    ParameterBound bound =
        t.getUpperTypeBound().substitute(new Substitution(s, JavaTypes.STRING_TYPE));
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(
                t, GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE)),
        ((ReferenceBound) bound).getBoundType());
  }

  /**
   * A type parameter whose bound contains the type parameter, but not as the last type argument.
   *
   * @param <T> the type parameter
   */
  static class SelfFirstArgumentBound<T extends Map<T, String>> {}

  /**
   * A class with a type parameter.
   *
   * @param <V> the type parameter
   */
  static class Unbounded<V> {}

  /** A field whose type has a wildcard argument for a type parameter with a recursive bound. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  SelfFirstArgumentBound<?> selfFirstArgumentWildcardField;

  @Test
  public void testSubstituteVariableIntoFirstArgument() {
    TypeVariable t = typeParameter(SelfFirstArgumentBound.class, 0);
    TypeVariable v = typeParameter(Unbounded.class, 0);
    ParameterBound bound = t.getUpperTypeBound().substitute(new Substitution(t, v));
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class).instantiate(v, JavaTypes.STRING_TYPE),
        ((ReferenceBound) bound).getBoundType());
  }

  @Test
  public void testSubstituteConcreteTypeIntoFirstArgument() {
    TypeVariable t = typeParameter(SelfFirstArgumentBound.class, 0);
    ParameterBound bound =
        t.getUpperTypeBound().substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertFalse(
        "bound " + bound + " should be eager since it contains no type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(JavaTypes.STRING_TYPE, JavaTypes.STRING_TYPE),
        ((ReferenceBound) bound).getBoundType());
  }

  @Test
  public void testCaptureOfMethodTypeParameter()
      throws NoSuchMethodException, RandoopSpecificationError {
    // <T extends Object & Comparable<? super T>> T min(Collection<? extends T>)
    Method min = Collections.class.getMethod("min", java.util.Collection.class);
    TypedClassOperation operation = TypedOperation.forMethod(min).applyCaptureConversion();
    InstantiatedType parameterType = (InstantiatedType) operation.getInputTypes().get(0);
    TypeVariable capture =
        (TypeVariable)
            ((ReferenceArgument) parameterType.getTypeArguments().get(0)).getReferenceType();
    // The capture variable's upper bound is T.  String can instantiate T, but Comparable<String>
    // cannot, because Comparable<String> is not a Comparable<? super Comparable<String>>.
    assertTrue(capture.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
    InstantiatedType comparableToString =
        JavaTypes.COMPARABLE_TYPE.instantiate(JavaTypes.STRING_TYPE);
    assertFalse(capture.canBeInstantiatedBy(comparableToString));
    assertFalse(
        GenericClassType.forClass(java.util.Collection.class)
            .instantiate(comparableToString)
            .isInstantiationOf(parameterType));
  }

  /**
   * A class whose type parameter's bound contains the type parameter in a wildcard bound.
   *
   * @param <T> the type parameter
   */
  static class WildcardSelfBound<T extends Comparable<? super T>> {}

  /**
   * A class whose type parameter's bound contains the type parameter in a nested type argument.
   *
   * @param <T> the type parameter
   */
  static class NestedSelfBound<T extends Comparable<List<T>>> {}

  /** A field whose type has a wildcard argument for a type parameter with a wildcard self-bound. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  WildcardSelfBound<?> wildcardSelfBoundField;

  /** A field whose type has a wildcard argument for a type parameter with a nested self-bound. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  NestedSelfBound<?> nestedSelfBoundField;

  /** A field whose type has wildcard arguments for a type parameter and its sibling. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  NestedSiblingLast<?, ?> nestedSiblingLastField;

  /**
   * Returns the capture conversion of the type of the given field of this class.
   *
   * @param fieldName the name of a field of this class, whose type has a wildcard argument
   * @return the capture conversion of the field's type
   * @throws NoSuchFieldException if this class has no field named {@code fieldName}
   */
  private static InstantiatedType capturedFieldType(String fieldName) throws NoSuchFieldException {
    InstantiatedType wildcardType =
        (InstantiatedType)
            ClassOrInterfaceType.forType(
                TypeVariableTest.class.getDeclaredField(fieldName).getGenericType());
    return wildcardType.applyCaptureConversion();
  }

  /**
   * Returns the type argument at the given index of the given type.
   *
   * @param type a parameterized type whose type argument at {@code index} is a type variable
   * @param index the index of the type argument
   * @return the type argument of {@code type} at {@code index}
   */
  private static TypeVariable typeArgument(InstantiatedType type, int index) {
    return (TypeVariable)
        ((ReferenceArgument) type.getTypeArguments().get(index)).getReferenceType();
  }

  @Test
  public void testCaptureConversionOfSelfBoundInsideTypeArgument() throws NoSuchFieldException {
    TypeVariable v = typeParameter(Unbounded.class, 0);
    for (String fieldName : Arrays.asList("wildcardSelfBoundField", "nestedSelfBoundField")) {
      InstantiatedType capturedType = capturedFieldType(fieldName);
      TypeVariable capture = typeArgument(capturedType, 0);
      ParameterBound bound = capture.getUpperTypeBound();
      assertTrue(
          "bound " + bound + " should be lazy since it contains a type variable",
          bound instanceof LazyReferenceBound);
      assertEquals(Collections.singletonList(capture), capturedType.getTypeParameters());
      // The substitution does not affect the captured type, whose bound refers to itself.
      assertEquals(
          capturedType, capturedType.substitute(new Substitution(v, JavaTypes.STRING_TYPE)));
    }
  }

  @Test
  public void testSubstituteVariableForCaptureVariable() throws NoSuchFieldException {
    InstantiatedType capturedType = capturedFieldType("selfFirstArgumentWildcardField");
    TypeVariable capture = typeArgument(capturedType, 0);
    TypeVariable v = typeParameter(Unbounded.class, 0);
    InstantiatedType substitutedType = capturedType.substitute(new Substitution(capture, v));
    ParameterBound bound = typeArgument(substitutedType, 0).getUpperTypeBound();
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class).instantiate(v, JavaTypes.STRING_TYPE),
        ((ReferenceBound) bound).getBoundType());
  }

  @Test
  public void testSubstituteIntoSiblingOfCaptureVariable() throws NoSuchFieldException {
    InstantiatedType capturedType = capturedFieldType("nestedSiblingLastField");
    TypeVariable captureS = typeArgument(capturedType, 0);
    TypeVariable captureT = typeArgument(capturedType, 1);
    // The bound of captureT is Map<captureT, List<captureS>>.
    ReferenceType substituted =
        captureT.substitute(new Substitution(captureS, JavaTypes.STRING_TYPE));
    ParameterBound bound = ((TypeVariable) substituted).getUpperTypeBound();
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(
                captureT, GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE)),
        ((ReferenceBound) bound).getBoundType());
  }

  @Test
  public void testInstantiationOfCaptureVariableWithLazyBound() throws NoSuchFieldException {
    InstantiatedType capturedType = capturedFieldType("selfFirstArgumentWildcardField");
    TypeVariable capture = typeArgument(capturedType, 0);
    TypeVariable v = typeParameter(Unbounded.class, 0);
    // The bound of v is Object, which is not within Map<v, String>.
    assertFalse(v.isInstantiationOf(capture));
    // The bound of the other capture variable is Map<otherCapture, String>, which is within
    // Map<otherCapture, String>.
    TypeVariable otherCapture =
        typeArgument(capturedFieldType("selfFirstArgumentWildcardField"), 0);
    assertTrue(otherCapture.isInstantiationOf(capture));
  }

  @Test
  public void testInstantiationOfCaptureVariableByVariableWithReflectiveBound()
      throws NoSuchFieldException {
    InstantiatedType capturedType = capturedFieldType("selfFirstArgumentWildcardField");
    TypeVariable capture = typeArgument(capturedType, 0);
    // The bound of t is Map<t, String>, represented reflectively.  The bound of the capture
    // variable, with t substituted for it, is also Map<t, String>.
    TypeVariable t = typeParameter(SelfFirstArgumentBound.class, 0);
    assertTrue(t.getUpperTypeBound() instanceof LazyParameterBound);
    assertTrue(t.isInstantiationOf(capture));
  }

  /**
   * A class whose type parameter has the intersection bound {@code Number & Runnable}.
   *
   * @param <T> the type parameter
   */
  static class NumberRunnableBound<T extends Number & Runnable> {}

  @Test
  public void testInstantiationByVariableWithIntersectionBound() {
    TypeVariable t = typeParameter(NumberRunnableBound.class, 0);
    TypeVariable u = typeParameter(NumberBound.class, 0);
    assertTrue(t.getUpperTypeBound() instanceof IntersectionTypeBound);
    // Number & Runnable is within the bound Number.
    assertTrue(t.isInstantiationOf(u));
    // Number is not within the bound Number & Runnable.
    assertFalse(u.isInstantiationOf(t));
  }

  /**
   * A generic class that is comparable to itself.
   *
   * @param <U> the type parameter
   */
  abstract static class Version<U> implements Comparable<Version<U>> {}

  /** A field whose type has a wildcard argument for a type parameter with a self-bound. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  SelfComparableBound<?> selfComparableField;

  @Test
  public void testInstantiateSelfBoundWithGenericType() {
    TypeVariable v = typeParameter(Unbounded.class, 0);
    // Version<v> is within the bound Comparable<Version<v>>, which contains a type variable.
    InstantiatedType versionOfV = GenericClassType.forClass(Version.class).instantiate(v);
    InstantiatedType instantiated =
        GenericClassType.forClass(SelfComparableBound.class).instantiate(versionOfV);
    assertEquals(Collections.singletonList(v), instantiated.getTypeParameters());
  }

  @Test
  public void testSubtypeTestOfCaptureVariableAgainstItsBound() throws NoSuchFieldException {
    InstantiatedType capturedType = capturedFieldType("selfComparableField");
    TypeVariable capture = typeArgument(capturedType, 0);
    // The bound of the capture variable is Comparable<capture>.  The general subtype test
    // substitutes Comparable<capture> for capture, so each substitution into the bound nests it
    // more deeply; the test must terminate, and must recognize the bound itself.
    ReferenceType bound = ((ReferenceBound) capture.getUpperTypeBound()).getBoundType();
    assertTrue(
        capture + " should be a subtype of its bound " + bound,
        capture.isSubtypeOfOrEqualTo(bound));
  }

  @Test
  public void testConcreteVariableBoundInContext() {
    TypeVariable w = typeParameter(BoundedVariableBound.class, 0);
    TypeVariable v = typeParameter(BoundedVariableBound.class, 1);
    // With W := Number, V extends W is satisfied by Integer but not by String.
    Substitution context = new Substitution(w, ReferenceType.forClass(Number.class));
    assertTrue(v.canBeInstantiatedBy(JavaTypes.INT_TYPE.toBoxedPrimitive(), context));
    assertFalse(v.canBeInstantiatedBy(JavaTypes.STRING_TYPE, context));
    // With W := Object, V extends W is satisfied by String.
    assertTrue(
        v.canBeInstantiatedBy(JavaTypes.STRING_TYPE, new Substitution(w, JavaTypes.OBJECT_TYPE)));
  }

  /**
   * A type parameter whose self-referential bound contains an array class.
   *
   * @param <T> the type parameter
   */
  static class StringArrayValueBound<T extends Map<T, String[]>> {}

  /** A class that satisfies the bound of {@link StringArrayValueBound}. */
  @SuppressWarnings("serial")
  static class StringArrayValueMap extends HashMap<StringArrayValueMap, String[]> {}

  @Test
  public void testArrayClassInSelfReferentialLazyBound() {
    TypeVariable t = typeParameter(StringArrayValueBound.class, 0);
    assertTrue(t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(StringArrayValueMap.class)));
    assertFalse(t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(SelfArrayMap.class)));
  }

  @Test
  public void testSubtypeTestOfExplicitVariableAgainstItsBound() {
    TypeVariable t = typeParameter(SelfComparableBound.class, 0);
    // The bound of t is a LazyParameterBound for Comparable<t>, which refers to t.
    assertTrue(t.getUpperTypeBound() instanceof LazyParameterBound);
    InstantiatedType bound = GenericClassType.forClass(Comparable.class).instantiate(t);
    assertTrue(t + " should be a subtype of its bound " + bound, t.isSubtypeOfOrEqualTo(bound));
  }

  @Test
  public void testTypeParametersAfterSubstituteIntoSiblingOfCaptureVariable()
      throws NoSuchFieldException {
    InstantiatedType capturedType = capturedFieldType("nestedSiblingLastField");
    TypeVariable captureS = typeArgument(capturedType, 0);
    TypeVariable captureT = typeArgument(capturedType, 1);
    // The bound of captureT is Map<captureT, List<captureS>>.
    ReferenceType substituted =
        captureT.substitute(new Substitution(captureS, JavaTypes.STRING_TYPE));
    assertEquals(Collections.singletonList(substituted), substituted.getTypeParameters());
  }

  @Test
  public void testSubstituteVariableIntoNestedArgument() {
    TypeVariable t = typeParameter(SelfNestedArgumentBound.class, 0);
    TypeVariable v = typeParameter(Unbounded.class, 0);
    ParameterBound bound = t.getUpperTypeBound().substitute(new Substitution(t, v));
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(
                GenericClassType.forClass(List.class).instantiate(v), JavaTypes.STRING_TYPE),
        ((ReferenceBound) bound).getBoundType());
    assertEquals(Collections.singletonList(v), bound.getTypeParameters());
  }

  @Test
  public void testSubstituteVariableIntoWildcardBound() {
    TypeVariable t = typeParameter(WildcardSelfBound.class, 0);
    TypeVariable v = typeParameter(Unbounded.class, 0);
    ParameterBound bound = t.getUpperTypeBound().substitute(new Substitution(t, v));
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(Collections.singletonList(v), bound.getTypeParameters());
    // The bound is Comparable<? super V>.
    InstantiatedType boundType = (InstantiatedType) ((ReferenceBound) bound).getBoundType();
    assertEquals(JavaTypes.COMPARABLE_TYPE, boundType.getGenericClassType());
    assertEquals(1, boundType.getTypeArguments().size());
    TypeArgument argument = boundType.getTypeArguments().get(0);
    assertTrue(argument + " should be a wildcard", argument instanceof WildcardArgument);
    WildcardArgument wildcard = (WildcardArgument) argument;
    assertFalse(wildcard + " should have a lower bound", wildcard.hasUpperBound());
    ParameterBound wildcardBound = wildcard.getTypeBound();
    assertTrue(wildcardBound instanceof ReferenceBound);
    assertEquals(v, ((ReferenceBound) wildcardBound).getBoundType());
  }

  @Test
  public void testSubstituteTypeContainingVariableIntoFirstArgument() {
    TypeVariable t = typeParameter(SelfFirstArgumentBound.class, 0);
    TypeVariable v = typeParameter(Unbounded.class, 0);
    InstantiatedType listOfV = GenericClassType.forClass(List.class).instantiate(v);
    ParameterBound bound = t.getUpperTypeBound().substitute(new Substitution(t, listOfV));
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class).instantiate(listOfV, JavaTypes.STRING_TYPE),
        ((ReferenceBound) bound).getBoundType());
    assertEquals(Collections.singletonList(v), bound.getTypeParameters());
  }

  /**
   * A class whose type parameter's bound contains the type parameter nested in a type argument.
   *
   * @param <T> the type parameter
   */
  static class SelfNestedArgumentBound<T extends Map<List<T>, String>> {}

  @Test
  public void testCaptureConversionOfFirstArgumentBound() throws NoSuchFieldException {
    InstantiatedType capturedType = capturedFieldType("selfFirstArgumentWildcardField");
    TypeVariable capture = typeArgument(capturedType, 0);
    // The capture variable's bound is Map<capture, String>, which refers to the capture variable.
    ParameterBound bound = capture.getUpperTypeBound();
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class).instantiate(capture, JavaTypes.STRING_TYPE),
        ((ReferenceBound) bound).getBoundType());
    assertEquals(Collections.singletonList(capture), capturedType.getTypeParameters());
  }

  /** A class that is a subtype of both {@code Number} and {@code Runnable}. */
  @SuppressWarnings("serial") // never instantiated
  abstract static class RunnableNumber extends Number implements Runnable {}

  @Test
  public void testLowerBoundOfIntersection() {
    ParameterBound intersection = typeParameter(IntersectionBound.class, 0).getUpperTypeBound();
    assertTrue(intersection instanceof IntersectionTypeBound);
    Substitution substitution = new Substitution();
    ReferenceBound runnableNumber =
        new EagerReferenceBound(ClassOrInterfaceType.forClass(RunnableNumber.class));
    assertTrue(runnableNumber.isLowerBound(intersection, substitution));
    // Integer is a Number but not a Runnable.
    ReferenceBound integer = new EagerReferenceBound(JavaTypes.INT_TYPE.toBoxedPrimitive());
    assertFalse(integer.isLowerBound(intersection, substitution));
  }

  /**
   * A class whose type parameter's bound is a class that refers to the type parameter.
   *
   * @param <T> the type parameter
   */
  static class SelfArrayListBound<T extends ArrayList<T>> {}

  @Test
  public void testSubtypeOfSupertypeOfSelfReferentialBound() {
    TypeVariable t = typeParameter(SelfArrayListBound.class, 0);
    // T is a subtype of its bound ArrayList<T>, which is a subtype of List<T>.
    InstantiatedType listOfT = GenericClassType.forClass(List.class).instantiate(t);
    assertTrue(t + " should be a subtype of " + listOfT, t.isSubtypeOfOrEqualTo(listOfT));
  }

  @Test
  public void testLazyBoundWithNestedArrayOfNullType() {
    TypeVariable t = typeParameter(SelfComparableBound.class, 0);
    InstantiatedType listOfArray =
        GenericClassType.forClass(List.class).instantiate(ArrayType.ofComponentType(t));
    LazyReferenceBound bound = new LazyReferenceBound(listOfArray);
    // There is no array of the null type, so the bound List<T[]> is left unresolved.
    assertSame(bound, bound.substitute(new Substitution(t, JavaTypes.NULL_TYPE)));
  }

  /** A class that is comparable to itself. */
  abstract static class ComparableBase implements Comparable<ComparableBase> {}

  /** A subclass of a class that is comparable to itself. */
  abstract static class ComparableSub extends ComparableBase {}

  @Test
  public void testLazyUpperBoundOfUnknownVariableTriesSupertypes() {
    // The unknown T, with T extends Comparable<T>, can be ComparableBase, a supertype of
    // ComparableSub, though it cannot be ComparableSub.
    LazyReferenceBound bound = new LazyReferenceBound(typeParameter(SelfComparableBound.class, 0));
    assertTrue(
        bound.isUpperBound(ClassOrInterfaceType.forClass(ComparableSub.class), new Substitution()));
  }

  @Test
  public void testLazyParameterBoundHasUpperBoundOfItsType() {
    TypeVariable t = typeParameter(SelfComparableBound.class, 0);
    ParameterBound lazyBound = t.getUpperTypeBound();
    assertTrue(lazyBound instanceof LazyParameterBound);
    ReferenceType comparableOfT = GenericClassType.forClass(Comparable.class).instantiate(t);
    // Both bounds are Comparable<T>.
    assertTrue(new EagerReferenceBound(comparableOfT).isUpperBound(lazyBound, new Substitution()));
    assertFalse(
        new EagerReferenceBound(JavaTypes.STRING_TYPE).isUpperBound(lazyBound, new Substitution()));
  }

  /**
   * A class with a field whose type has a wildcard bounded below by the type parameter.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  static class SuperOfParameter<T> {
    /** A wildcard type whose lower bound is the type parameter. */
    Comparable<? super T> superOfT;
  }

  @Test
  public void testVariableIsSubtypeOfCaptureWithItAsLowerBound() throws NoSuchFieldException {
    TypeVariable t = typeParameter(SuperOfParameter.class, 0);
    Field field = SuperOfParameter.class.getDeclaredField("superOfT");
    InstantiatedType captured =
        ((InstantiatedType) Type.forType(field.getGenericType())).applyCaptureConversion();
    ReferenceType capture =
        ((ReferenceArgument) captured.getTypeArguments().get(0)).getReferenceType();
    // T is the lower bound of the capture of `? super T`.
    assertTrue(Subtyping.isSubtypeOrEqual(t, capture, new Substitution()));
    assertFalse(Subtyping.isSubtypeOrEqual(JavaTypes.OBJECT_TYPE, capture, new Substitution()));
  }
}
