package randoop.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/** Tests {@link Substitution}. */
public class SubstitutionTest {

  /**
   * A class with several type parameters.
   *
   * @param <S> a type parameter
   * @param <T> a type parameter
   * @param <U> a type parameter
   * @param <R> a type parameter
   */
  static class Siblings<S, T, U, R> {}

  /**
   * {@link Substitution#withIdentityFor} does not change which type variables are unknowns, even if
   * it maps to themselves type variables that the substitution swaps.
   */
  @Test
  public void withIdentityForKeepsClassificationTest() {
    List<TypeVariable> parameters = GenericClassType.forClass(Siblings.class).getTypeParameters();
    TypeVariable s = parameters.get(0);
    TypeVariable t = parameters.get(1);
    TypeVariable u = parameters.get(2);
    TypeVariable r = parameters.get(3);
    Substitution substitution =
        new Substitution(Arrays.asList(s, t, u, r), JavaTypes.STRING_TYPE, u, t, r);
    // T and U occur in replacements, so they stand for themselves.  R is a sibling of S, which is
    // instantiated, so R is an unknown.
    assertFalse(substitution.isUnknown(t));
    assertFalse(substitution.isUnknown(u));
    assertTrue(substitution.isUnknown(r));

    Substitution once = substitution.withIdentityFor(Arrays.asList(t, u));
    assertTrue(once.mapsToItself(t));
    assertTrue(once.mapsToItself(u));
    assertFalse(once.isUnknown(t));
    assertFalse(once.isUnknown(u));
    assertTrue(once.isUnknown(r));
    // The classification survives copying.
    Substitution copy = new Substitution(once);
    assertFalse(copy.isUnknown(t));
    assertFalse(copy.isUnknown(u));
    assertTrue(copy.isUnknown(r));
  }

  /**
   * {@link Substitution#withIdentityFor} does not make a sibling of a type parameter that it maps
   * to itself stand for itself.
   */
  @Test
  public void withIdentityForKeepsInstantiatedDeclarationTest() {
    List<TypeVariable> parameters = GenericClassType.forClass(Siblings.class).getTypeParameters();
    TypeVariable s = parameters.get(0);
    TypeVariable t = parameters.get(1);
    Substitution substitution = new Substitution(Arrays.asList(s, t), JavaTypes.STRING_TYPE, t);
    assertTrue(substitution.isUnknown(t));
    Substitution once = substitution.withIdentityFor(Arrays.asList(s));
    assertTrue(once.mapsToItself(s));
    // S stands for itself, and T is still an unknown.
    assertFalse(once.isUnknown(s));
    assertTrue(once.isUnknown(t));
  }
}
