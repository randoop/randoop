package randoop.types;

import java.util.Objects;

/**
 * Represents an upper bound on a boundType variable that is a {@link ReferenceType}.
 * These should only occur as bounds for {@link CaptureTypeVariable} objects constructed during
 * capture conversion.
 */
class ReferenceBound extends TypeBound {

  /** The boundType for this bound */
  private final ReferenceType boundType;

  /**
   * Creates a bound for the given reference boundType.
   *
   * @param boundType  the reference boundType
   */
  ReferenceBound(ReferenceType boundType) {
    this.boundType = boundType;
  }

  @Override
  public boolean equals(Object obj) {
    if ( !(obj instanceof ReferenceBound)) {
      return false;
    }
    ReferenceBound bound = (ReferenceBound)obj;
    return this.boundType.equals(bound.boundType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(boundType);
  }

  @Override
  public boolean isSatisfiedBy(GeneralType argType, Substitution<ReferenceType> subst) {
    ReferenceType boundType =  this.boundType.apply(subst);
    return boundType.isAssignableFrom(argType);
  }

  @Override
  public boolean isSatisfiedBy(GeneralType argType) {
    return boundType.isAssignableFrom(argType);
  }

  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    return boundType.isSubtypeOf(otherType);
  }

  @Override
  public TypeBound apply(Substitution<ReferenceType> substitution) {
    return new ReferenceBound(boundType.apply(substitution));
  }
}
