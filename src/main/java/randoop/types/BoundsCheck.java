package randoop.types;

import java.util.List;

/** Predicate to check that a substitution instantiates type variables within their bounds. */
public class BoundsCheck {
  /** The type variables to check against. */
  private final List<TypeVariable> typeParameters;

  /**
   * Creates a {@link BoundsCheck} predicate for a given list of type parameters.
   *
   * @param typeParameters the list of type parameters to be checked by the predicate
   */
  public BoundsCheck(List<TypeVariable> typeParameters) {
    this.typeParameters = typeParameters;
  }

  /**
   * Checks if the substitution instantiates each type parameter to a type within the parameter's
   * bounds, after the substitution has been applied to the bounds.
   *
   * <p>Requires that the substitution instantiate each type parameter of this object. It need not
   * instantiate the other type variables that occur in their bounds: such a type variable stands
   * for an unknown type, which may be any type that satisfies its own bounds. For example, for
   * {@code <T extends Comparable<U>, U>}, a substitution that maps {@code T} to {@code String} and
   * does not map {@code U} satisfies the bound of {@code T}, because {@code String} implements
   * {@code Comparable<String>}. A client that instantiates those type variables later should check
   * the bounds again.
   *
   * @param substitution substitution for checking bounds
   * @return true if each type parameter's instantiation is within the bounds of the type parameter,
   *     after the bounds have been substituted
   * @throws IllegalArgumentException if the substitution does not instantiate a type parameter of
   *     this object
   */
  public boolean test(Substitution substitution) {
    for (TypeVariable paramType : typeParameters) {
      ReferenceType argType = substitution.get(paramType);
      if (argType == null) {
        throw new IllegalArgumentException(
            "substitution " + substitution + " does not instantiate " + paramType);
      }
      if (!(paramType.getLowerTypeBound().isLowerBound(argType, substitution)
          && paramType.getUpperTypeBound().isUpperBound(argType, substitution))) {
        return false;
      }
    }
    return true;
  }
}
