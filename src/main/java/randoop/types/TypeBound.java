package randoop.types;

/**
 * Represents general upper bound on a type variable of a parameterized type.
 */
public abstract class TypeBound {
  /**
   * Determines if this is an upper bound for the argument type.
   *
   * @param argType  the concrete argument type
   * @param subst  the substitution
   * @return true if this bound is satisfied by the concrete type when the
   *         substitution is used on the bound, false otherwise
   */
  public abstract boolean isSatisfiedBy(GeneralType argType, Substitution<ReferenceType> subst);

  /**
   * Determines if this object is an upper bound for the argument type using the most stringent
   * relaxation of the criterion used in {@link #isSatisfiedBy(GeneralType, Substitution)} allowed
   * when not using a substitution. The most relaxed form is simply checking assignability of raw
   * types.
   *
   * @param argType  the argument type
   * @return true, if the type satisfies the
   */
  public abstract boolean isSatisfiedBy(GeneralType argType);

  /**
   * Indicates whether this bound is a subtype of the given general type.
   *
   * @param otherType  the general type
   * @return true if this bound is a subtype of the given type
   */
  public abstract boolean isSubtypeOf(GeneralType otherType);

  /**
   * Applies the given substitution to this bound.
   *
   * @param substitution  the type substitution
   * @return a new bound with types replaced as indicated by the substitution
   */
  public abstract TypeBound apply(Substitution<ReferenceType> substitution);

  /**
   * Creates a type bound from a given {@link ReferenceType}.
   *
   * @param type  the reference type
   * @return the type bound with the given type
   */
  public static TypeBound forType(ReferenceType type) {
    if (type instanceof TypeVariable || type instanceof ClassOrInterfaceType) {
      return ParameterBound.forType(type);
    }

    return new ReferenceBound(type);
  }
}
