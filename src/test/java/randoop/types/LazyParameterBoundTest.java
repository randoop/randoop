package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/** Tests {@link LazyParameterBound}. */
public class LazyParameterBoundTest {

  /**
   * A type parameter whose bound contains the type parameter, but not as the last type argument.
   *
   * @param <T> the type parameter
   */
  static class SelfFirstArgumentBound<T extends Map<T, String>> {}

  /**
   * A type parameter whose bound contains the type parameter nested in a type argument.
   *
   * @param <T> the type parameter
   */
  static class SelfNestedArgumentBound<T extends Map<List<T>, String>> {}

  /**
   * A class with a type parameter.
   *
   * @param <V> the type parameter
   */
  static class Unbounded<V> {}

  /** A field whose type has a wildcard argument for a type parameter with a recursive bound. */
  SelfFirstArgumentBound<?> wildcardField;

  /**
   * Returns the type parameter of {@link SelfFirstArgumentBound}.
   *
   * @return the type parameter of {@link SelfFirstArgumentBound}
   */
  private static TypeVariable selfFirstArgumentBoundParameter() {
    return GenericClassType.forClass(SelfFirstArgumentBound.class).getTypeParameters().get(0);
  }

  @Test
  public void testSubstituteVariable() {
    TypeVariable t = selfFirstArgumentBoundParameter();
    TypeVariable v = GenericClassType.forClass(Unbounded.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound().substitute(new Substitution(t, v));
    assertTrue(
        "bound " + bound + " should be lazy since it contains a type variable",
        bound instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class).instantiate(v, JavaTypes.STRING_TYPE),
        ((ReferenceBound) bound).getBoundType());
  }

  @Test
  public void testSubstituteConcreteType() {
    TypeVariable t = selfFirstArgumentBoundParameter();
    ParameterBound bound =
        t.getUpperTypeBound().substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertTrue(
        "bound " + bound + " should be eager since it contains no type variable",
        bound instanceof EagerReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(JavaTypes.STRING_TYPE, JavaTypes.STRING_TYPE),
        ((ReferenceBound) bound).getBoundType());
  }

  @Test
  public void testCaptureConversion() throws NoSuchFieldException {
    InstantiatedType wildcardType =
        (InstantiatedType)
            ClassOrInterfaceType.forType(
                LazyParameterBoundTest.class.getDeclaredField("wildcardField").getGenericType());
    InstantiatedType capturedType = wildcardType.applyCaptureConversion();
    TypeVariable capture =
        (TypeVariable)
            ((ReferenceArgument) capturedType.getTypeArguments().get(0)).getReferenceType();
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

  @Test
  public void testSubstituteNestedVariable() {
    TypeVariable t =
        GenericClassType.forClass(SelfNestedArgumentBound.class).getTypeParameters().get(0);
    TypeVariable v = GenericClassType.forClass(Unbounded.class).getTypeParameters().get(0);
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
  public void testSubstituteTypeContainingVariable() {
    TypeVariable t = selfFirstArgumentBoundParameter();
    TypeVariable v = GenericClassType.forClass(Unbounded.class).getTypeParameters().get(0);
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
}
