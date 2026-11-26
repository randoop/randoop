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

  /**
   * A class with a field whose type has a wildcard whose lower bound is a type parameter whose
   * bound mentions another type parameter.
   *
   * @param <X> the element type in the bound of {@code Y}
   * @param <Y> a type parameter bounded by {@code List<X>}
   */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  static class SuperBoundedVariableField<X, Y extends List<X>> {
    /** A wildcard type whose lower bound is a type parameter with a generic bound. */
    Comparable<? super Y> superY;

    /** The bound of {@code Y}, with {@code X := String}. */
    List<String> listOfString;
  }

  @Test
  public void testSubstitutionChangesBoundOfCaptureLowerBound() throws NoSuchFieldException {
    InstantiatedType captured =
        ((InstantiatedType)
                Type.forType(
                    SuperBoundedVariableField.class.getDeclaredField("superY").getGenericType()))
            .applyCaptureConversion();
    TypeVariable capture =
        (TypeVariable) ((ReferenceArgument) captured.getTypeArguments().get(0)).getReferenceType();
    // The substitution changes only the bound of Y, which is the lower bound of the capture.  The
    // substituted lower bound equals the original one, but the copy must keep it.
    ReferenceType copy =
        capture.substitute(
            new Substitution(
                typeParameter(SuperBoundedVariableField.class, 0), JavaTypes.STRING_TYPE));
    assertTrue(copy instanceof CaptureTypeVariable);
    ParameterBound lowerBound = ((TypeVariable) copy).getLowerTypeBound();
    TypeVariable y = (TypeVariable) ((ReferenceBound) lowerBound).getBoundType();
    assertEquals(
        Type.forType(
            SuperBoundedVariableField.class.getDeclaredField("listOfString").getGenericType()),
        ((ReferenceBound) y.getUpperTypeBound()).getBoundType());
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

  /**
   * A type variable that the substitution puts in a variable bound stands for itself, not for an
   * unknown type.
   */
  @Test
  public void testVariableBoundReplacedByFixedVariable() {
    TypeVariable k = typeParameter(UnboundedVariableBound.class, 0);
    TypeVariable v = typeParameter(UnboundedVariableBound.class, 1);
    TypeVariable e = typeParameter(List.class, 0);
    // K is E, which stands for itself.  String is not known to be a subtype of E.
    assertFalse(v.canBeInstantiatedBy(JavaTypes.STRING_TYPE, new Substitution(k, e)));
    // K is unknown, so it can be String or a supertype of String.
    assertTrue(v.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
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

  /**
   * Substitutions that map a type variable to copies of a type variable with different bounds are
   * inconsistent, and cannot be merged.
   */
  @Test
  public void testSubstitutionsToTypeVariableCopiesAreInconsistent() {
    TypeVariable x = typeParameter(ListBound.class, 0);
    TypeVariable y = typeParameter(ListBound.class, 1);
    TypeVariable copy = (TypeVariable) y.substitute(new Substitution(x, JavaTypes.STRING_TYPE));
    Substitution toY = new Substitution(x, y);
    Substitution toCopy = new Substitution(x, copy);
    assertFalse(toY.isConsistentWith(toCopy));
    assertFalse(toCopy.isConsistentWith(toY));
    try {
      toY.extend(toCopy);
      throw new AssertionError("extend should reject a copy with other bounds");
    } catch (IllegalArgumentException expected) {
      // expected
    }
    Substitution toSameY = new Substitution(x, y);
    assertTrue(toY.isConsistentWith(toSameY));
    assertEquals(toY, toY.extend(toSameY));
  }

  /**
   * Substitutions that map a type variable to equal types that contain a type variable and, at
   * another occurrence, either it or a copy of it with different bounds are inconsistent.
   */
  @Test
  public void testSubstitutionsToTypesWithRepeatedTypeVariableCopiesAreInconsistent() {
    TypeVariable k = typeParameter(VariableBound.class, 0);
    TypeVariable x = typeParameter(ListBound.class, 0);
    TypeVariable y = typeParameter(ListBound.class, 1);
    TypeVariable copy = (TypeVariable) y.substitute(new Substitution(x, JavaTypes.STRING_TYPE));
    GenericClassType map = GenericClassType.forClass(Map.class);
    InstantiatedType mapYY = map.instantiate(y, y);
    InstantiatedType mapYCopy = map.instantiate(y, copy);
    assertEquals(mapYY, mapYCopy);
    assertTrue(TypeVariable.areIdentical(mapYY, map.instantiate(y, y)));
    assertFalse(TypeVariable.areIdentical(mapYY, mapYCopy));
    assertFalse(new Substitution(k, mapYY).isConsistentWith(new Substitution(k, mapYCopy)));
  }

  /**
   * The capture variable of {@code ? super Number} is not a supertype of an array type. It stands
   * for a particular type, which might be {@code Object} but is known only to be a supertype of
   * {@code Number}.
   */
  @Test
  public void testCaptureVariableIsNotSupertypeOfArray() {
    CaptureTypeVariable superNumber =
        new CaptureTypeVariable(
            new WildcardArgument(
                new WildcardType(
                    new EagerReferenceBound(ClassOrInterfaceType.forClass(Number.class)), false)));
    ArrayType stringArray = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);
    assertFalse(superNumber.canBeSupertypeOf(stringArray, new Substitution()));
    assertFalse(superNumber.canBeSupertypeOf(JavaTypes.STRING_TYPE, new Substitution()));
    assertTrue(
        superNumber.canBeSupertypeOf(JavaTypes.INT_TYPE.toBoxedPrimitive(), new Substitution()));

    // An upper bound of Number leaves no supertype of String[] to instantiate the capture variable.
    CaptureTypeVariable extendsNumber =
        new CaptureTypeVariable(
            new WildcardArgument(
                new WildcardType(
                    new EagerReferenceBound(ClassOrInterfaceType.forClass(Number.class)), true)));
    assertFalse(extendsNumber.canBeSupertypeOf(stringArray, new Substitution()));
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

  /** Substituting a copy with other bounds for a variable whose bound changes yields that copy. */
  @Test
  public void testSubstituteCopyForVariableWithChangedBound() {
    TypeVariable t = typeParameter(SelfComparableBound.class, 0);
    TypeVariable copy =
        t.createCopyWithBounds(
            t.getLowerTypeBound(), new EagerReferenceBound(JavaTypes.OBJECT_TYPE));
    assertSame(copy, t.substitute(new Substitution(t, copy)));
  }

  /** Every occurrence of a type variable is compared, not only the first. */
  @Test
  public void testAreIdenticalComparesRepeatedOccurrences() {
    TypeVariable t = typeParameter(SelfComparableBound.class, 0);
    TypeVariable copy =
        t.createCopyWithBounds(
            t.getLowerTypeBound(), new EagerReferenceBound(JavaTypes.OBJECT_TYPE));
    GenericClassType mapType = GenericClassType.forClass(Map.class);
    InstantiatedType mapTT = mapType.instantiate(t, t);
    InstantiatedType mapTCopy = mapType.instantiate(t, copy);
    // Copies of a type variable are equal, even though their bounds differ.
    assertEquals(mapTT, mapTCopy);
    assertTrue(TypeVariable.areIdentical(mapTT, mapType.instantiate(t, t)));
    assertFalse(TypeVariable.areIdentical(mapTT, mapTCopy));
    assertFalse(TypeVariable.areIdentical(mapTCopy, mapTT));
    assertFalse(
        TypeVariable.areIdentical(ParameterBound.forType(mapTT), ParameterBound.forType(mapTCopy)));

    // The same holds within an array type and within a wildcard bound.
    GenericClassType listType = GenericClassType.forClass(List.class);
    InstantiatedType listOfMapTT = listType.instantiate(ArrayType.ofComponentType(mapTT));
    InstantiatedType listOfMapTCopy = listType.instantiate(ArrayType.ofComponentType(mapTCopy));
    assertEquals(listOfMapTT, listOfMapTCopy);
    assertFalse(TypeVariable.areIdentical(listOfMapTT, listOfMapTCopy));
    InstantiatedType extendsTT =
        new InstantiatedType(
            listType,
            Collections.<TypeArgument>singletonList(
                new WildcardArgument(new WildcardType(new EagerReferenceBound(mapTT), true))));
    InstantiatedType extendsTCopy =
        new InstantiatedType(
            listType,
            Collections.<TypeArgument>singletonList(
                new WildcardArgument(new WildcardType(new EagerReferenceBound(mapTCopy), true))));
    assertEquals(extendsTT, extendsTCopy);
    assertFalse(TypeVariable.areIdentical(extendsTT, extendsTCopy));
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

  /**
   * Copies of a capture variable are not identical if their bounds contain copies of a type
   * variable with different bounds.
   */
  @Test
  public void testIsIdenticalToComparesNestedBoundsOfCaptureVariable() {
    TypeVariable y = typeParameter(ListBound.class, 1);
    TypeVariable yCopy =
        y.createCopyWithBounds(
            y.getLowerTypeBound(),
            new EagerReferenceBound(
                ((GenericClassType) ClassOrInterfaceType.forClass(List.class))
                    .instantiate(JavaTypes.STRING_TYPE)));
    CaptureTypeVariable capture =
        new CaptureTypeVariable(
            new WildcardArgument(new WildcardType(new EagerReferenceBound(y), true)));
    ParameterBound lower = capture.getLowerTypeBound();
    TypeVariable withY = capture.createCopyWithBounds(lower, new EagerReferenceBound(y));
    TypeVariable withY2 = capture.createCopyWithBounds(lower, new EagerReferenceBound(y));
    TypeVariable withYCopy = capture.createCopyWithBounds(lower, new EagerReferenceBound(yCopy));
    assertTrue(withY.isIdenticalTo(withY2));
    assertFalse(withY.isIdenticalTo(withYCopy));
  }

  /** Comparing copies of a capture variable whose bounds refer to the copies terminates. */
  @Test
  public void testIsIdenticalToSelfReferentialCaptureVariables() {
    GenericClassType comparable = JavaTypes.COMPARABLE_TYPE;
    CaptureTypeVariable capture =
        new CaptureTypeVariable(
            new WildcardArgument(
                new WildcardType(new EagerReferenceBound(JavaTypes.OBJECT_TYPE), true)));
    TypeVariable copy1 =
        capture.createCopyWithBounds(capture.getLowerTypeBound(), capture.getUpperTypeBound());
    TypeVariable copy2 =
        capture.createCopyWithBounds(capture.getLowerTypeBound(), capture.getUpperTypeBound());
    // Each copy is bounded by Comparable of itself.
    copy1.setUpperBound(new EagerReferenceBound(comparable.instantiate(copy1)));
    copy2.setUpperBound(new EagerReferenceBound(comparable.instantiate(copy2)));
    assertTrue(copy1.isIdenticalTo(copy2));
    TypeVariable copy3 =
        capture.createCopyWithBounds(capture.getLowerTypeBound(), capture.getUpperTypeBound());
    copy3.setUpperBound(new EagerReferenceBound(comparable.instantiate(JavaTypes.STRING_TYPE)));
    assertFalse(copy1.isIdenticalTo(copy3));
  }

  /** A substitution that maps two copies of a type variable to themselves maps it to itself. */
  @Test
  public void testIdentitySubstitutionOfTwoCopies() {
    java.lang.reflect.Type reflectionVariable = SelfComparableBound.class.getTypeParameters()[0];
    TypeVariable t1 = TypeVariable.forType(reflectionVariable);
    // `TypeVariable.forType` caches its result, so create a distinct copy with the same bounds.
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

  /**
   * A type parameter bounded by a list whose elements are lists of strings.
   *
   * @param <S> the type parameter
   */
  static class ListOfStringListsBound<S extends List<List<String>>> {}

  /**
   * A type parameter whose bound has a lower-bounded wildcard whose bound contains another type
   * parameter.
   *
   * @param <B> the element type in the wildcard bound
   * @param <T> the type parameter
   */
  static class SuperListBound<B, T extends List<? super List<B>>> {}

  /**
   * A type parameter whose bound has a lower-bounded wildcard whose bound contains another type
   * parameter, which is bounded by {@code Number}.
   *
   * @param <B> the element type in the wildcard bound
   * @param <T> the type parameter
   */
  static class SuperListNumberBound<B extends Number, T extends List<? super List<B>>> {}

  /**
   * A type parameter whose bound has a lower-bounded wildcard whose bound is an array of another
   * type parameter.
   *
   * @param <B> the component type in the wildcard bound
   * @param <T> the type parameter
   */
  static class SuperArrayBound<B, T extends List<? super B[]>> {}

  /**
   * A type parameter bounded by a list whose elements are arrays of strings.
   *
   * @param <S> the type parameter
   */
  static class ListOfStringArraysBound<S extends List<String[]>> {}

  @Test
  public void testUnknownInLowerWildcardBound() {
    TypeVariable s = typeParameter(ListOfStringListsBound.class);
    // B := String
    assertTrue(s.isInstantiationOf(typeParameter(SuperListBound.class, 1)));
    // String is not a subtype of Number.
    assertFalse(s.isInstantiationOf(typeParameter(SuperListNumberBound.class, 1)));
    // B := String
    assertTrue(
        typeParameter(ListOfStringArraysBound.class)
            .isInstantiationOf(typeParameter(SuperArrayBound.class, 1)));
    assertFalse(s.isInstantiationOf(typeParameter(SuperArrayBound.class, 1)));
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
      // The substitution does not affect the captured type, whose bound refers to itself.  The
      // comparison includes the bounds of the capture variable.
      assertTrue(
          TypeVariable.areIdentical(
              capturedType, capturedType.substitute(new Substitution(v, JavaTypes.STRING_TYPE))));
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
  abstract static class Version<U> implements Comparable<Version<U>> {}

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
    // variable.  Substituting Comparable<capture> for capture in the bound would nest it more
    // deeply with each substitution.  The subtype test terminates, and compares the declared bound
    // with itself.
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

  /** A class that is comparable to itself. */
  abstract static class ComparableToSelf implements Comparable<ComparableToSelf> {}

  /** A subclass of a class that is comparable to itself. */
  abstract static class SubclassOfComparableToSelf extends ComparableToSelf {}

  @Test
  public void testUnknownVariableBoundBySupertype() {
    // X extends Comparable<X> cannot be SubclassOfComparableToSelf, but it can be ComparableToSelf.
    ParameterBound bound = new EagerReferenceBound(typeParameter(SelfComparableBound.class));
    assertTrue(
        bound.isUpperBound(
            ClassOrInterfaceType.forClass(SubclassOfComparableToSelf.class), new Substitution()));
    assertFalse(bound.isUpperBound(JavaTypes.OBJECT_TYPE, new Substitution()));

    // The capture of ? super Number can be Number, a supertype of Integer.
    InstantiatedType listOfSuperNumber =
        GenericClassType.forClass(List.class)
            .instantiate(
                new WildcardType(
                    ParameterBound.forType(ClassOrInterfaceType.forClass(Number.class)), false));
    ReferenceType capture =
        ((ReferenceArgument) listOfSuperNumber.applyCaptureConversion().getTypeArguments().get(0))
            .getReferenceType();
    ParameterBound captureBound = new EagerReferenceBound(capture);
    assertTrue(
        captureBound.isUpperBound(
            ClassOrInterfaceType.forClass(Integer.class), new Substitution()));
    assertFalse(captureBound.isUpperBound(JavaTypes.STRING_TYPE, new Substitution()));
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
    TypeVariable t = typeParameter(NumberRunnableBound.class, 0);
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
    ParameterBound intersection = typeParameter(NumberRunnableBound.class, 0).getUpperTypeBound();
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

  /**
   * A generic class with a member class that declares no type parameters.
   *
   * @param <T> the type parameter
   */
  static class OuterWithPlainInner<T> {
    /** A member class that declares no type parameters. */
    class Inner {}
  }

  /** Fields whose types are member classes of parameterized types. */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  static class PlainInnerFields {
    /** A member class of {@code OuterWithPlainInner<String>}. */
    OuterWithPlainInner<String>.Inner stringInner;

    /** A member class of {@code OuterWithPlainInner<Integer>}. */
    OuterWithPlainInner<Integer>.Inner integerInner;

    /** A parameterized type that is not related to the member classes. */
    List<String> stringList;

    /** A member class of {@code OuterWithPlainInner<? extends Integer>}. */
    OuterWithPlainInner<? extends Integer>.Inner extendsIntegerInner;

    /** A member class of {@code OuterWithPlainInner<? extends Number>}. */
    OuterWithPlainInner<? extends Number>.Inner extendsNumberInner;
  }

  @Test
  public void testSubtypeOfMemberClassWithoutTypeParameters() throws NoSuchFieldException {
    ReferenceType stringInner = plainInnerFieldType("stringInner");
    ReferenceType integerInner = plainInnerFieldType("integerInner");
    ReferenceType stringList = plainInnerFieldType("stringList");
    Substitution substitution = new Substitution();
    assertTrue(stringInner instanceof NonParameterizedType);
    assertTrue(stringInner.isParameterized());
    assertTrue(Subtyping.isSubtypeOrEqual(stringInner, stringInner, substitution));
    assertFalse(Subtyping.isSubtypeOrEqual(integerInner, stringInner, substitution));
    assertFalse(Subtyping.isSubtypeOrEqual(stringList, stringInner, substitution));
  }

  /**
   * A class whose type parameter's bound is a member class of a type that mentions a sibling type
   * parameter.
   *
   * @param <U> a type parameter
   * @param <T> a type parameter whose bound mentions {@code U}
   */
  static class PlainInnerSiblingBound<U extends Number, T extends OuterWithPlainInner<U>.Inner> {}

  @Test
  public void testMemberClassBoundWithUnmappedSibling() throws NoSuchFieldException {
    TypeVariable t = typeParameter(PlainInnerSiblingBound.class, 1);
    ReferenceType integerInner = plainInnerFieldType("integerInner");
    ReferenceType stringInner = plainInnerFieldType("stringInner");
    // U can be Integer.
    assertTrue(t.canBeInstantiatedBy(integerInner));
    // U cannot be String, which is not a Number.
    assertFalse(t.canBeInstantiatedBy(stringInner));
  }

  /**
   * A member class whose enclosing type has a wildcard argument is a subtype of whatever its
   * capture is a subtype of.
   */
  @Test
  public void testSubtypeOfMemberClassWithWildcardInEnclosingType() throws NoSuchFieldException {
    ReferenceType extendsIntegerInner = plainInnerFieldType("extendsIntegerInner");
    ReferenceType extendsNumberInner = plainInnerFieldType("extendsNumberInner");
    Substitution substitution = new Substitution();
    // OuterWithPlainInner<? extends Number> contains the capture of ? extends Integer.
    assertTrue(Subtyping.isSubtypeOrEqual(extendsIntegerInner, extendsNumberInner, substitution));
    assertTrue(Subtyping.isSubtypeOrEqual(extendsNumberInner, extendsNumberInner, substitution));
    assertFalse(Subtyping.isSubtypeOrEqual(extendsNumberInner, extendsIntegerInner, substitution));
  }

  /** An eager bound compares the enclosing types of member classes by containment. */
  @Test
  public void testEagerBoundOfMemberClassWithWildcardOwner() throws NoSuchFieldException {
    ReferenceType extendsIntegerInner = plainInnerFieldType("extendsIntegerInner");
    ReferenceType extendsNumberInner = plainInnerFieldType("extendsNumberInner");
    ParameterBound integerBound = new EagerReferenceBound(extendsIntegerInner);
    ParameterBound numberBound = new EagerReferenceBound(extendsNumberInner);
    Substitution substitution = new Substitution();
    assertTrue(numberBound.isUpperBound(extendsIntegerInner, substitution));
    assertTrue(numberBound.isUpperBound(extendsNumberInner, substitution));
    assertFalse(integerBound.isUpperBound(extendsNumberInner, substitution));
    assertTrue(integerBound.isLowerBound(extendsNumberInner, substitution));
    assertTrue(numberBound.isLowerBound(extendsNumberInner, substitution));
    assertFalse(numberBound.isLowerBound(extendsIntegerInner, substitution));
  }

  /**
   * Returns the type of the given field of {@link PlainInnerFields}.
   *
   * @param name the name of the field
   * @return the type of the field
   * @throws NoSuchFieldException if there is no such field
   */
  private static ReferenceType plainInnerFieldType(String name) throws NoSuchFieldException {
    return (ReferenceType)
        Type.forType(PlainInnerFields.class.getDeclaredField(name).getGenericType());
  }

  /**
   * Fields whose types are lists of member classes of parameterized types.
   *
   * @param <S> a type parameter
   * @param <U> a type parameter
   */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  static class PlainInnerListFields<S, U> {
    /** A list of member classes of {@code OuterWithPlainInner<String>}. */
    List<OuterWithPlainInner<String>.Inner> stringInnerList;

    /** A list of member classes of {@code OuterWithPlainInner<Integer>}. */
    List<OuterWithPlainInner<Integer>.Inner> integerInnerList;

    /** A list of member classes of {@code OuterWithPlainInner<U>}. */
    List<OuterWithPlainInner<U>.Inner> variableInnerList;
  }

  @Test
  public void testSameMemberClassWithUnknownInEnclosingType() throws NoSuchFieldException {
    ReferenceType stringInnerList = plainInnerListFieldType("stringInnerList");
    ReferenceType integerInnerList = plainInnerListFieldType("integerInnerList");
    ReferenceType variableInnerList = plainInnerListFieldType("variableInnerList");
    TypeVariable s = typeParameter(PlainInnerListFields.class, 0);
    TypeVariable u = typeParameter(PlainInnerListFields.class, 1);
    // U is a sibling of S, which the substitution instantiates, so mapping U to itself makes it an
    // unknown, which can be String.
    Substitution unknownU =
        new Substitution(s, JavaTypes.STRING_TYPE).extendWithIdentity(Collections.singletonList(u));
    assertTrue(Subtyping.isSubtypeOrEqual(stringInnerList, variableInnerList, unknownU));
    assertFalse(Subtyping.isSubtypeOrEqual(integerInnerList, stringInnerList, unknownU));
    // U stands for itself, which is not String.
    assertFalse(Subtyping.isSubtypeOrEqual(stringInnerList, variableInnerList, new Substitution()));
  }

  /**
   * Returns the type of the given field of {@link PlainInnerListFields}.
   *
   * @param name the name of the field
   * @return the type of the field
   * @throws NoSuchFieldException if there is no such field
   */
  private static ReferenceType plainInnerListFieldType(String name) throws NoSuchFieldException {
    return (ReferenceType)
        Type.forType(PlainInnerListFields.class.getDeclaredField(name).getGenericType());
  }

  /**
   * A generic class with a generic member class.
   *
   * @param <E> the type parameter
   */
  static class GenericOuter<E> {
    /**
     * A generic member class.
     *
     * @param <T> the type parameter
     */
    class GenericInner<T> {}
  }

  @Test
  public void testSubtypeComparesEnclosingTypesOfTypeArguments() {
    // ClassOrInterfaceType.forClass records the enclosing type GenericOuter<E>, so the type
    // parameters are E and T.
    GenericClassType innerType =
        (GenericClassType) ClassOrInterfaceType.forClass(GenericOuter.GenericInner.class);
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    InstantiatedType numberInner =
        innerType.instantiate(ClassOrInterfaceType.forClass(Number.class), integerType);
    InstantiatedType integerInner = innerType.instantiate(integerType, integerType);
    // GenericOuter<Number>.GenericInner<Integer> and GenericOuter<Integer>.GenericInner<Integer>
    // differ only in their enclosing types.  They are different types, and so are lists of them.
    assertFalse(numberInner.equals(integerInner));
    GenericClassType listType = GenericClassType.forClass(List.class);
    assertFalse(
        Subtyping.isSubtypeOrEqual(
            listType.instantiate(integerInner),
            listType.instantiate(numberInner),
            new Substitution()));
    assertTrue(
        Subtyping.isSubtypeOrEqual(
            listType.instantiate(numberInner),
            listType.instantiate(numberInner),
            new Substitution()));
  }

  /**
   * A class with a field whose type has a wildcard with a lazy bound, which mentions a type
   * parameter whose bound is self-referential.
   *
   * @param <T> a type parameter whose bound is self-referential
   */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  static class LazyWildcardBound<T extends Comparable<T>> {
    /** A list whose type argument is a wildcard whose bound mentions {@code T}. */
    List<? extends Comparable<T>> comparableList;
  }

  @Test
  public void testContainmentByWildcardWithLazyBound() throws NoSuchFieldException {
    TypeVariable t = typeParameter(LazyWildcardBound.class, 0);
    InstantiatedType comparableList =
        (InstantiatedType)
            Type.forType(
                LazyWildcardBound.class.getDeclaredField("comparableList").getGenericType());
    WildcardArgument wildcard = (WildcardArgument) comparableList.getTypeArguments().get(0);
    assertTrue(wildcard.getTypeBound() instanceof LazyParameterBound);
    GenericClassType listType = GenericClassType.forClass(List.class);
    // T is a subtype of its bound Comparable<T>, so ? extends Comparable<T> contains T.
    assertTrue(
        Subtyping.isSubtypeOrEqual(listType.instantiate(t), comparableList, new Substitution()));
    // String is a Comparable<String>, not a Comparable<T>.
    assertFalse(
        Subtyping.isSubtypeOrEqual(
            listType.instantiate(JavaTypes.STRING_TYPE), comparableList, new Substitution()));
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
    TypeVariable original = parameters.get(1);
    TypeVariable copy =
        (TypeVariable)
            original.substitute(new Substitution(parameters.get(0), JavaTypes.STRING_TYPE));
    assertNotSame(original, copy);
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
  static class NonGenericInnerOuter<Z> {
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
   *     NonGenericInnerOuter<String>.Inner}
   */
  static class BoundWithMemberClass<
      K, V extends Map<K, V>, T extends Map<V, NonGenericInnerOuter<String>.Inner>> {}

  /** A field whose type is a non-generic member class of an instantiated generic class. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  NonGenericInnerOuter<String>.Inner stringInnerField;

  /**
   * A substitution for {@code K} converts a non-generic member class in the bound of {@code T}. The
   * member class keeps its owner type, {@code NonGenericInnerOuter<String>}.
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
    ClassOrInterfaceType inner =
        (ClassOrInterfaceType)
            ((ReferenceArgument) bound.getTypeArguments().get(1)).getReferenceType();
    assertEquals(NonGenericInnerOuter.Inner.class, inner.getRuntimeClass());
    // The owner type is NonGenericInnerOuter<String>, as written, not the declaration form
    // NonGenericInnerOuter<Z>.
    assertEquals(
        GenericClassType.forClass(NonGenericInnerOuter.class).instantiate(JavaTypes.STRING_TYPE),
        inner.enclosingType);
    assertFalse(inner.isGeneric());
    // The expected type is NonGenericInnerOuter<String>.Inner, whose owner is instantiated.
    // ClassOrInterfaceType.forClass(NonGenericInnerOuter.Inner.class) would have the generic class
    // as its owner, which is not equal.
    assertEquals(ownFieldType("stringInnerField"), inner);
  }

  /** A field whose type has a wildcard bounded below by an array type. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  SecondBoundedByFirst<? super String[], ?> superStringArrayField;

  /**
   * A capture variable stands for a particular type, so an array type is not within a bound that is
   * a capture variable merely because a supertype of the array type, such as {@code Object}, could
   * instantiate the capture variable. The search among strict supertypes applies only to explicit
   * type variables; see {@link #testVariableBoundIsSupertype}.
   */
  @Test
  public void testArraySupertypeDoesNotInstantiateCaptureBound() throws NoSuchFieldException {
    ReferenceType integerArray = ArrayType.forClass(Integer[].class);
    InstantiatedType capturedType = capturedFieldType("superStringArrayField");
    TypeVariable first = typeArgument(capturedType, 0);
    TypeVariable second = typeArgument(capturedType, 1);
    assertFalse(first.canBeInstantiatedBy(integerArray));
    // The capture of `? super String[]` might be String[], so Integer[] is not known to be a
    // subtype of it, even though Integer[] <: Object.
    assertFalse(first.canBeSupertypeOf(integerArray, new Substitution()));
    assertFalse(second.canBeInstantiatedBy(integerArray));
  }

  /** A field whose type has a wildcard bounded below by an array type and above by another. */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  SecondBoundedByFirst<Object[], ? super String[]> objectArraySuperStringArrayField;

  /**
   * An array type whose component type is a type variable is not within a bound that is a capture
   * variable merely because an array of a supertype of the type variable's bound, such as {@code
   * Object[]}, can instantiate the capture variable. A capture variable stands for a particular
   * unknown type, so its strict supertypes are not searched.
   */
  @Test
  public void testVariableComponentArraySupertypeCannotInstantiateCaptureVariable()
      throws NoSuchFieldException {
    // U extends Number
    ReferenceType uArray = ArrayType.ofComponentType(typeParameter(NumberBound.class, 0));
    InstantiatedType capturedType = capturedFieldType("objectArraySuperStringArrayField");
    TypeVariable capture = typeArgument(capturedType, 1);
    assertFalse(capture.canBeInstantiatedBy(uArray));
    // U[] <: Object[], which is within both bounds of the capture variable, but the capture
    // variable
    // could be String[], and U[] is not known to be a subtype of it.
    assertFalse(capture.canBeSupertypeOf(uArray, new Substitution()));
  }

  /**
   * An interface type is not within a bound that is a capture variable merely because {@code
   * Object}, which is a supertype of every interface, can instantiate the capture variable. A
   * capture variable stands for a particular unknown type, so its strict supertypes are not
   * searched.
   */
  @Test
  public void testInterfaceSupertypeCannotInstantiateCaptureVariableBound()
      throws NoSuchFieldException {
    ReferenceType runnable = ClassOrInterfaceType.forClass(Runnable.class);
    InstantiatedType capturedType = capturedFieldType("comparableSuperNumberField");
    TypeVariable capture = typeArgument(capturedType, 0);
    assertFalse(capture.canBeInstantiatedBy(runnable));
    // The capture of `? super Number` could be Object, but Runnable is not known to be a subtype
    // of it.
    assertFalse(capture.canBeSupertypeOf(runnable, new Substitution()));
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
   * A class whose type parameter's bound is an instantiation of the class, with no type variable.
   *
   * @param <T> the type parameter
   */
  static class SelfInstantiationBound<T extends SelfInstantiationBound<?>> {}

  @Test
  public void testBoundThatInstantiatesDeclaringClass() {
    // Converting the bound needs the generic class, which needs T, whose bound is being converted.
    TypeVariable t = typeParameter(SelfInstantiationBound.class, 0);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof EagerReferenceBound);
    ReferenceType boundType = ((EagerReferenceBound) bound).getBoundType();
    assertTrue(boundType.runtimeClassIs(SelfInstantiationBound.class));
    assertSame(t, TypeVariable.forType(SelfInstantiationBound.class.getTypeParameters()[0]));
  }

  @Test
  public void testIdenticalComparesEachOccurrence() {
    TypeVariable t = typeParameter(NumberBound.class, 0);
    TypeVariable tCopy =
        t.createCopyWithBounds(
            t.getLowerTypeBound(), new EagerReferenceBound(JavaTypes.OBJECT_TYPE));
    GenericClassType mapType = GenericClassType.forClass(Map.class);
    InstantiatedType mapOfT = mapType.instantiate(t, t);
    InstantiatedType mapOfTAndCopy = mapType.instantiate(t, tCopy);
    assertEquals(mapOfT, mapOfTAndCopy);
    assertTrue(TypeVariable.areIdentical(mapOfT, mapType.instantiate(t, t)));
    // The second occurrence of T has another bound.
    assertFalse(TypeVariable.areIdentical(mapOfT, mapOfTAndCopy));
  }

  @Test
  public void testLazyBoundHasLowerBoundOfSubtypeWithSameVariables() {
    TypeVariable t = typeParameter(SelfComparableBound.class, 0);
    InstantiatedType listOfT = GenericClassType.forClass(List.class).instantiate(t);
    InstantiatedType collectionOfT = GenericClassType.forClass(Collection.class).instantiate(t);
    InstantiatedType listOfString =
        GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE);
    Substitution substitution = new Substitution();
    LazyReferenceBound collectionBound = new LazyReferenceBound(collectionOfT);
    assertTrue(new LazyReferenceBound(listOfT).isLowerBound(collectionBound, substitution));
    assertTrue(collectionBound.isLowerBound(collectionBound, substitution));
    // List<String> is not a subtype of every instantiation of List<T>.
    assertFalse(
        new EagerReferenceBound(listOfString)
            .isLowerBound(new LazyReferenceBound(listOfT), substitution));
  }

  /** Fields whose types are member classes, nested two deep, of parameterized types. */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  static class DeepMemberFields {
    /** A member class of a member class of {@code OuterWithMiddle<String>}. */
    OuterWithMiddle<String>.Middle.Inner stringInner;

    /** A member class of a member class of {@code OuterWithMiddle<Integer>}. */
    OuterWithMiddle<Integer>.Middle.Inner integerInner;
  }

  /**
   * A generic class with a member class, which declares no type parameters, that has a member
   * class.
   *
   * @param <T> the type parameter
   */
  static class OuterWithMiddle<T> {
    /** A member class that declares no type parameters. */
    class Middle {
      /** A member class that declares no type parameters. */
      class Inner {}
    }
  }

  @Test
  public void testMemberClassPreservesTypeArgumentsOfOuterOwner() throws NoSuchFieldException {
    ReferenceType stringInner =
        (ReferenceType)
            Type.forType(DeepMemberFields.class.getDeclaredField("stringInner").getGenericType());
    ReferenceType integerInner =
        (ReferenceType)
            Type.forType(DeepMemberFields.class.getDeclaredField("integerInner").getGenericType());
    // Middle has no type arguments of its own, but its owner OuterWithMiddle<String> does.
    assertTrue(stringInner.isParameterized());
    assertFalse(stringInner.isGeneric());
    assertFalse(Subtyping.isSubtypeOrEqual(stringInner, integerInner, new Substitution()));
  }

  @Test
  public void testParameterizedTypeIsNotSubtypeOfPlainMemberClass() throws NoSuchFieldException {
    ReferenceType stringInner = plainInnerFieldType("stringInner");
    ReferenceType stringList = plainInnerFieldType("stringList");
    Substitution substitution = new Substitution();
    assertFalse(stringList.isSubtypeOfOrEqualTo(stringInner));
    assertFalse(new EagerReferenceBound(stringInner).isUpperBound(stringList, substitution));
    assertFalse(new EagerReferenceBound(stringList).isLowerBound(stringInner, substitution));
  }

  @Test
  public void testUnknownInPlainMemberClassInLowerBoundedWildcard() throws NoSuchFieldException {
    ReferenceType stringInnerList = plainInnerListFieldType("stringInnerList");
    InstantiatedType variableInnerList =
        (InstantiatedType) plainInnerListFieldType("variableInnerList");
    ReferenceType variableInner =
        ((ReferenceArgument) variableInnerList.getTypeArguments().get(0)).getReferenceType();
    // List<? super OuterWithPlainInner<U>.Inner>
    InstantiatedType superVariableInnerList =
        new InstantiatedType(
            GenericClassType.forClass(List.class),
            Collections.singletonList(
                new WildcardArgument(
                    new WildcardType(new EagerReferenceBound(variableInner), false))));
    TypeVariable s = typeParameter(PlainInnerListFields.class, 0);
    TypeVariable u = typeParameter(PlainInnerListFields.class, 1);
    Substitution unknownU =
        new Substitution(s, JavaTypes.STRING_TYPE).extendWithIdentity(Collections.singletonList(u));
    // U can be String.
    assertTrue(Subtyping.isSubtypeOrEqual(stringInnerList, superVariableInnerList, unknownU));
    // U stands for itself, which is not String.
    assertFalse(
        Subtyping.isSubtypeOrEqual(stringInnerList, superVariableInnerList, new Substitution()));
  }

  /**
   * A class with a type parameter that has an intersection bound, and a sibling type parameter.
   *
   * @param <S> a type parameter
   * @param <B> a type parameter whose bound is an intersection
   */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  static class IntersectionBoundWithSibling<S, B extends Number & Runnable> {
    /** A list whose type argument is a wildcard bounded below by {@code B}. */
    List<? super B> superB;
  }

  @Test
  public void testLowerBoundedWildcardOfUnknownWithIntersectionBound() throws NoSuchFieldException {
    TypeVariable s = typeParameter(IntersectionBoundWithSibling.class, 0);
    TypeVariable b = typeParameter(IntersectionBoundWithSibling.class, 1);
    ReferenceType superB =
        (ReferenceType)
            Type.forType(
                IntersectionBoundWithSibling.class.getDeclaredField("superB").getGenericType());
    Substitution unknownB =
        new Substitution(s, JavaTypes.STRING_TYPE).extendWithIdentity(Collections.singletonList(b));
    GenericClassType listType = GenericClassType.forClass(List.class);
    // Every B is a Runnable, though Runnable, which is not a Number, cannot be B.
    assertTrue(
        Subtyping.isSubtypeOrEqual(
            listType.instantiate(ClassOrInterfaceType.forClass(Runnable.class)), superB, unknownB));
    assertTrue(
        Subtyping.isSubtypeOrEqual(
            listType.instantiate(ClassOrInterfaceType.forClass(RunnableNumber.class)),
            superB,
            unknownB));
    // B need not be an Integer.
    assertFalse(
        Subtyping.isSubtypeOrEqual(
            listType.instantiate(JavaTypes.INT_TYPE.toBoxedPrimitive()), superB, unknownB));
  }

  @Test
  public void testEnclosingTypeParameterInParameterizedBoundStandsForItself()
      throws NoSuchMethodException {
    TypeVariable t =
        TypeVariable.forType(Outer.class.getDeclaredMethod("m").getTypeParameters()[0]);
    TypeVariable e = typeParameter(Outer.class, 0);
    InstantiatedType listOfString =
        GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE);
    // E is fixed in Outer, so List<String> is not known to be a List<E>.
    assertFalse(t.canBeInstantiatedBy(listOfString));
    assertTrue(t.canBeInstantiatedBy(listOfString, new Substitution(e, JavaTypes.STRING_TYPE)));
  }

  /**
   * A class with two unbounded type parameters.
   *
   * @param <X> a type parameter
   * @param <Y> a type parameter
   */
  static class TwoUnbounded<X, Y> {}

  @Test
  public void testMappingToCopyWithOtherBoundsIsFixed() {
    TypeVariable x = typeParameter(TwoUnbounded.class, 0);
    TypeVariable y = typeParameter(TwoUnbounded.class, 1);
    TypeVariable copyOfX =
        x.createCopyWithBounds(
            x.getLowerTypeBound(), new EagerReferenceBound(ReferenceType.forClass(Number.class)));
    ReferenceType integer = JavaTypes.INT_TYPE.toBoxedPrimitive();

    // A type variable that the substitution maps to itself is an unknown, but a copy of it that
    // the substitution maps it to stands for itself.
    assertTrue(
        new Substitution(y, JavaTypes.STRING_TYPE)
            .extendWithIdentity(Collections.singletonList(x))
            .isUnknown(x));
    Substitution toCopy = new Substitution(Arrays.asList(x, y), copyOfX, JavaTypes.STRING_TYPE);
    assertFalse(toCopy.isUnknown(copyOfX));

    // Integer is within the bound of the copy, but is not known to be a subtype of it.
    assertFalse(new EagerReferenceBound(x).isUpperBound(integer, new Substitution(x, copyOfX)));

    // Mapping x to itself keeps a copy of x, but mapping a copy of x to x replaces the copy.
    assertTrue(new Substitution(x, x).isConsistentWith(new Substitution(x, x)));
    assertFalse(new Substitution(x, x).isConsistentWith(new Substitution(copyOfX, x)));
  }

  @Test
  public void testLazyReferenceBoundReplacementVariableIsFixed() {
    TypeVariable x = typeParameter(TwoUnbounded.class, 0);
    TypeVariable e = typeParameter(Outer.class, 0);
    // An unmapped x is an unknown, which can be String.
    assertTrue(new LazyReferenceBound(x).isUpperBound(JavaTypes.STRING_TYPE, new Substitution()));
    // With x := E, String is not known to be a subtype of E.
    assertFalse(
        new LazyReferenceBound(x).isUpperBound(JavaTypes.STRING_TYPE, new Substitution(x, e)));
  }

  @Test
  public void testWildcardContainsWildcard() {
    WildcardArgument unbounded =
        new WildcardArgument(
            new WildcardType(new EagerReferenceBound(JavaTypes.OBJECT_TYPE), true));
    WildcardArgument superNumber =
        new WildcardArgument(
            new WildcardType(new EagerReferenceBound(ReferenceType.forClass(Number.class)), false));
    WildcardArgument superInteger =
        new WildcardArgument(
            new WildcardType(
                new EagerReferenceBound(JavaTypes.INT_TYPE.toBoxedPrimitive()), false));
    assertTrue(unbounded.contains(superNumber));
    assertFalse(superNumber.contains(unbounded));
    assertTrue(superInteger.contains(superNumber));
    assertFalse(superNumber.contains(superInteger));

    // A lazy bound, such as Comparable<T> for T extends Comparable<T>, can be compared.
    ParameterBound selfComparable = typeParameter(SelfComparableBound.class, 0).getUpperTypeBound();
    assertTrue(selfComparable instanceof LazyParameterBound);
    WildcardArgument extendsSelfComparable =
        new WildcardArgument(new WildcardType(selfComparable, true));
    WildcardArgument otherExtendsSelfComparable =
        new WildcardArgument(new WildcardType(selfComparable, true));
    assertTrue(extendsSelfComparable.contains(otherExtendsSelfComparable));
  }

  /**
   * A generic class with a generic member class.
   *
   * @param <T> the type parameter
   */
  static class GenericOwner<T> {
    /**
     * A generic member class.
     *
     * @param <U> the type parameter
     */
    class GenericMember<U> {}
  }

  /**
   * A generic class with a member class whose type parameter is bounded by the type parameter of
   * the class.
   *
   * @param <T> the type parameter
   */
  static class BoundingOwner<T> {
    /**
     * A generic member class.
     *
     * @param <U> a type parameter bounded by {@code T}
     */
    class BoundedMember<U extends T> {}
  }

  /**
   * A generic class.
   *
   * @param <V> the type parameter
   */
  static class Box<V> {}

  /**
   * A generic class with a member class whose superclass mentions the type parameter of the class.
   *
   * @param <T> the type parameter
   */
  static class BoxOwner<T> {
    /** A member class that declares no type parameters. */
    class BoxMember extends Box<T> {}
  }

  /**
   * A generic class with a generic member class of a generic member class.
   *
   * @param <A> the type parameter
   */
  static class DeclarationFormOwner<A> {
    /**
     * A generic member class.
     *
     * @param <B> the type parameter
     */
    class Middle<B> {
      /**
       * A generic member class.
       *
       * @param <C> the type parameter
       */
      class Leaf<C> {}

      /** A field whose type's owner is {@code DeclarationFormOwner<A>.Middle<B>}. */
      Leaf<String> leaf;
    }
  }

  /**
   * A class whose type parameter's bound is a member class of a parameterized type.
   *
   * @param <T> a type parameter bounded by a member class
   */
  static class MemberClassBound<T extends GenericOwner<String>.GenericMember<Integer>> {}

  /** Fields and methods whose types are member classes of parameterized types. */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  static class OwnerFields {
    /** A member class of {@code GenericOwner<String>}. */
    GenericOwner<String>.GenericMember<Integer> stringOwner;

    /** A member class of {@code GenericOwner<Number>}. */
    GenericOwner<Number>.GenericMember<Integer> numberOwner;

    /** A member class of {@code GenericOwner<Long>}, with a wildcard argument. */
    GenericOwner<Long>.GenericMember<? extends Number> longOwnerExtendsNumber;

    /** A member class of {@code GenericOwner<String>}, with a wildcard argument. */
    GenericOwner<String>.GenericMember<? extends Number> stringOwnerExtendsNumber;

    /** A member class with a wildcard argument whose bound depends on the enclosing type. */
    BoundingOwner<String>.BoundedMember<?> wildcardBoundedMember;

    /** A member class of a type with a wildcard argument. */
    BoxOwner<? extends Number>.BoxMember wildcardOwnerMember;

    /** A box of a wildcard type. */
    Box<? extends Number> extendsNumberBox;

    /**
     * A generic method whose parameter is a member class of a generic owner type.
     *
     * @param <X> the type parameter
     * @param parameter a parameter
     */
    <X> void instantiateOwner(GenericOwner<X>.GenericMember<Integer> parameter) {}
  }

  /**
   * Returns the type of the given field of {@link OwnerFields}.
   *
   * @param name the name of a field of {@link OwnerFields}
   * @return the type of the field
   * @throws NoSuchFieldException if there is no such field
   */
  private static ReferenceType ownerFieldType(String name) throws NoSuchFieldException {
    return (ReferenceType) Type.forType(OwnerFields.class.getDeclaredField(name).getGenericType());
  }

  @Test
  public void testSubtypeComparesEnclosingTypes() throws NoSuchFieldException {
    ReferenceType stringOwner = ownerFieldType("stringOwner");
    ReferenceType numberOwner = ownerFieldType("numberOwner");
    assertTrue(stringOwner.isSubtypeOfOrEqualTo(stringOwner));
    assertFalse(stringOwner.isSubtypeOfOrEqualTo(numberOwner));
    assertTrue(stringOwner.isSubtypeOfOrEqualTo(ownerFieldType("stringOwnerExtendsNumber")));
    assertFalse(stringOwner.isSubtypeOfOrEqualTo(ownerFieldType("longOwnerExtendsNumber")));

    // The same holds for a type variable whose bound is a member class.
    TypeVariable t = typeParameter(MemberClassBound.class);
    assertTrue(t.isSubtypeOfOrEqualTo(stringOwner));
    assertFalse(t.isSubtypeOfOrEqualTo(numberOwner));
  }

  @Test
  public void testCaptureBoundUsesEnclosingTypeArgument() throws NoSuchFieldException {
    InstantiatedType captured =
        ((InstantiatedType) ownerFieldType("wildcardBoundedMember")).applyCaptureConversion();
    ReferenceType capture =
        ((ReferenceArgument) captured.getTypeArguments().get(0)).getReferenceType();
    // The bound of U is T, which is String in BoundingOwner<String>.
    assertTrue(capture.isSubtypeOfOrEqualTo(JavaTypes.STRING_TYPE));
  }

  @Test
  public void testSupertypeOfMemberOfWildcardOwner() throws NoSuchFieldException {
    // BoxOwner<? extends Number>.BoxMember extends Box<capture of ? extends Number>.
    assertTrue(
        Subtyping.isSubtypeOrEqual(
            ownerFieldType("wildcardOwnerMember"),
            ownerFieldType("extendsNumberBox"),
            new Substitution()));
  }

  @Test
  public void testInstantiatingSubstitutionMatchesEnclosingType()
      throws NoSuchMethodException, NoSuchFieldException {
    Method method =
        OwnerFields.class.getDeclaredMethod("instantiateOwner", GenericOwner.GenericMember.class);
    ReferenceType goal = (ReferenceType) Type.forType(method.getGenericParameterTypes()[0]);
    TypeVariable x = TypeVariable.forType(method.getTypeParameters()[0]);
    ReferenceType stringOwner = ownerFieldType("stringOwner");
    // GenericOwner<String>.GenericMember<Integer> equals GenericOwner<X>.GenericMember<Integer>
    // except for the enclosing types, so the substitution maps X.
    Substitution substitution = stringOwner.getInstantiatingSubstitution(goal);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(x));
  }

  @Test
  public void testSubstitutionIntoEnclosingTypeOfDeclarationFormOwner()
      throws NoSuchFieldException {
    java.lang.reflect.Type leafType =
        DeclarationFormOwner.Middle.class.getDeclaredField("leaf").getGenericType();
    TypeVariable a = typeParameter(DeclarationFormOwner.class, 0);
    ParameterBound bound =
        new LazyParameterBound(leafType).substitute(new Substitution(a, JavaTypes.STRING_TYPE));
    ClassOrInterfaceType leaf = (ClassOrInterfaceType) ((ReferenceBound) bound).getBoundType();
    // The substitution changes only the enclosing type of Middle<B>, which is kept.
    ClassOrInterfaceType middle = leaf.enclosingType;
    assertNotNull(middle);
    assertEquals(
        GenericClassType.forClass(DeclarationFormOwner.class).instantiate(JavaTypes.STRING_TYPE),
        middle.enclosingType);
  }

  /**
   * Type parameters whose bounds have a wildcard whose bound is a non-generic member class of a
   * parameterized type.
   *
   * @param <X> a type parameter whose bound mentions {@code NonGenericInnerOuter<String>.Inner}
   * @param <Y> a type parameter whose bound mentions {@code NonGenericInnerOuter<Integer>.Inner}
   */
  static class WildcardMemberBound<
      X extends List<? extends NonGenericInnerOuter<String>.Inner>,
      Y extends List<? extends NonGenericInnerOuter<Integer>.Inner>> {}

  /**
   * Copies of a type variable are identical if their bounds differ only in the representation of a
   * nested wildcard bound: a {@link LazyParameterBound} in one, and the equivalent {@link
   * ReferenceBound} in the other.
   */
  @Test
  public void testIdenticalCopiesWithMaterializedWildcardBound() {
    TypeVariable x = typeParameter(WildcardMemberBound.class, 0);
    TypeVariable y = typeParameter(WildcardMemberBound.class, 1);
    ParameterBound bound = x.getUpperTypeBound();
    // Substituting materializes the lazy bound of the wildcard, NonGenericInnerOuter<String>.Inner.
    ParameterBound materialized = bound.substitute(new Substitution(x, x));
    assertNotSame(bound, materialized);
    TypeVariable copy = x.createCopyWithBounds(x.getLowerTypeBound(), materialized);
    assertTrue(x.isIdenticalTo(copy));
    assertTrue(copy.isIdenticalTo(x));

    // A copy whose bound has a different enclosing type, NonGenericInnerOuter<Integer>, is not
    // identical.
    ParameterBound otherBound = y.getUpperTypeBound().substitute(new Substitution(y, y));
    TypeVariable otherCopy = x.createCopyWithBounds(x.getLowerTypeBound(), otherBound);
    assertFalse(x.isIdenticalTo(otherCopy));
    assertFalse(copy.isIdenticalTo(otherCopy));
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
