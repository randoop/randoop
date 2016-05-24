package randoop.types;

/**
 * Created by bjkeller on 5/6/16.
 */
public abstract class AbstractTypeVariable extends ReferenceType {

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

  public ReferenceType getLowerTypeBound() {
    return ConcreteTypes.NULL_TYPE;
  }
}
