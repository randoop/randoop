package randoop.types;

import java.lang.reflect.Type;

/**
 * Represents a wildcard type that has an upper bound (e.g., {@code ? extends T}).
 * See  <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.5.1">JLS Section 4.5.1</a>.
 */
class WildcardArgumentWithUpperBound extends WildcardArgument {

  /**
   * Creates a wildcard argument with an upper bound.
   * Assumes that the type array has a single element as defined in
   *  <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.5.1">JLS Section 4.5.1</a>.
   *
   * @param upperBounds  the upper bound type array
   */
  WildcardArgumentWithUpperBound(Type[] upperBounds) {
    super(ParameterBound.forTypes(upperBounds));
  }

  /**
   * Creates a wildcard argument with the given reference type as an Upper bound.
   * @param bound  the bound type
   */
  WildcardArgumentWithUpperBound(ReferenceBound bound) {
    super(bound);
  }

  @Override
  public String toString() {
    return "? extends " + this.getTypeBound().toString();
  }

  @Override
  public WildcardArgument apply(Substitution<ReferenceType> substitution) {
    return new WildcardArgumentWithUpperBound((ReferenceBound) getTypeBound().apply(substitution));
  }

  /**
   * {@inheritDoc}
   * Checks for containment cases:
   * <ul>
   *   <li>{@code ? extends T} contains {@code ? extends S} if {@code T.isSubtypeOf(S)}</li>
   *   <li>{@code ? extends T} contains {@code ?}</li>
   * </ul>
   * Both of which are technically the same because from a reflection perspective {@code ?} is
   * just {@code ? extends Object}.
   */
  @Override
  public boolean contains(TypeArgument otherArgument) {
    return otherArgument.isWildcard()
        && ((WildcardArgument) otherArgument).hasUpperBound()
        && this.getTypeBound().isSubtypeOf(((WildcardArgument) otherArgument).getTypeBound());
  }

  @Override
  public boolean hasUpperBound() {
    return true;
  }
}
