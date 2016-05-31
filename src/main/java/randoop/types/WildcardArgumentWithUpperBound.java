package randoop.types;

import java.lang.reflect.Type;

/**
 * Represents a wildcard type that has an upper bound (e.g., {@code ? extends T}).
 * See JLS, section 4.5.1
 */
class WildcardArgumentWithUpperBound extends WildcardArgument {

  /**
   * Creates a wildcard argument with an upper bound.
   * Assumes that the type array has a single element as defined in
   * JLS, section 4.5.1
   *
   * @param upperBounds  the upper bound type array
   */
  WildcardArgumentWithUpperBound(Type[] upperBounds) {
    super(ReferenceType.forType(upperBounds[0]));
  }

  /**
   * Creates a wildcard argument with the given reference type as an Upper bound.
   * @param type  the bound type
   */
  private WildcardArgumentWithUpperBound(ReferenceType type) {
    super(type);
  }

  @Override
  public String toString() {
    return "? extends " + this.getBoundType().toString();
  }

  @Override
  public WildcardArgument apply(Substitution<ReferenceType> substitution) {
    return new WildcardArgumentWithUpperBound(getBoundType().apply(substitution));
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
            && this.getBoundType().isSubtypeOf(((WildcardArgument) otherArgument).getBoundType());
  }

  @Override
  public boolean canBeInstantiatedAs(GeneralType generalType, Substitution<ReferenceType> substitution) {
    return getBoundType().isAssignableFrom(generalType);
  }

  @Override
  public boolean hasUpperBound() {
    return true;
  }
}
