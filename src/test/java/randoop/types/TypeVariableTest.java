package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
}
