package randoop.types;

import java.lang.reflect.Type;

/**
 * An abstract class representing type variables.
 * Assumes a type variable has both upper and lower bounds.
 *
 * @see ExplicitTypeVariable
 * @see CaptureTypeVariable
 */
public abstract class TypeVariable extends ReferenceType {

  /** The lower bound on this type */
  private ParameterBound lowerBound;

  /** The upper bound on this type */
  private ParameterBound upperBound;

  /**
   * Creates a type variable with {@link NullReferenceType} as the lower bound, and
   * the {@code Object} type as upper bound.
   */
  public TypeVariable() {
    this.lowerBound = new ReferenceBound(ConcreteTypes.NULL_TYPE);
    this.upperBound = new ReferenceBound(ConcreteTypes.OBJECT_TYPE);
  }

  /**
   * Creates a type variable with the given type bounds.
   * Assumes the bounds are consistent and does not check for the subtype relationship.
   *
   * @param lowerBound  the lower type bound on this variable
   * @param upperBound  the upper type bound on this variable
   */
  public TypeVariable(ParameterBound lowerBound, ParameterBound upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

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
    return super.isSubtypeOf(otherType) || getUpperTypeBound().isSubtypeOf(otherType);
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
  public ParameterBound getUpperTypeBound() {
    return upperBound;
  }

  /**
   * Get the lower bound for this type variable.
   *
   * @return {@link NullReferenceType} in default case since no lower bound is defined
   */
  public ParameterBound getLowerTypeBound() {
    return lowerBound;
  }

  void setUpperBound(ParameterBound upperBound) {
    this.upperBound = upperBound;
  }

  void setLowerBound(ParameterBound lowerBound) {
    this.lowerBound = lowerBound;
  }

  /**
   * Creates a {@code TypeVariable} object for a given {@code java.lang.reflect.Type}
   * reference, which must be a {@code java.lang.reflect.TypeVariable}.
   *
   * @param type  the type reference
   * @return the {@code TypeVariable} for the given type
   */
  public static TypeVariable forType(Type type) {
    if (!(type instanceof java.lang.reflect.TypeVariable<?>)) {
      throw new IllegalArgumentException("type must be a type variable, got " + type);
    }
    java.lang.reflect.TypeVariable<?> v = (java.lang.reflect.TypeVariable) type;
    return new ExplicitTypeVariable(v, ParameterBound.forTypes(v.getBounds()));
  }
}
