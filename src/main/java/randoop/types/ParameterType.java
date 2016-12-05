package randoop.types;

/**
 * An abstract class representing kinds of type parameters, which are either type variables or
 * wildcard types.
 * Manages both upper and lower type bounds.
 */
public abstract class ParameterType extends ReferenceType {

  /** The lower bound on this type */
  private ParameterBound lowerBound;

  /** The upper bound on this type */
  private ParameterBound upperBound;

  ParameterType() {
    this.lowerBound = new EagerReferenceBound(JavaTypes.NULL_TYPE);
    this.upperBound = new EagerReferenceBound(JavaTypes.OBJECT_TYPE);
  }

  ParameterType(ParameterBound lowerBound, ParameterBound upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  @Override
  public String getCanonicalName() {
    return this.getName();
  }

  public ParameterBound getLowerTypeBound() {
    return lowerBound;
  }

  public ParameterBound getUpperTypeBound() {
    return upperBound;
  }

  /**
   * {@inheritDoc}
   *
   * @return null since type variables do not have a runtime class
   */
  @Override
  public Class<?> getRuntimeClass() {
    return null;
  }

  void setUpperBound(ParameterBound upperBound) {
    this.upperBound = upperBound;
  }

  void setLowerBound(ParameterBound lowerBound) {
    this.lowerBound = lowerBound;
  }

  public boolean hasGenericBound() {
    return getUpperTypeBound().isGeneric() || getLowerTypeBound().isGeneric();
  }
}
