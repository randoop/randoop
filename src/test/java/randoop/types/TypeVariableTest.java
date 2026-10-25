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
}
