package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
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
   * Returns the type parameter at the given index of the given generic class.
   *
   * @param c a generic class
   * @param index the index of the type parameter
   * @return the type parameter of {@code c} at {@code index}
   */
  private static TypeVariable typeParameter(Class<?> c, int index) {
    return GenericClassType.forClass(c).getTypeParameters().get(index);
  }

  /**
   * Mutually recursive type parameters that {@code String} does not satisfy.
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

  /** Fields whose types are used in tests. */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  static class FieldTypes {
    /** A wildcard type whose type parameter has a self-referential bound. */
    SelfComparableBound<?> selfComparableWildcard;
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

  @Test
  public void testVariableBoundInstantiatedInContext() {
    TypeVariable k = typeParameter(PartiallySubstitutedRecursiveBound.class, 0);
    TypeVariable v = typeParameter(PartiallySubstitutedRecursiveBound.class, 1);
    TypeVariable w = typeParameter(PartiallySubstitutedRecursiveBound.class, 2);
    ReferenceType integerKeyed = ClassOrInterfaceType.forClass(IntegerKeyedMapOfSelf.class);
    ReferenceType stringKeyed = ClassOrInterfaceType.forClass(StringKeyedMapOfSelf.class);
    // With V := StringKeyedMapOfSelf, W can be StringKeyedMapOfSelf but not IntegerKeyedMapOfSelf.
    Substitution context =
        new Substitution(k, JavaTypes.STRING_TYPE).extend(new Substitution(v, stringKeyed));
    assertTrue(w.canBeInstantiatedBy(stringKeyed, context));
    assertFalse(w.canBeInstantiatedBy(integerKeyed, context));
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

  /** Mapping a capture variable to itself keeps a copy of it and the copy's bounds. */
  @Test
  public void testIdentitySubstitutionKeepsCaptureVariableCopy() {
    TypeVariable capture = captureVariable("selfComparableWildcard");
    TypeVariable copy =
        capture.createCopyWithBounds(
            capture.getLowerTypeBound(), new EagerReferenceBound(JavaTypes.OBJECT_TYPE));
    assertSame(copy, copy.substitute(new Substitution(capture, capture)));
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

  /**
   * Returns the type that is the upper bound of the given type variable.
   *
   * @param variable a type variable whose upper bound is a {@link ReferenceBound}
   * @return the type of the upper bound of {@code variable}
   */
  private static ReferenceType upperBoundType(ReferenceType variable) {
    return ((ReferenceBound) ((TypeVariable) variable).getUpperTypeBound()).getBoundType();
  }

  /** Substitution into an array type changes the bound of its component type variable. */
  @Test
  public void testSubstitutionChangesBoundOfArrayComponent() {
    TypeVariable x = typeParameter(ListBound.class, 0);
    TypeVariable y = typeParameter(ListBound.class, 1);
    ArrayType result =
        ArrayType.ofComponentType(y).substitute(new Substitution(x, JavaTypes.STRING_TYPE));
    InstantiatedType listOfString =
        GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE);
    assertEquals(listOfString, upperBoundType((ReferenceType) result.getComponentType()));
  }

  /** Substitution into a wildcard type changes the bound of the type variable in its bound. */
  @Test
  public void testSubstitutionChangesBoundOfWildcardBound() {
    TypeVariable x = typeParameter(ListBound.class, 0);
    TypeVariable y = typeParameter(ListBound.class, 1);
    WildcardType wildcard = new WildcardType(new EagerReferenceBound(y), true);
    WildcardType result = wildcard.substitute(new Substitution(x, JavaTypes.STRING_TYPE));
    InstantiatedType listOfString =
        GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE);
    assertEquals(
        listOfString, upperBoundType(((ReferenceBound) result.getTypeBound()).getBoundType()));
  }

  /** Substitution into a capture variable changes the bound of the type variable in its bound. */
  @Test
  public void testSubstitutionChangesBoundOfCaptureVariableBound() {
    TypeVariable x = typeParameter(ListBound.class, 0);
    TypeVariable y = typeParameter(ListBound.class, 1);
    CaptureTypeVariable capture =
        new CaptureTypeVariable(
            new WildcardArgument(new WildcardType(new EagerReferenceBound(y), true)));
    ReferenceType result = capture.substitute(new Substitution(x, JavaTypes.STRING_TYPE));
    InstantiatedType listOfString =
        GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE);
    assertEquals(listOfString, upperBoundType(upperBoundType(result)));
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
   * A class with a generic inner class whose type parameter is bounded by the outer class's.
   *
   * @param <E> the outer class's type parameter
   */
  static class Outer<E> {
    /**
     * A generic inner class.
     *
     * @param <T> a type parameter bounded by {@code E}
     */
    class Inner<T extends E> {}
  }

  @Test
  public void testSubstituteCopyForOriginal() {
    TypeVariable e = typeParameter(Outer.class, 0);
    TypeVariable t = typeParameter(Outer.Inner.class, 0);
    TypeVariable copy = (TypeVariable) t.substitute(new Substitution(e, JavaTypes.STRING_TYPE));
    // Substituting a copy with other bounds yields that copy.
    TypeVariable result = (TypeVariable) t.substitute(new Substitution(t, copy));
    assertEquals(copy.getUpperTypeBound(), result.getUpperTypeBound());
  }

  /** Substituting a copy with other bounds for a variable whose bound changes yields that copy. */
  @Test
  public void testSubstituteCopyForVariableWithChangedBound() {
    TypeVariable t = typeParameter(SelfComparableBound.class, 0);
    TypeVariable copy =
        t.createCopyWithBounds(
            t.getLowerTypeBound(), new EagerReferenceBound(JavaTypes.OBJECT_TYPE));
    assertSame(copy, t.substitute(new Substitution(t, copy)));
  }

  /** Substituting a copy with other bounds for a capture variable yields that copy. */
  @Test
  public void testSubstituteCopyForCaptureVariableWithChangedBound() {
    TypeVariable x = typeParameter(ListBound.class, 0);
    TypeVariable y = typeParameter(ListBound.class, 1);
    CaptureTypeVariable capture =
        new CaptureTypeVariable(
            new WildcardArgument(new WildcardType(new EagerReferenceBound(y), true)));
    TypeVariable copy =
        capture.createCopyWithBounds(
            capture.getLowerTypeBound(), new EagerReferenceBound(JavaTypes.OBJECT_TYPE));
    // The substitution changes the bound of the capture variable, because it maps X.
    Substitution substitution =
        new Substitution(Arrays.asList(capture, x), copy, JavaTypes.STRING_TYPE);
    assertSame(copy, capture.substitute(substitution));
  }

  /** A substitution that maps two copies of a type variable to themselves maps it to itself. */
  @Test
  public void testIdentitySubstitutionOfTwoCopies() {
    java.lang.reflect.Type reflectionVariable = SelfComparableBound.class.getTypeParameters()[0];
    TypeVariable t1 = TypeVariable.forType(reflectionVariable);
    TypeVariable t2 = t1.createCopyWithBounds(t1.getLowerTypeBound(), t1.getUpperTypeBound());
    assertNotSame(t1, t2);
    assertEquals(t1, t2);
    Substitution substitution = new Substitution(Arrays.asList(t1, t2), t1, t2);
    assertTrue(substitution.mapsToItself(t1));
    assertTrue(substitution.mapsToItself(t2));
  }

  /**
   * A class whose type parameter T has a bound with a sibling in a nested type argument, before T.
   *
   * @param <S> a type parameter
   * @param <T> a type parameter bounded by {@code Map<List<S>, T>}
   */
  static class NestedSiblingFirst<S, T extends Map<List<S>, T>> {}

  /**
   * A class whose type parameter T has a bound with a sibling in a nested type argument, after T.
   *
   * @param <S> a type parameter
   * @param <T> a type parameter bounded by {@code Map<T, List<S>>}
   */
  static class NestedSiblingLast<S, T extends Map<T, List<S>>> {}

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
   * A class whose type parameter's bound contains the type parameter, but not as the last type
   * argument.
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
   * A class whose type parameter's bound contains the type parameter as an upper wildcard bound.
   *
   * @param <T> the type parameter
   */
  static class ExtendsWildcardSelfBound<T extends Comparable<? extends T>> {}

  /** Mapping a type parameter that occurs in an upper wildcard bound of its bound terminates. */
  @Test
  public void testIdentitySubstitutionOfExtendsWildcardSelfBound() {
    TypeVariable t = typeParameter(ExtendsWildcardSelfBound.class, 0);
    ReferenceType result = t.substitute(new Substitution(t, t));
    assertEquals(t, result);
  }

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
   * A class whose type parameter has an intersection bound.
   *
   * @param <T> the type parameter
   */
  static class IntersectionBound<T extends Number & Runnable> {}

  /**
   * A class whose type parameter is bounded by {@code Number}.
   *
   * @param <U> the type parameter
   */
  static class NumberBound<U extends Number> {}

  @Test
  public void testInstantiationByVariableWithIntersectionBound() {
    TypeVariable t = typeParameter(IntersectionBound.class, 0);
    TypeVariable u = typeParameter(NumberBound.class, 0);
    assertTrue(t.getUpperTypeBound() instanceof IntersectionTypeBound);
    // Number & Runnable is within the bound Number.
    assertTrue(t.isInstantiationOf(u));
    // Number is not within the bound Number & Runnable.
    assertFalse(u.isInstantiationOf(t));
  }

  /**
   * A class whose type parameter's bound is the type parameter's own interface.
   *
   * @param <T> the type parameter
   */
  static class SelfComparable<T extends Comparable<T>> {}

  /**
   * A generic class that is comparable to itself.
   *
   * @param <U> the type parameter
   */
  @SuppressWarnings("PMD.OverrideBothEqualsAndHashCodeOnComparable") // test input
  static class Version<U> implements Comparable<Version<U>> {
    @Override
    public int compareTo(Version<U> other) {
      return 0;
    }
  }

  /** A field whose type has a wildcard argument for a type parameter with a self-bound. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  SelfComparable<?> selfComparableField;

  @Test
  public void testInstantiateSelfBoundWithGenericType() {
    TypeVariable v = typeParameter(Unbounded.class, 0);
    // Version<v> is within the bound Comparable<Version<v>>, which contains a type variable.
    InstantiatedType versionOfV = GenericClassType.forClass(Version.class).instantiate(v);
    InstantiatedType instantiated =
        GenericClassType.forClass(SelfComparable.class).instantiate(versionOfV);
    assertEquals(Collections.singletonList(v), instantiated.getTypeParameters());
  }

  @Test
  public void testSubtypeTestOfCaptureVariableAgainstItsBound() throws NoSuchFieldException {
    InstantiatedType capturedType = capturedFieldType("selfComparableField");
    TypeVariable capture = typeArgument(capturedType, 0);
    // The bound of the capture variable is Comparable<capture>, which refers to the capture
    // variable.
    ReferenceType bound = ((ReferenceBound) capture.getUpperTypeBound()).getBoundType();
    assertTrue(
        capture + " should be a subtype of its bound " + bound,
        capture.isSubtypeOfOrEqualTo(bound));
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

  /**
   * A class whose type parameter's bound contains the type parameter nested in a type argument.
   *
   * @param <T> the type parameter
   */
  static class SelfNestedArgumentBound<T extends Map<List<T>, String>> {}

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

  /**
   * A class whose type parameter T is bounded by its sibling S.
   *
   * @param <S> a type parameter
   * @param <T> a type parameter bounded by {@code S}
   */
  static class SiblingBound<S, T extends S> {}

  @Test
  public void testSubtypeOfVariableBoundedBySibling() {
    TypeVariable s = typeParameter(SiblingBound.class, 0);
    TypeVariable t = typeParameter(SiblingBound.class, 1);
    assertTrue(t.isSubtypeOfOrEqualTo(s));
    assertTrue(t.isSubtypeOfOrEqualTo(JavaTypes.OBJECT_TYPE));
    // S need not be a subtype of String.
    assertFalse(t.isSubtypeOfOrEqualTo(JavaTypes.STRING_TYPE));
  }

  /** A field whose type has wildcard arguments for a type parameter and its sibling bound. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  SiblingBound<?, ?> siblingBoundField;

  @Test
  public void testSubtypeOfCaptureVariableBoundedBySibling() throws NoSuchFieldException {
    InstantiatedType capturedType = capturedFieldType("siblingBoundField");
    TypeVariable captureS = typeArgument(capturedType, 0);
    TypeVariable captureT = typeArgument(capturedType, 1);
    assertTrue(captureT.isSubtypeOfOrEqualTo(captureS));
    // The capture variable of S need not be a subtype of String.
    assertFalse(captureT.isSubtypeOfOrEqualTo(JavaTypes.STRING_TYPE));
  }

  @Test
  public void testSubtypeOfMemberOfIntersectionBound() {
    TypeVariable t = typeParameter(IntersectionBound.class, 0);
    // The bound of T is Number & Runnable.
    assertTrue(t.isSubtypeOfOrEqualTo(ClassOrInterfaceType.forClass(Number.class)));
    assertTrue(t.isSubtypeOfOrEqualTo(ClassOrInterfaceType.forClass(Runnable.class)));
    assertTrue(t.isSubtypeOfOrEqualTo(JavaTypes.OBJECT_TYPE));
    assertFalse(t.isSubtypeOfOrEqualTo(JavaTypes.STRING_TYPE));
  }

  /**
   * A class whose type parameter is bounded by a sibling that is bounded by {@code Integer}.
   *
   * @param <S> a type parameter bounded by {@code Integer}
   * @param <T> a type parameter bounded by {@code S}
   */
  static class IntegerSiblingBound<S extends Integer, T extends S> {}

  @Test
  public void testSubtypeOfBoundOfSibling() {
    TypeVariable t = typeParameter(IntegerSiblingBound.class, 1);
    assertTrue(t.isSubtypeOfOrEqualTo(ClassOrInterfaceType.forClass(Number.class)));
    assertFalse(t.isSubtypeOfOrEqualTo(JavaTypes.STRING_TYPE));
  }

  @Test
  public void testSubtypeOfSelfReferentialBound() {
    TypeVariable t = typeParameter(SelfComparable.class, 0);
    GenericClassType comparable = GenericClassType.forClass(Comparable.class);
    // The bound of T is Comparable<T>.
    assertTrue(t.isSubtypeOfOrEqualTo(comparable.instantiate(t)));
    assertFalse(t.isSubtypeOfOrEqualTo(comparable.instantiate(JavaTypes.STRING_TYPE)));
    assertFalse(t.isSubtypeOfOrEqualTo(ClassOrInterfaceType.forClass(Number.class)));
  }

  /**
   * A class whose type parameter is bounded like that of {@link Enum}.
   *
   * @param <E> a type parameter bounded by {@code Enum<E>}
   */
  static class EnumBound<E extends Enum<E>> {}

  @Test
  public void testSubtypeOfSupertypeOfEnumBound() {
    TypeVariable e = typeParameter(EnumBound.class, 0);
    // The bound of E is Enum<E>, which implements Serializable.
    ReferenceType enumOfE =
        ((LazyParameterBound) e.getUpperTypeBound()).toReferenceBound().getBoundType();
    assertTrue(e.isSubtypeOfOrEqualTo(enumOfE));
    assertTrue(e.isSubtypeOfOrEqualTo(ClassOrInterfaceType.forClass(java.io.Serializable.class)));
    assertFalse(
        e.isSubtypeOfOrEqualTo(
            GenericClassType.forClass(Comparable.class).instantiate(JavaTypes.STRING_TYPE)));
  }

  @Test
  public void testSubtypeOfWildcardSelfBound() {
    TypeVariable t = typeParameter(WildcardSelfBound.class, 0);
    // The bound of T is Comparable<? super T>, which is not a subtype of Comparable<T>.
    assertFalse(t.isSubtypeOfOrEqualTo(GenericClassType.forClass(Comparable.class).instantiate(t)));
  }

  /**
   * A class whose type parameter is bounded by {@code List<String>}.
   *
   * @param <T> a type parameter bounded by {@code List<String>}
   */
  static class StringListBound<T extends List<String>> {}

  /** A field of type {@code List<?>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<?> unboundedListField;

  /** A field of type {@code List<? extends Number>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<? extends Number> numberListField;

  /** A field of type {@code List<? extends CharSequence>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<? extends CharSequence> charSequenceListField;

  /** A field of type {@code List<? super String>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<? super String> superStringListField;

  /** A field of type {@code List<? super Integer>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<? super Integer> superIntegerListField;

  /** A field of type {@code Collection<?>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  Collection<?> unboundedCollectionField;

  /** A field of type {@code Collection<? extends Number>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  Collection<? extends Number> numberCollectionField;

  /**
   * Returns the type of the given field of this class.
   *
   * @param fieldName a field name of this class
   * @return the type of the field
   * @throws NoSuchFieldException if this class has no field named {@code fieldName}
   */
  private static ClassOrInterfaceType ownFieldType(String fieldName) throws NoSuchFieldException {
    return ClassOrInterfaceType.forType(
        TypeVariableTest.class.getDeclaredField(fieldName).getGenericType());
  }

  @Test
  public void testSubtypeOfWildcardInstantiation() throws NoSuchFieldException {
    InstantiatedType listOfString =
        GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE);
    assertTrue(listOfString.isSubtypeOfOrEqualTo(ownFieldType("unboundedListField")));
    assertTrue(listOfString.isSubtypeOfOrEqualTo(ownFieldType("charSequenceListField")));
    assertTrue(listOfString.isSubtypeOfOrEqualTo(ownFieldType("superStringListField")));
    assertFalse(listOfString.isSubtypeOfOrEqualTo(ownFieldType("numberListField")));
    assertFalse(listOfString.isSubtypeOfOrEqualTo(ownFieldType("superIntegerListField")));
    // A wildcard argument is not contained by a non-wildcard argument.
    assertFalse(ownFieldType("charSequenceListField").isSubtypeOfOrEqualTo(listOfString));

    // A supertype with another rawtype is compared by containment of its type arguments.
    assertTrue(listOfString.isSubtypeOfOrEqualTo(ownFieldType("unboundedCollectionField")));
    assertFalse(listOfString.isSubtypeOfOrEqualTo(ownFieldType("numberCollectionField")));

    TypeVariable t = typeParameter(StringListBound.class, 0);
    assertTrue(t.isSubtypeOfOrEqualTo(ownFieldType("unboundedListField")));
    assertFalse(t.isSubtypeOfOrEqualTo(ownFieldType("numberListField")));
    assertTrue(t.isSubtypeOfOrEqualTo(ownFieldType("unboundedCollectionField")));
    assertFalse(t.isSubtypeOfOrEqualTo(ownFieldType("numberCollectionField")));
  }

  /** A field of type {@code List<List<?>>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<List<?>> unboundedListListField;

  /** A field of type {@code List<List<? super Integer>>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<List<? super Integer>> superIntegerListListField;

  /** A field of type {@code List<? extends List<? super Integer>>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<? extends List<? super Integer>> extendsSuperIntegerListListField;

  /** A field of type {@code List<? extends List<?>>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<? extends List<?>> extendsUnboundedListListField;

  /**
   * Tests that a wildcard nested in the bound of a wildcard type argument is compared by
   * containment (JLS section 4.5.1): {@code ? super Integer} does not contain {@code ?}, but {@code
   * ?} contains {@code ? super Integer}.
   */
  @Test
  public void testSubtypeOfNestedWildcard() throws NoSuchFieldException {
    ClassOrInterfaceType unboundedListList = ownFieldType("unboundedListListField");
    ClassOrInterfaceType superIntegerListList = ownFieldType("superIntegerListListField");
    ClassOrInterfaceType extendsSuperIntegerListList =
        ownFieldType("extendsSuperIntegerListListField");
    ClassOrInterfaceType extendsUnboundedListList = ownFieldType("extendsUnboundedListListField");

    assertFalse(unboundedListList.isSubtypeOfOrEqualTo(extendsSuperIntegerListList));
    assertTrue(superIntegerListList.isSubtypeOfOrEqualTo(extendsUnboundedListList));
    assertTrue(superIntegerListList.isSubtypeOfOrEqualTo(extendsSuperIntegerListList));
    assertTrue(unboundedListList.isSubtypeOfOrEqualTo(extendsUnboundedListList));
  }

  /**
   * Fields whose types have nested wildcards with a bound that mentions a type variable with a
   * recursive bound. Such a wildcard bound is a {@link LazyParameterBound}.
   *
   * @param <T> a type parameter with a self-referential bound
   */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  static class NestedLazyWildcardBounds<T extends Comparable<T>> {
    /** A list whose elements are lists with a wildcard element type. */
    List<List<? extends Comparable<T>>> source;

    /** A list with a wildcard element type whose bound has a wildcard. */
    List<? extends List<? extends Comparable<T>>> target;
  }

  /**
   * Comparing nested wildcards whose bounds are lazy compares the bounds rather than throwing a
   * {@link LazyParameterBound.LazyBoundException}.
   */
  @Test
  public void testSubtypeOfNestedLazyWildcardBound() throws NoSuchFieldException {
    ClassOrInterfaceType source =
        ClassOrInterfaceType.forType(
            NestedLazyWildcardBounds.class.getDeclaredField("source").getGenericType());
    ClassOrInterfaceType target =
        ClassOrInterfaceType.forType(
            NestedLazyWildcardBounds.class.getDeclaredField("target").getGenericType());
    assertTrue(source.isSubtypeOfOrEqualTo(target));
  }

  /**
   * A class whose type parameters have bounds that refer to each other.
   *
   * @param <T> a type parameter bounded by {@code Comparable<U>}
   * @param <U> a type parameter bounded by {@code Comparable<T>}
   */
  static class MutuallyRecursiveBounds<T extends Comparable<U>, U extends Comparable<T>> {}

  @Test
  public void testSubtypeOfBoundThatMentionsSibling() {
    TypeVariable t = typeParameter(MutuallyRecursiveBounds.class, 0);
    TypeVariable u = typeParameter(MutuallyRecursiveBounds.class, 1);
    GenericClassType comparable = GenericClassType.forClass(Comparable.class);
    // The bound of T is Comparable<U>.
    assertTrue(t.isSubtypeOfOrEqualTo(comparable.instantiate(u)));
    assertFalse(t.isSubtypeOfOrEqualTo(comparable.instantiate(t)));
    assertFalse(t.isSubtypeOfOrEqualTo(ClassOrInterfaceType.forClass(Number.class)));
  }

  /**
   * A generic interface that extends {@code Runnable}.
   *
   * @param <X> the type parameter
   */
  interface RunnableLeft<X> extends Runnable {}

  /**
   * A generic interface that extends {@code Runnable}.
   *
   * @param <X> the type parameter
   */
  interface RunnableRight<X> extends Runnable {}

  /**
   * A class whose type parameter has an intersection bound whose members refer to the type
   * parameter.
   *
   * @param <T> a type parameter bounded by {@code RunnableLeft<T> & RunnableRight<T>}
   */
  static class SelfReferentialIntersectionBound<T extends RunnableLeft<T> & RunnableRight<T>> {}

  @Test
  public void testSubtypeOfMemberOfSelfReferentialIntersectionBound() {
    TypeVariable t = typeParameter(SelfReferentialIntersectionBound.class, 0);
    assertTrue(
        t.isSubtypeOfOrEqualTo(GenericClassType.forClass(RunnableLeft.class).instantiate(t)));
    assertTrue(
        t.isSubtypeOfOrEqualTo(GenericClassType.forClass(RunnableRight.class).instantiate(t)));
    assertTrue(t.isSubtypeOfOrEqualTo(ClassOrInterfaceType.forClass(Runnable.class)));
    assertFalse(t.isSubtypeOfOrEqualTo(ClassOrInterfaceType.forClass(Number.class)));
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

  /** A field of type {@code List<? extends List<String>[]>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<? extends List<String>[]> extendsStringListArrayListField;

  /** A field of type {@code List<? super List<String>[]>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<? super List<String>[]> superStringListArrayListField;

  /**
   * Returns the first type argument of the type of the given field of this class.
   *
   * @param fieldName a field name of this class, for a field whose type is parameterized
   * @return the first type argument of the type of the field
   * @throws NoSuchFieldException if this class has no field named {@code fieldName}
   */
  private static TypeArgument firstTypeArgument(String fieldName) throws NoSuchFieldException {
    return ((InstantiatedType) ownFieldType(fieldName)).getTypeArguments().get(0);
  }

  @Test
  public void testWildcardWithVariableFreeGenericArrayBound() throws NoSuchFieldException {
    ArrayType stringListArray =
        ArrayType.ofComponentType(
            GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE));
    TypeArgument argument = TypeArgument.forType(stringListArray);
    assertTrue(firstTypeArgument("extendsStringListArrayListField").contains(argument));
    assertTrue(firstTypeArgument("superStringListArrayListField").contains(argument));
    TypeArgument stringArray =
        TypeArgument.forType(ArrayType.ofComponentType(JavaTypes.STRING_TYPE));
    assertFalse(firstTypeArgument("extendsStringListArrayListField").contains(stringArray));
    assertFalse(firstTypeArgument("superStringListArrayListField").contains(stringArray));
  }

  /** A field of type {@code List<? extends Collection<?>>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<? extends Collection<?>> collectionListField;

  /** A field of type {@code List<? extends Collection<? extends CharSequence>>}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  List<? extends Collection<? extends CharSequence>> charSequenceCollectionListField;

  @Test
  public void testSubtypeOfWildcardWithInheritedSupertype() throws NoSuchFieldException {
    GenericClassType list = GenericClassType.forClass(List.class);
    InstantiatedType listOfListOfString = list.instantiate(list.instantiate(JavaTypes.STRING_TYPE));
    InstantiatedType listOfListOfInteger =
        list.instantiate(list.instantiate(ClassOrInterfaceType.forClass(Integer.class)));
    assertTrue(listOfListOfString.isSubtypeOfOrEqualTo(ownFieldType("collectionListField")));
    assertTrue(
        listOfListOfString.isSubtypeOfOrEqualTo(ownFieldType("charSequenceCollectionListField")));
    assertTrue(listOfListOfInteger.isSubtypeOfOrEqualTo(ownFieldType("collectionListField")));
    assertFalse(
        listOfListOfInteger.isSubtypeOfOrEqualTo(ownFieldType("charSequenceCollectionListField")));
    // The inherited supertype Collection<String> of List<String> is compared with Collection<?> by
    // containment, not equality.
    ReferenceType collectionOfWildcard =
        ((ReferenceBound)
                ((WildcardArgument) firstTypeArgument("collectionListField")).getTypeBound())
            .getBoundType();
    assertTrue(list.instantiate(JavaTypes.STRING_TYPE).isSubtypeOfOrEqualTo(collectionOfWildcard));
  }

  /**
   * Type parameters whose bounds form a cycle through a parameterized type: {@code V}'s bound
   * mentions {@code W} and {@code A}, and {@code A extends W extends V}.
   *
   * @param <V> a type parameter whose bound refers to {@code W} and {@code A}
   * @param <W> a type parameter bounded by {@code V}
   * @param <A> a type parameter bounded by {@code W}
   */
  static class ChainedVariableBounds<V extends Pair<W, A>, W extends V, A extends W> {}

  /** A class that is a pair of itself and itself, and so can instantiate {@code V}. */
  abstract static class PairOfSelf implements Pair<PairOfSelf, PairOfSelf> {}

  /**
   * A class that cannot instantiate {@code V} in {@link ChainedVariableBounds}: that would need
   * {@code W := PairOfSelfAndOther} and {@code A := PairOfSelf}, but {@code A extends W} does not
   * hold, since {@link PairOfSelf} is not a subtype of {@link PairOfSelfAndOther}.
   */
  abstract static class PairOfSelfAndOther implements Pair<PairOfSelfAndOther, PairOfSelf> {}

  /**
   * Checking {@code V := PairOfSelfAndOther} substitutes it into the bound of {@code A}, which is
   * {@code W}, whose bound is {@code V}. The substitution must reach the bound of {@code W} within
   * the bound of {@code A}, so that {@code A := PairOfSelf} is checked against {@code
   * PairOfSelfAndOther}.
   */
  @Test
  public void testSubstitutionReachesBoundOfVariableInBound() {
    TypeVariable v = typeParameter(ChainedVariableBounds.class, 0);
    assertTrue(v.canBeInstantiatedBy(ClassOrInterfaceType.forClass(PairOfSelf.class)));
    assertFalse(v.canBeInstantiatedBy(ClassOrInterfaceType.forClass(PairOfSelfAndOther.class)));
  }

  /**
   * Type parameters, two of which are bounded by a type that contains {@code V}, whose bound
   * contains {@code K}.
   *
   * @param <K> the key type
   * @param <V> a type parameter whose bound refers to {@code K} and to itself
   * @param <W> a type parameter bounded by {@code V}
   * @param <Y> a type parameter bounded by a list of {@code V}
   */
  static class BoundsContainingKeyedMap<
      K, V extends Map<K, List<V>>, W extends V, Y extends List<V>> {}

  /** A map whose keys are integers. */
  abstract static class IntegerKeyedMap implements Map<Integer, List<IntegerKeyedMap>> {}

  /**
   * A substitution for {@code K} changes the bound of {@code V}, and so the bounds of {@code W} and
   * {@code Y}, which contain {@code V}.
   */
  @Test
  public void testSubstitutionChangesBoundOfVariableInBound() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(BoundsContainingKeyedMap.class).getTypeParameters();
    Substitution keyIsString = new Substitution(parameters.get(0), JavaTypes.STRING_TYPE);
    ReferenceType integerKeyedMap = ClassOrInterfaceType.forClass(IntegerKeyedMap.class);
    TypeVariable w = parameters.get(2);
    assertTrue(w.canBeInstantiatedBy(integerKeyedMap));
    assertFalse(((TypeVariable) w.substitute(keyIsString)).canBeInstantiatedBy(integerKeyedMap));
    TypeVariable y = parameters.get(3);
    InstantiatedType listOfIntegerKeyedMap =
        GenericClassType.forClass(List.class).instantiate(integerKeyedMap);
    assertTrue(y.canBeInstantiatedBy(listOfIntegerKeyedMap));
    assertFalse(
        ((TypeVariable) y.substitute(keyIsString)).canBeInstantiatedBy(listOfIntegerKeyedMap));
  }

  /**
   * The type parameters of a copy of a type variable, whose bound contains the original type
   * variable, start with the copy.
   */
  @Test
  public void testTypeParametersOfCopyStartWithCopy() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(BoundsContainingKeyedMap.class).getTypeParameters();
    TypeVariable copy =
        (TypeVariable)
            parameters
                .get(1)
                .substitute(new Substitution(parameters.get(0), JavaTypes.STRING_TYPE));
    assertSame(copy, copy.getTypeParameters().get(0));
  }

  /**
   * A type with a wildcard bounded below.
   *
   * @param <A> a type parameter
   * @param <B> a type parameter bounded by {@code A}
   */
  static class SecondBoundedByFirst<A, B extends A> {}

  /** A field whose type has a wildcard bounded below, for a type parameter that bounds another. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  SecondBoundedByFirst<? super Number, ? extends Comparable<?>> superNumberField;

  /** A field whose type has a wildcard bounded below. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  Comparable<? super Number> comparableSuperNumberField;

  /**
   * A subtype of the lower bound of a capture variable is within a bound that is the capture
   * variable, even though it cannot instantiate the capture variable.
   */
  @Test
  public void testSubtypeOfLowerBoundIsWithinVariableBound() throws NoSuchFieldException {
    ReferenceType integer = ClassOrInterfaceType.forClass(Integer.class);
    TypeVariable captureOfSuperNumber =
        typeArgument(capturedFieldType("comparableSuperNumberField"), 0);
    assertFalse(captureOfSuperNumber.canBeInstantiatedBy(integer));
    assertTrue(
        new EagerReferenceBound(captureOfSuperNumber).isUpperBound(integer, new Substitution()));
    // Integer <: Number <: the capture variable for the first argument.
    TypeVariable second = typeArgument(capturedFieldType("superNumberField"), 1);
    assertTrue(second.canBeInstantiatedBy(integer));
  }

  /**
   * Type parameters, one of which has a bound with a wildcard bounded below by a type variable
   * whose bound mentions another type variable.
   *
   * @param <X> a type parameter
   * @param <T> a type parameter bounded by a list of {@code X}
   * @param <U> a type parameter bounded by a map whose value is bounded below by {@code T}
   */
  static class LowerWildcardOfBoundedVariable<X, T extends List<X>, U extends Map<U, ? super T>> {}

  /** A substitution for {@code X} reaches the bound of {@code T} in the bound of {@code U}. */
  @Test
  public void testSubstitutionReachesLowerWildcardBound() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(LowerWildcardOfBoundedVariable.class).getTypeParameters();
    TypeVariable u = parameters.get(2);
    TypeVariable substituted =
        (TypeVariable) u.substitute(new Substitution(parameters.get(0), JavaTypes.STRING_TYPE));
    InstantiatedType bound =
        (InstantiatedType) ((ReferenceBound) substituted.getUpperTypeBound()).getBoundType();
    WildcardArgument wildcard = (WildcardArgument) bound.getTypeArguments().get(1);
    TypeVariable t = (TypeVariable) ((ReferenceBound) wildcard.getTypeBound()).getBoundType();
    assertEquals(
        GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE),
        ((ReferenceBound) t.getUpperTypeBound()).getBoundType());
  }

  /**
   * A generic class with a non-generic member class.
   *
   * @param <Z> a type parameter
   */
  static class OuterWithMember<Z> {
    /** A non-generic member class. */
    class Inner {}
  }

  /**
   * Type parameters, one of which has a bound that mentions a non-generic member class of a generic
   * class.
   *
   * @param <K> a type parameter
   * @param <V> a type parameter whose bound mentions {@code K}
   * @param <T> a type parameter whose bound mentions {@code V} and {@code
   *     OuterWithMember<String>.Inner}
   */
  static class BoundWithMemberClass<
      K, V extends Map<K, V>, T extends Map<V, OuterWithMember<String>.Inner>> {}

  /** A field of type {@code OuterWithMember<String>.Inner}. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  OuterWithMember<String>.Inner stringInnerField;

  /**
   * A substitution for {@code K} converts a non-generic member class in the bound of {@code T}. The
   * member class keeps its owner type, {@code OuterWithMember<String>}.
   */
  @Test
  public void testSubstitutionReachesBoundWithMemberClass() throws NoSuchFieldException {
    List<TypeVariable> parameters =
        GenericClassType.forClass(BoundWithMemberClass.class).getTypeParameters();
    TypeVariable t = parameters.get(2);
    TypeVariable substituted =
        (TypeVariable) t.substitute(new Substitution(parameters.get(0), JavaTypes.STRING_TYPE));
    InstantiatedType bound =
        (InstantiatedType) ((ReferenceBound) substituted.getUpperTypeBound()).getBoundType();
    assertEquals(
        ownFieldType("stringInnerField"),
        ((ReferenceArgument) bound.getTypeArguments().get(1)).getReferenceType());
  }

  /** A field whose type has a wildcard bounded below by an array type. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  SecondBoundedByFirst<? super String[], ?> superStringArrayField;

  /**
   * An array type is within a bound that is a capture variable if a supertype of the array type,
   * such as {@code Object}, can instantiate the capture variable.
   */
  @Test
  public void testArraySupertypeCanInstantiateVariableBound() throws NoSuchFieldException {
    ReferenceType integerArray = ArrayType.forClass(Integer[].class);
    InstantiatedType capturedType = capturedFieldType("superStringArrayField");
    TypeVariable first = typeArgument(capturedType, 0);
    TypeVariable second = typeArgument(capturedType, 1);
    assertFalse(first.canBeInstantiatedBy(integerArray));
    // Integer[] <: Object, which can instantiate the capture variable for the first argument.
    assertTrue(first.canBeSupertypeOf(integerArray, new Substitution()));
    assertTrue(second.canBeInstantiatedBy(integerArray));
  }

  /** A field whose type has a wildcard bounded below by an array type and above by another. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  SecondBoundedByFirst<Object[], ? super String[]> objectArraySuperStringArrayField;

  /**
   * An array type whose component type is a type variable is within a bound that is a capture
   * variable if an array of a supertype of the type variable's bound, such as {@code Object[]}, can
   * instantiate the capture variable.
   */
  @Test
  public void testVariableComponentArraySupertypeCanInstantiateCaptureVariable()
      throws NoSuchFieldException {
    // U extends Number
    ReferenceType uArray = ArrayType.ofComponentType(typeParameter(NumberBound.class, 0));
    InstantiatedType capturedType = capturedFieldType("objectArraySuperStringArrayField");
    TypeVariable capture = typeArgument(capturedType, 1);
    assertFalse(capture.canBeInstantiatedBy(uArray));
    // U[] <: Object[], which is within both bounds of the capture variable.
    assertTrue(capture.canBeSupertypeOf(uArray, new Substitution()));
  }

  /**
   * An interface type is within a bound that is a capture variable if {@code Object}, which is a
   * supertype of every interface, can instantiate the capture variable.
   */
  @Test
  public void testObjectSupertypeOfInterfaceCanInstantiateCaptureVariable()
      throws NoSuchFieldException {
    ReferenceType runnable = ClassOrInterfaceType.forClass(Runnable.class);
    InstantiatedType capturedType = capturedFieldType("comparableSuperNumberField");
    TypeVariable capture = typeArgument(capturedType, 0);
    assertFalse(capture.canBeInstantiatedBy(runnable));
    // Runnable <: Object, which can instantiate the capture variable.
    assertTrue(capture.canBeSupertypeOf(runnable, new Substitution()));
  }

  /**
   * A type parameter bounded by a sibling with a self-referential bound.
   *
   * @param <K> a type parameter with a self-referential bound
   * @param <V> a type parameter bounded by {@code K}
   */
  static class BoundedBySelfComparable<K extends Comparable<K>, V extends K> {}

  /** A class that is comparable to itself. */
  abstract static class ComparableBase implements Comparable<ComparableBase> {}

  /**
   * A subclass of {@link ComparableBase}. It is comparable to {@link ComparableBase}, not to
   * itself.
   */
  abstract static class ComparableSub extends ComparableBase {}

  /**
   * A bound that is a type variable admits a type if a strict supertype of the type can instantiate
   * the type variable.
   */
  @Test
  public void testVariableBoundIsSupertype() {
    TypeVariable k = typeParameter(BoundedBySelfComparable.class, 0);
    TypeVariable v = typeParameter(BoundedBySelfComparable.class, 1);
    ReferenceType sub = ClassOrInterfaceType.forClass(ComparableSub.class);
    // K cannot be ComparableSub, but it can be ComparableBase, a supertype of ComparableSub.
    assertFalse(k.canBeInstantiatedBy(sub));
    assertTrue(v.canBeInstantiatedBy(sub));
    assertTrue(new EagerReferenceBound(k).isUpperBound(sub, new Substitution()));
    assertTrue(new LazyReferenceBound(k).isUpperBound(sub, new Substitution()));
    // No supertype of ComparableToOther is comparable to itself.
    ReferenceType other = ClassOrInterfaceType.forClass(ComparableToOther.class);
    assertFalse(v.canBeInstantiatedBy(other));
    assertFalse(new EagerReferenceBound(k).isUpperBound(other, new Substitution()));
    assertFalse(new LazyReferenceBound(k).isUpperBound(other, new Substitution()));
  }

  /**
   * A generic class that is comparable to itself.
   *
   * @param <X> a type parameter
   */
  abstract static class GenericComparableBase<X> implements Comparable<GenericComparableBase<X>> {}

  /**
   * A subclass of {@link GenericComparableBase}, which is not comparable to itself.
   *
   * @param <X> a type parameter
   */
  abstract static class GenericComparableSub<X> extends GenericComparableBase<X> {}

  /**
   * A strict supertype that mentions the type variables of a generic type can instantiate a type
   * variable that is a bound.
   */
  @Test
  public void testVariableBoundIsGenericSupertype() {
    TypeVariable k = typeParameter(BoundedBySelfComparable.class, 0);
    TypeVariable v = typeParameter(BoundedBySelfComparable.class, 1);
    TypeVariable x = typeParameter(Unbounded.class, 0);
    // K can be GenericComparableBase<X>, a supertype of GenericComparableSub<X>.
    InstantiatedType subOfX = GenericClassType.forClass(GenericComparableSub.class).instantiate(x);
    assertFalse(k.canBeInstantiatedBy(subOfX));
    assertTrue(v.canBeInstantiatedBy(subOfX));
    assertTrue(new EagerReferenceBound(k).isUpperBound(subOfX, new Substitution()));
    assertTrue(new LazyReferenceBound(k).isUpperBound(subOfX, new Substitution()));
  }

  /**
   * A class with a field whose wildcard bound mentions the class's type parameter.
   *
   * @param <T> a type parameter that the wildcard bound mentions
   */
  static class ComparableListHolder<T extends Comparable<? super T>> {
    /** A field of type {@code List<? extends Comparable<? super T>>}. */
    @SuppressWarnings("UnusedVariable") // the field is read reflectively
    List<? extends Comparable<? super T>> comparableListField;
  }

  @Test
  public void testCaptureWithBoundThatMentionsOtherVariable() throws NoSuchFieldException {
    InstantiatedType comparableList =
        (InstantiatedType)
            ClassOrInterfaceType.forType(
                ComparableListHolder.class
                    .getDeclaredField("comparableListField")
                    .getGenericType());
    // The upper bound of the capture variable, Comparable<? super T>, mentions T, which the
    // subtype test does not substitute.
    assertTrue(comparableList.applyCaptureConversion().isSubtypeOfOrEqualTo(comparableList));
  }
}
