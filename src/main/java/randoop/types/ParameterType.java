package randoop.types;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * An abstract class representing kinds of type parameters, which are either type variables or
 * wildcard types. Manages both upper and lower type bounds.
 */
public abstract class ParameterType extends ReferenceType {

  /** The lower bound on this type. */
  private ParameterBound lowerBound;

  /** The upper bound on this type. */
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
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof ParameterType)) {
      return false;
    }
    ParameterType other = (ParameterType) object;
    return this.lowerBound.equals(other.lowerBound) && this.upperBound.equals(other.upperBound);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lowerBound, upperBound);
  }

  @Override
  public String toString() {
    return "ParameterType [ " + lowerBound + ", " + upperBound + " ]";
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

  @Override
  public List<TypeVariable> getTypeParameters() {
    Set<TypeVariable> parameters = new LinkedHashSet<>();
    parameters.addAll(lowerBound.getTypeParameters());
    parameters.addAll(upperBound.getTypeParameters());
    return new ArrayList<>(parameters);
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

  @Override
  public boolean hasWildcard() {
    return getLowerTypeBound().hasWildcard() || getUpperTypeBound().hasWildcard();
  }

  public boolean hasGenericBound() {
    return getUpperTypeBound().isGeneric() || getLowerTypeBound().isGeneric();
  }
}
