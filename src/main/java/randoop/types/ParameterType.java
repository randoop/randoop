package randoop.types;

import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.CollectionsPlume;
import randoop.main.RandoopBug;

/**
 * An abstract class representing kinds of type parameters, which are either type variables or
 * wildcard types. Manages both upper and lower type bounds.
 */
public abstract class ParameterType extends ReferenceType {

  /** The lower bound on this type. */
  private ParameterBound lowerBound;

  /** The upper bound on this type. */
  private ParameterBound upperBound;

  /**
   * Creates a parameter type whose lower bound is the null type and whose upper bound is Object.
   */
  ParameterType() {
    this.lowerBound = new EagerReferenceBound(JavaTypes.NULL_TYPE);
    this.upperBound = new EagerReferenceBound(JavaTypes.OBJECT_TYPE);
  }

  /**
   * Creates a parameter type with the given bounds.
   *
   * @param lowerBound the lower bound
   * @param upperBound the upper bound
   */
  ParameterType(ParameterBound lowerBound, ParameterBound upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  @Override
  public boolean equals(@Nullable Object object) {
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
  public @Nullable String getCanonicalName() {
    return this.getFqName();
  }

  /**
   * Returns the lower bound on this type.
   *
   * @return the lower bound on this type
   */
  public ParameterBound getLowerTypeBound() {
    return lowerBound;
  }

  /**
   * Returns the upper bound on this type.
   *
   * @return the upper bound on this type
   */
  public ParameterBound getUpperTypeBound() {
    return upperBound;
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    List<TypeVariable> lowerTypeParams = lowerBound.getTypeParameters();
    List<TypeVariable> upperTypeParams = upperBound.getTypeParameters();
    return CollectionsPlume.listUnion(lowerTypeParams, upperTypeParams);
  }

  @Override
  public Class<?> getRuntimeClass() {
    throw new RandoopBug(
        String.format("no run-time class for a type variable %s [%s]", this, this.getClass()));
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns false: type variables are never primitive, boxed primitive, {@code String}, or
   * {@code Class}, and calling the default implementation would invoke {@link #getRuntimeClass()}
   * which throws for type variables.
   */
  @Override
  public boolean isNonreceiverType() {
    return false;
  }

  /**
   * Sets the upper bound of this type parameter.
   *
   * @param upperBound the new upper bound
   */
  void setUpperBound(ParameterBound upperBound) {
    this.upperBound = upperBound;
  }

  /**
   * Sets the lower bound of this type parameter.
   *
   * @param lowerBound the new lower bound
   */
  void setLowerBound(ParameterBound lowerBound) {
    this.lowerBound = lowerBound;
  }

  @Override
  public boolean hasCaptureVariable() {
    return getLowerTypeBound().hasCaptureVariable() || getUpperTypeBound().hasCaptureVariable();
  }

  /**
   * Returns true if this has a generic bound.
   *
   * @return true if this has a generic bound
   */
  public boolean hasGenericBound() {
    return getUpperTypeBound().isGeneric() || getLowerTypeBound().isGeneric();
  }
}
