package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    assertFalse(capture.equals(copy));
    assertTrue(TypeVariable.isSameVariable(capture, copy));
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
  public void testCopyWithResolvedBound() {
    TypeVariable t = typeParameter(SelfComparableBound.class, 0);
    TypeVariable copy = (TypeVariable) t.substitute(new Substitution(t, t));
    assertTrue(TypeVariable.isSameVariable(t, copy));
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
    assertTrue(TypeVariable.isSameVariable(y, copy));
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
    TypeVariable t = typeParameter(Outer.Inner.class, 0);
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
      assertTrue(bound.getLazyTypeParameters().contains(s));
    }
  }
}
