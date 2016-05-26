package randoop.types;

/**
 * An abstract class representing type variables including standard type variables and those
 * constructed from capture conversion.
 * Capture conversion type variables have both upper and lower bounds, so apply that interface here.
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
    return super.isSubtypeOf(otherType)
            || getTypeBound().isSubtypeOf(otherType);
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
   * @return the (upper) {@link ParameterBound} for this type variable
   */
  public abstract ParameterBound getTypeBound();

  /**
   * Get the lower bound for this type variable.
   *
   * @return {@link NullReferenceType} in default case since no lower bound is defined
   */
  public ReferenceType getLowerTypeBound() {
    return ConcreteTypes.NULL_TYPE;
  }
}
