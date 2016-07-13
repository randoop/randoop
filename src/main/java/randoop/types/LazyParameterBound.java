package randoop.types;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Represents a type bound using reflection types.
 * Note that these objects may have recursive structure.
 */
class LazyParameterBound extends ParameterBound {

  /** the type for this bound */
  private final Type boundType;

  /**
   * Creates a {@code LazyParameterBound} from the given rawtype and type parameters.
   *
   * @param boundType  the reflection type for this bound
   */
  LazyParameterBound(Type boundType) {
    this.boundType = boundType;
  }

  /**
   * {@inheritDoc}
   * @return true if argument is a {@code LazyParameterBound}, and the rawtype
   *         and parameters are identical, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LazyParameterBound)) {
      return false;
    }
    LazyParameterBound b = (LazyParameterBound) obj;
    return this.boundType.equals(b.boundType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(boundType);
  }

  /**
   * {@inheritDoc}
   * @return the name of the type bound
   */
  @Override
  public String toString() {
    return boundType.toString();
  }

  /**
   * {@inheritDoc}
   * This generic type bound is satisfied by a concrete type if the concrete type
   * formed by applying the substitution to this generic bound is satisfied by
   * the concrete type.
   */
  @Override
  public boolean isSatisfiedBy(GeneralType argType, Substitution<ReferenceType> substitution) {
    ReferenceBound b = this.apply(substitution);

    return b.isSatisfiedBy(argType, substitution);
  }

  @Override
  public boolean isSatisfiedBy(GeneralType argType) {
    return true;
  }

  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    return false;
  }

  @Override
  public ReferenceBound apply(Substitution<ReferenceType> substitution) {
    return null;
  }

}
