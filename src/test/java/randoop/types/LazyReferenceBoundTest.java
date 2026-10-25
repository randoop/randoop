package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/** Tests {@link LazyReferenceBound}. */
public class LazyReferenceBoundTest {

  /**
   * A class with two type parameters.
   *
   * @param <V> the first type parameter
   * @param <W> the second type parameter
   */
  static class TwoParameters<V, W> {}

  /**
   * A class whose type parameter has a bound that contains the type parameter.
   *
   * @param <T> the type parameter
   */
  static class SelfBound<T extends Map<T, String>> {}

  /**
   * A class whose second type parameter has a bound that contains both type parameters.
   *
   * @param <K> the first type parameter
   * @param <V> the second type parameter
   */
  static class SelfAndKeyBound<K, V extends Map<V, K>> {}

  /**
   * A map whose keys have the type of the map, so it satisfies the bounds of both classes above.
   */
  @SuppressWarnings("serial")
  static class SelfKeyedMap extends HashMap<SelfKeyedMap, String> {}

  /**
   * A class with a field whose type has a wildcard argument and a type variable.
   *
   * @param <V> the type parameter
   */
  static class WildcardHolder<V> {
    /** A field whose type has a wildcard argument and a type variable. */
    Map<V, ? extends Number> field;
  }

  /** A field whose type has a wildcard argument for {@link SelfBound}. */
  SelfBound<?> selfBoundField;

  /** A field whose type has wildcard arguments for {@link SelfAndKeyBound}. */
  SelfAndKeyBound<?, ?> selfAndKeyBoundField;

  /**
   * Returns a capture variable from the capture conversion of the type of the given field.
   *
   * @param fieldName the name of a field of this class whose type has wildcard arguments
   * @param index the index of the type argument whose capture variable to return
   * @return the capture variable for the given type argument
   * @throws NoSuchFieldException if there is no such field
   */
  private static TypeVariable captureVariable(String fieldName, int index)
      throws NoSuchFieldException {
    InstantiatedType capturedType =
        ((InstantiatedType)
                ClassOrInterfaceType.forType(
                    LazyReferenceBoundTest.class.getDeclaredField(fieldName).getGenericType()))
            .applyCaptureConversion();
    return (TypeVariable)
        ((ReferenceArgument) capturedType.getTypeArguments().get(index)).getReferenceType();
  }

  /** The first type parameter of {@link TwoParameters}. */
  private static final TypeVariable V =
      GenericClassType.forClass(TwoParameters.class).getTypeParameters().get(0);

  /** The second type parameter of {@link TwoParameters}. */
  private static final TypeVariable W =
      GenericClassType.forClass(TwoParameters.class).getTypeParameters().get(1);

  @Test
  public void testSubstituteArray() {
    ReferenceBound bound = new LazyReferenceBound(ArrayType.ofComponentType(V));
    ReferenceBound result = bound.substitute(new Substitution(V, JavaTypes.STRING_TYPE));
    assertFalse(
        "bound " + result + " should be eager since it contains no type variable",
        result instanceof LazyReferenceBound);
    assertEquals(ArrayType.ofComponentType(JavaTypes.STRING_TYPE), result.getBoundType());
  }

  @Test
  public void testSubstituteParameterizedWithVariable() {
    GenericClassType mapType = GenericClassType.forClass(Map.class);
    ReferenceBound bound = new LazyReferenceBound(mapType.instantiate(V, JavaTypes.STRING_TYPE));
    ReferenceBound result = bound.substitute(new Substitution(V, W));
    assertTrue(
        "bound " + result + " should be lazy since it contains a type variable",
        result instanceof LazyReferenceBound);
    assertEquals(mapType.instantiate(W, JavaTypes.STRING_TYPE), result.getBoundType());
  }

  @Test
  public void testSubstituteVariableWithTypeContainingVariable() {
    InstantiatedType listOfW = GenericClassType.forClass(List.class).instantiate(W);
    ReferenceBound result = new LazyReferenceBound(V).substitute(new Substitution(V, listOfW));
    assertTrue(
        "bound " + result + " should be lazy since it contains a type variable",
        result instanceof LazyReferenceBound);
    assertEquals(listOfW, result.getBoundType());
  }

  @Test
  public void testApplyCaptureConversion() throws NoSuchFieldException {
    ReferenceType mapType =
        ClassOrInterfaceType.forType(
            WildcardHolder.class.getDeclaredField("field").getGenericType());
    ReferenceBound bound = new LazyReferenceBound(mapType);
    ReferenceBound result = bound.applyCaptureConversion();
    assertTrue(
        "bound " + result + " should be lazy since it contains a type variable",
        result instanceof LazyReferenceBound);
    assertTrue(result.getBoundType().hasCaptureVariable());
  }

  @Test
  public void testIsInstantiationOfCaptureVariable() throws NoSuchFieldException {
    TypeVariable capture = captureVariable("selfBoundField", 0);
    TypeVariable otherCapture = captureVariable("selfBoundField", 0);
    assertTrue(
        "bound " + capture.getUpperTypeBound() + " should be lazy",
        capture.getUpperTypeBound() instanceof LazyReferenceBound);
    assertTrue(capture.isInstantiationOf(otherCapture));
  }

  @Test
  public void testCanBeInstantiatedByWithPartialSubstitution() throws NoSuchFieldException {
    // The bound is Map<capture, otherCapture>; instantiating capture leaves otherCapture unmapped.
    TypeVariable capture = captureVariable("selfAndKeyBoundField", 1);
    assertTrue(
        "bound " + capture.getUpperTypeBound() + " should be lazy",
        capture.getUpperTypeBound() instanceof LazyReferenceBound);
    assertTrue(capture.canBeInstantiatedBy(ClassOrInterfaceType.forClass(SelfKeyedMap.class)));
  }
}
