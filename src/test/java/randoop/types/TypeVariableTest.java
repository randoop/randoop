package randoop.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
  static class SelfComparableBound<T extends Comparable<T>> {}

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
   * Mutually recursive type parameters.
   *
   * @param <S> a type parameter whose bound refers to {@code T}
   * @param <T> a type parameter whose bound refers to {@code S}
   */
  static class MutualBound<S extends Comparable<T>, T extends List<S>> {}

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
  static class SelfArrayBound<T extends java.util.Map<T, T[]>> {}

  /**
   * A type parameter whose self-referential bound contains an array type.
   *
   * @param <T> the type parameter
   */
  static class OtherSelfArrayBound<T extends java.util.Map<T, T[]>> {}

  /**
   * A type parameter bounded by {@code Map<?, ?>}.
   *
   * @param <T> the type parameter
   */
  static class MapWildcardBound<T extends java.util.Map<?, ?>> {}

  /**
   * A class in the style of a fluent assertion library, whose type parameter {@code SELF} refers to
   * its sibling {@code ACTUAL}.
   *
   * @param <SELF> the type of this assertion
   * @param <ACTUAL> the type of the value under test
   */
  abstract static class AbstractAssert<SELF extends AbstractAssert<SELF, ACTUAL>, ACTUAL> {}

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

  /** A concrete instantiation of {@link AbstractAssert}. */
  static class ConcreteAssert extends AbstractAssert<ConcreteAssert, String> {}

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
   * Returns the first type parameter of the given generic class.
   *
   * @param c a generic class
   * @return the first type parameter of {@code c}
   */
  private static TypeVariable typeParameter(Class<?> c) {
    return typeParameter(c, 0);
  }

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
  public void testMutuallyRecursiveBound() {
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
  public void testSiblingTypeParameter() {
    TypeVariable self = typeParameter(AbstractAssert.class, 0);
    assertTrue(typeParameter(AbstractObjectAssert.class, 0).isInstantiationOf(self));
    assertTrue(typeParameter(StringAssertBound.class).isInstantiationOf(self));
    assertFalse(typeParameter(SelfComparableBound.class).isInstantiationOf(self));

    assertTrue(self.canBeInstantiatedBy(ClassOrInterfaceType.forClass(ConcreteAssert.class)));
    assertFalse(self.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
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
}
