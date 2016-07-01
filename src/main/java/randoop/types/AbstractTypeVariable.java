package randoop.types;

/**
 * An abstract class representing type variables including type variables that are
 * type parameters, and those constructed from capture conversion.
 * Assumes a type variable has both upper and lower bounds, which are only formally
 * defined for capture conversion type variables.
 * When there is no explicit lower bound, the type {@link NullReferenceType} is returned.
 */
public abstract class AbstractTypeVariable extends ReferenceType {

  /**
   * {@inheritDoc}
   * @return false, since an uninstantiated type variable may not be assigned to
   */
  @Override
  public boolean isAssignableFrom(GeneralType sourceType) {
    return false;
  }

  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    return super.isSubtypeOf(otherType) || getTypeBound().isSubtypeOf(otherType);
  }

  @Override
  public ReferenceType apply(Substitution<ReferenceType> substitution) {
    ReferenceType type = substitution.get(this);
    if (type != null) {
      return type;
    }
    return this;
  }

  /**
   * Get the upper bound for for this type variable.
   *
   * @return the (upper) {@link TypeBound} for this type variable
   */
  public abstract TypeBound getTypeBound();

  /**
   * Get the lower bound for this type variable.
   *
   * @return {@link NullReferenceType} in default case since no lower bound is defined
   */
  public ReferenceType getLowerTypeBound() {
    return ConcreteTypes.NULL_TYPE;
  }
}
