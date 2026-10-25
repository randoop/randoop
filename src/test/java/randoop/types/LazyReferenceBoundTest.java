package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    assertTrue(
        "bound " + result + " should be eager since it contains no type variable",
        result instanceof EagerReferenceBound);
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
}
