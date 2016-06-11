package randoop.types;

import java.lang.reflect.Type;


/**
 * Represents a wildcard type that has a lower bound (e.g., {@code ? super T}).
 * See JLS, section 4.5.1
 */
public class WildcardArgumentWithLowerBound extends WildcardArgument {

  /**
   * Creates a wildcard argument with a lower bound.
   * Assumes that the type array has a single element as defined in
   * JLS, section 4.5.1
   *
   * @param lowerBounds  the lower bound type array
   */
  WildcardArgumentWithLowerBound(Type[] lowerBounds) {
    super(ReferenceType.forType(lowerBounds[0]));
  }

  private WildcardArgumentWithLowerBound(ReferenceType type) {
    super(type);
  }

  @Override
  public String toString() {
    return "? super " + this.getBoundType().toString();
  }

  @Override
  public WildcardArgument apply(Substitution<ReferenceType> substitution) {
    return new WildcardArgumentWithLowerBound(getBoundType().apply(substitution));
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
        return otherWildcard.getBoundType().equals(ConcreteTypes.OBJECT_TYPE);
      } else {
        return otherWildcard.getBoundType().isSubtypeOf(this.getBoundType());
      }
    }
    return false;
  }

  @Override
  public boolean canBeInstantiatedAs(GeneralType generalType, Substitution<ReferenceType> substitution) {
    return generalType.isAssignableFrom(getBoundType());
  }

  @Override
  public boolean hasUpperBound() {
    return false;
  }
}
