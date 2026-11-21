package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
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
  static class SelfBound<T extends Comparable<T>> {}

  /**
   * A class whose second type parameter has a bound that contains both type parameters.
   *
   * @param <K> the first type parameter
   * @param <V> the second type parameter
   */
  static class KeyAndSelfBound<K, V extends Map<K, V>> {}

  /** A map whose values have the type of the map, so it satisfies the bound of the class above. */
  @SuppressWarnings("serial")
  static class SelfValuedMap extends HashMap<String, SelfValuedMap> {}

  /** A field whose type has a wildcard argument for {@link SelfBound}. */
  SelfBound<?> selfBoundField;

  /** A field whose type has wildcard arguments for {@link KeyAndSelfBound}. */
  KeyAndSelfBound<?, ?> keyAndSelfBoundField;

  /** The first type parameter of {@link TwoParameters}. */
  private static final TypeVariable V =
      GenericClassType.forClass(TwoParameters.class).getTypeParameters().get(0);

  /** The second type parameter of {@link TwoParameters}. */
  private static final TypeVariable W =
      GenericClassType.forClass(TwoParameters.class).getTypeParameters().get(1);

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
    // The bound is Map<otherCapture, capture>; instantiating capture leaves otherCapture unmapped.
    TypeVariable capture = captureVariable("keyAndSelfBoundField", 1);
    assertTrue(
        "bound " + capture.getUpperTypeBound() + " should be lazy",
        capture.getUpperTypeBound() instanceof LazyReferenceBound);
    assertTrue(capture.canBeInstantiatedBy(ClassOrInterfaceType.forClass(SelfValuedMap.class)));
  }

  @Test
  public void testGetTypeParametersOfWildcard() {
    WildcardType wildcard = new WildcardType(new EagerReferenceBound(V), true);
    assertEquals(
        Collections.singletonList(V), new LazyReferenceBound(wildcard).getTypeParameters());
  }

  /**
   * A type variable that the substitution puts in the bound stands for itself, not for an unknown:
   * for bound {@code V} with {@code V} mapped to the unrelated, unbounded {@code W}, {@code String}
   * is not known to be a subtype of {@code W}.
   */
  @Test
  public void testFixedReplacementVariableIsNotUnknown() {
    LazyReferenceBound bound = new LazyReferenceBound(V);
    assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, new Substitution(V, W)));
    // V is a sibling of W, which the substitution instantiates, so V is an unknown.
    assertTrue(
        bound.isUpperBound(JavaTypes.STRING_TYPE, new Substitution(W, JavaTypes.STRING_TYPE)));
  }

  /**
   * A lower bound that is a {@link LazyParameterBound} is compared as the equivalent {@link
   * ReferenceBound}: for {@code T extends Comparable<T>}, the resolved bound {@code Comparable<T>}
   * has the reflective bound {@code Comparable<T>} as a lower bound.
   */
  @Test
  public void testHasLazyParameterLowerBound() {
    TypeVariable t = GenericClassType.forClass(SelfBound.class).getTypeParameters().get(0);
    ParameterBound reflectiveBound = t.getUpperTypeBound();
    assertTrue(reflectiveBound instanceof LazyParameterBound);
    ParameterBound resolvedBound = t.getResolvedUpperTypeBound();
    assertTrue(resolvedBound instanceof LazyReferenceBound);
    assertTrue(
        ((LazyReferenceBound) resolvedBound).hasLowerBound(reflectiveBound, new Substitution()));
    assertTrue(
        ((LazyReferenceBound) resolvedBound).hasLowerBound(resolvedBound, new Substitution()));
  }
}
