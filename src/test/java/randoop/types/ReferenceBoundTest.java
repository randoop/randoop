package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/** Tests {@link ReferenceBound} and its subclasses. */
public class ReferenceBoundTest {

  /**
   * Returns {@code List<? super U>}, where the bound of {@code U} is {@code Integer}.
   *
   * @param <U> a type parameter whose bound is {@code Integer}
   * @return null
   */
  public static <U extends Integer> List<? super U> listSuperIntegerVariable() {
    return null;
  }

  /**
   * Returns {@code List<? super T>}, where the bound of {@code T} is {@code Object}.
   *
   * @param <T> a type parameter whose bound is {@code Object}
   * @return null
   */
  public static <T> List<? super T> listSuperVariable() {
    return null;
  }

  /**
   * Returns {@code List<? extends U>}, where the bound of {@code U} is {@code Number}.
   *
   * @param <U> a type parameter whose bound is {@code Number}
   * @return null
   */
  public static <U extends Number> List<? extends U> listExtendsNumberVariable() {
    return null;
  }

  /**
   * Returns {@code List<? super U>}, where the bound of {@code U} is {@code Comparable<T>}.
   *
   * @param <T> a type parameter
   * @param <U> a type parameter whose bound is {@code Comparable<T>}
   * @return null
   */
  public static <T, U extends Comparable<T>> List<? super U> listSuperComparableVariable() {
    return null;
  }

  /**
   * Returns {@code List<? super U>}, where the bound of {@code U} is {@code Number & Runnable}.
   *
   * @param <U> a type parameter whose bound is {@code Number & Runnable}
   * @return null
   */
  public static <U extends Number & Runnable> List<? super U> listSuperIntersectionVariable() {
    return null;
  }

  /**
   * Returns the given method of this class.
   *
   * @param methodName the name of a method of this class that has no parameters
   * @return the method of this class named {@code methodName}
   * @throws NoSuchMethodException if this class has no such method
   */
  private static Method method(String methodName) throws NoSuchMethodException {
    return ReferenceBoundTest.class.getMethod(methodName);
  }

  /**
   * Returns the type parameter at the given index of the given method of this class.
   *
   * @param methodName the name of a method of this class that has type parameters
   * @param index the index of the type parameter
   * @return the type parameter at {@code index} of the method named {@code methodName}
   * @throws NoSuchMethodException if this class has no such method
   */
  private static TypeVariable typeParameter(String methodName, int index)
      throws NoSuchMethodException {
    return TypeVariable.forType(method(methodName).getTypeParameters()[index]);
  }

  /**
   * Returns the capture variable of the type argument of the return type of the given method of
   * this class.
   *
   * @param methodName the name of a method of this class whose return type is {@code List<W>} for
   *     some wildcard {@code W}
   * @return the capture variable of the wildcard in the return type of the method
   * @throws NoSuchMethodException if this class has no such method
   */
  private static TypeVariable captureVariable(String methodName) throws NoSuchMethodException {
    InstantiatedType listType =
        (InstantiatedType) Type.forType(method(methodName).getGenericReturnType());
    return (TypeVariable)
        ((ReferenceArgument) listType.applyCaptureConversion().getTypeArguments().get(0))
            .getReferenceType();
  }

  @Test
  public void testEagerAndLazyEquality() {
    ReferenceType boundType = NonParameterizedType.forClass(Object.class);
    ReferenceBound eager = new EagerReferenceBound(boundType);
    ReferenceBound lazy = new LazyReferenceBound(boundType);

    // Equality is symmetric across the two subclasses.
    assertTrue(eager.equals(lazy));
    assertTrue(lazy.equals(eager));
    assertEquals(eager.hashCode(), lazy.hashCode());
  }

  @Test
  public void testEqualityOfSameSubclass() {
    ReferenceType boundType = NonParameterizedType.forClass(Number.class);
    ReferenceType otherBoundType = NonParameterizedType.forClass(Object.class);

    assertEquals(new EagerReferenceBound(boundType), new EagerReferenceBound(boundType));
    assertEquals(
        new EagerReferenceBound(boundType).hashCode(),
        new EagerReferenceBound(boundType).hashCode());
    assertEquals(new LazyReferenceBound(boundType), new LazyReferenceBound(boundType));
    assertEquals(
        new LazyReferenceBound(boundType).hashCode(), new LazyReferenceBound(boundType).hashCode());

    assertFalse(new EagerReferenceBound(boundType).equals(new LazyReferenceBound(otherBoundType)));
    assertFalse(new LazyReferenceBound(otherBoundType).equals(new EagerReferenceBound(boundType)));
  }

  /**
   * Returns {@code List<? extends String[]>}.
   *
   * @return null
   */
  public static List<? extends String[]> listExtendsStringArray() {
    return null;
  }

  @Test
  public void testArrayClassWildcardBound() throws NoSuchMethodException {
    InstantiatedType listType =
        (InstantiatedType)
            Type.forType(
                ReferenceBoundTest.class
                    .getMethod("listExtendsStringArray")
                    .getGenericReturnType());
    ArrayType stringArray = ArrayType.forClass(String[].class);
    // Reflection represents String[] in the wildcard bound as a Class, which is an array type.
    WildcardArgument wildcard = (WildcardArgument) listType.getTypeArguments().get(0);
    assertEquals(stringArray, ((ReferenceBound) wildcard.getTypeBound()).getBoundType());

    TypeVariable captureVariable =
        (TypeVariable)
            ((ReferenceArgument) listType.applyCaptureConversion().getTypeArguments().get(0))
                .getReferenceType();
    assertNotNull(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(stringArray, captureVariable));
    // Object[] is not a subtype of String[].
    assertNull(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
            ArrayType.forClass(Object[].class), captureVariable));
  }

  @Test
  public void testArrayClassBound() {
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), String[].class);
    Type argType = ArrayType.forClass(String[].class);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(argType, empty));
    assertTrue(bound.isLowerBound(argType, empty));
  }

  /**
   * A type parameter whose bound has a wildcard bounded by an array class.
   *
   * @param <T> the type parameter
   */
  static class WildcardArrayClassBound<T extends List<? extends String[]>> {}

  /** A class that satisfies the bound of {@link WildcardArrayClassBound}. */
  @SuppressWarnings("serial")
  static class ArrayClassList extends ArrayList<String[]> {}

  @Test
  public void testWildcardArrayClassBound() {
    TypeVariable t =
        GenericClassType.forClass(WildcardArrayClassBound.class).getTypeParameters().get(0);
    assertTrue(t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(ArrayClassList.class)));
  }

  @Test
  public void testVariableIsLowerBound() throws NoSuchMethodException {
    TypeVariable u = typeParameter("listSuperIntegerVariable", 0);
    TypeVariable captureVariable = captureVariable("listSuperIntegerVariable");
    Substitution substitution = new Substitution();
    ReferenceBound bound = new EagerReferenceBound(u);
    assertTrue(bound.isLowerBound(JavaTypes.OBJECT_TYPE, substitution));
    assertTrue(bound.isLowerBound(NonParameterizedType.forClass(Number.class), substitution));
    // U is a subtype of Integer, which is not a subtype of String.
    assertFalse(bound.isLowerBound(JavaTypes.STRING_TYPE, substitution));
    // U is a subtype of the capture variable of `? super U`.
    assertTrue(bound.isLowerBound(captureVariable, substitution));
  }

  @Test
  public void testVariableIsLowerBoundWithSubstitutedBound() throws NoSuchMethodException {
    TypeVariable t = typeParameter("listSuperComparableVariable", 0);
    TypeVariable u = typeParameter("listSuperComparableVariable", 1);
    ReferenceBound bound = new EagerReferenceBound(u);
    Substitution substitution = new Substitution(t, JavaTypes.STRING_TYPE);
    GenericClassType comparable = GenericClassType.forClass(Comparable.class);
    // U is a subtype of Comparable<T>, which the substitution makes Comparable<String>.
    assertTrue(bound.isLowerBound(comparable.instantiate(JavaTypes.STRING_TYPE), substitution));
    assertFalse(
        bound.isLowerBound(
            comparable.instantiate(JavaTypes.INT_TYPE.toBoxedPrimitive()), substitution));
  }

  @Test
  public void testVariableIsUpperBound() throws NoSuchMethodException {
    TypeVariable u = typeParameter("listExtendsNumberVariable", 0);
    Substitution substitution = new Substitution();
    ReferenceBound bound = new EagerReferenceBound(u);
    assertTrue(bound.isUpperBound(u, substitution));
    // The substitution does not map U, so U can be chosen as Number, a supertype of Integer.
    assertTrue(bound.isUpperBound(JavaTypes.INT_TYPE.toBoxedPrimitive(), substitution));
    // U extends Number, so it cannot be a supertype of String.
    assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, substitution));
  }

  @Test
  public void testCaptureOfSuperVariable() throws NoSuchMethodException {
    // The capture variable of List<? super U> has lower bound U, where U extends Integer.
    TypeVariable captureVariable = captureVariable("listSuperIntegerVariable");
    assertTrue(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
                NonParameterizedType.forClass(Number.class), captureVariable)
            != null);
    // No supertype of U, where U extends Integer, is String.
    assertTrue(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
                JavaTypes.STRING_TYPE, captureVariable)
            == null);
  }

  @Test
  public void testCaptureOfSuperUnboundedVariable() throws NoSuchMethodException {
    // The capture variable of List<? super T> has lower bound T, where T extends Object.
    TypeVariable captureVariable = captureVariable("listSuperVariable");
    // T is not a subtype of String, but T can be chosen as String, as when
    // Collections.sort(List<T>, Comparator<? super T>) receives a Comparator<String>.
    assertTrue(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
                JavaTypes.STRING_TYPE, captureVariable)
            != null);
  }

  @Test
  public void testCaptureOfSuperIntersectionVariable() throws NoSuchMethodException {
    // The capture variable of List<? super U> has lower bound U, where U extends Number & Runnable.
    TypeVariable captureVariable = captureVariable("listSuperIntersectionVariable");
    // U is a subtype of each member of its bound.
    assertTrue(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
                NonParameterizedType.forClass(Number.class), captureVariable)
            != null);
    assertTrue(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
                NonParameterizedType.forClass(Runnable.class), captureVariable)
            != null);
    // No supertype of U is String.
    assertTrue(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
                JavaTypes.STRING_TYPE, captureVariable)
            == null);
  }

  @Test
  public void testArraySubtypeOfCaptureOfSuperArray() {
    // A capture variable of `? super String[]` has lower bound String[].
    ArrayType stringArray = ArrayType.forClass(String[].class);
    TypeVariable captureVariable =
        new CaptureTypeVariable(
            new WildcardArgument(new WildcardType(new EagerReferenceBound(stringArray), false)));
    assertTrue(stringArray.isSubtypeOfOrEqualTo(captureVariable));
    // Object[] is not a subtype of String[].
    assertFalse(ArrayType.forClass(Object[].class).isSubtypeOfOrEqualTo(captureVariable));
  }

  @Test
  public void testCaptureOfExtendsVariable() throws NoSuchMethodException {
    // The capture variable of List<? extends U> has upper bound U, where U extends Number.
    TypeVariable captureVariable = captureVariable("listExtendsNumberVariable");
    TypeVariable u = typeParameter("listExtendsNumberVariable", 0);
    assertTrue(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(u, captureVariable) != null);
    // U can be chosen as Number, a supertype of Integer.
    assertTrue(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
                JavaTypes.INT_TYPE.toBoxedPrimitive(), captureVariable)
            != null);
    // U extends Number, so it cannot be a supertype of String.
    assertTrue(
        ReferenceType.getInstantiatingSubstitutionforTypeVariable(
                JavaTypes.STRING_TYPE, captureVariable)
            == null);
  }
}
