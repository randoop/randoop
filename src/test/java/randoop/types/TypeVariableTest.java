package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
            .instantiate(t, GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE)),
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
}
