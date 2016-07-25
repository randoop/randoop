package randoop.types;

import java.lang.reflect.Type;

/**
 * Represents a wildcard type that has a lower bound (e.g., {@code ? super T}).
 * See JLS, section 4.5.1
 */
class WildcardArgumentWithLowerBound extends WildcardArgument {

  /**
   * Creates a wildcard argument with a lower bound.
   * Assumes that the type array has a single element as defined in
   * JLS, section 4.5.1
   *
   * @param lowerBounds  the lower bound type array
   */
  WildcardArgumentWithLowerBound(Type[] lowerBounds) {
    super(ParameterBound.forTypes(lowerBounds));
  }

  WildcardArgumentWithLowerBound(ReferenceBound bound) {
    super(bound);
  }

  @Override
  public String toString() {
    return "? super " + this.getTypeBound().toString();
  }

  @Override
  public WildcardArgument apply(Substitution<ReferenceType> substitution) {
    return new WildcardArgumentWithLowerBound((ReferenceBound) getTypeBound().apply(substitution));
  }

  /**
   * {@inheritDoc}
   * Checks for containment cases:
   * <ul>
   *   <li>{@code ? super T} contains {@code ? super S} if {@code S.isSubtypeOf(T)}</li>
   *   <li>{@code ? super T} contains {@code ?}</li>
   *   <li>{@code ? super T} contains {@code ? extends Object}</li>
   * </ul>
   * The last two being equivalent from the perspective of reflection.
   */
  @Override
  public boolean contains(TypeArgument otherArgument) {
    if (otherArgument.isWildcard()) {
      WildcardArgument otherWildcard = (WildcardArgument) otherArgument;
      if (otherWildcard.hasUpperBound()) {
        return otherWildcard.getTypeBound().equals(new ReferenceBound(ConcreteTypes.OBJECT_TYPE));
      } else {
        return otherWildcard.getTypeBound().isSubtypeOf(this.getTypeBound());
      }
    }
    return false;
  }

  @Override
  public boolean hasUpperBound() {
    return false;
  }
}
