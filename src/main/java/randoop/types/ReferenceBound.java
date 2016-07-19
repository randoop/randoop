package randoop.types;

import java.util.List;
import java.util.Objects;

/**
 * Represents a bound on a type variable that is a {@link ReferenceType}.
 */
class ReferenceBound extends ParameterBound {

  /** The type for this bound */
  private final ReferenceType boundType;

  /**
   * Creates a bound for the given reference type.
   *
   * @param boundType  the reference boundType
   */
  ReferenceBound(ReferenceType boundType) {
    this.boundType = boundType;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ReferenceBound)) {
      return false;
    }
    ReferenceBound bound = (ReferenceBound) obj;
    return this.boundType.equals(bound.boundType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(boundType);
  }

  @Override
  public String toString() {
    return boundType.toString();
  }

  @Override
  public boolean isUpperBound(GeneralType argType, Substitution<ReferenceType> subst) {
    // XXX in practice, substitution not necessary because doesn't have variables by construction
    ReferenceType boundType = this.boundType.apply(subst);
    if (boundType.equals(ConcreteTypes.OBJECT_TYPE)) {
      return true;
    }
    if (boundType.isParameterized()) {
      if (!(argType instanceof ClassOrInterfaceType)) {
        return false;
      }
      InstantiatedType boundClassType = (InstantiatedType) boundType.applyCaptureConversion();
      InstantiatedType argSuperType =
          (InstantiatedType)
              ((ClassOrInterfaceType) argType)
                  .getMatchingSupertype(boundClassType.getGenericClassType());
      if (argSuperType == null) {
        return false;
      }
      argSuperType = argSuperType.applyCaptureConversion();
      return argSuperType.isInstantiationOf(boundClassType);
    }
    return argType.isSubtypeOf(boundType);
  }

  @Override
  boolean isUpperBound(ParameterBound bound, Substitution<ReferenceType> substitution) {
    return isUpperBound(boundType, substitution);
  }

  @Override
  public boolean isLowerBound(GeneralType argType, Substitution<ReferenceType> subst) {
    // XXX in practice, substitution not necessary because doesn't have variables by construction
    ReferenceType boundType = this.boundType.apply(subst);
    if (boundType.equals(ConcreteTypes.NULL_TYPE)) {
      return true;
    }
    if (argType.isParameterized()) {
      if (!(boundType instanceof ClassOrInterfaceType)) {
        return false;
      }
      InstantiatedType argClassType = (InstantiatedType) argType.applyCaptureConversion();
      InstantiatedType boundSuperType =
          (InstantiatedType)
              ((ClassOrInterfaceType) boundType)
                  .getMatchingSupertype(argClassType.getGenericClassType());
      if (boundSuperType == null) {
        return false;
      }
      boundSuperType = boundSuperType.applyCaptureConversion();
      return boundSuperType.isInstantiationOf(argClassType);
    }
    return boundType.isSubtypeOf(argType);
  }

  @Override
  boolean isLowerBound(ParameterBound bound, Substitution<ReferenceType> substitution) {
    assert bound instanceof ReferenceBound : "only handling reference bounds";
    return isLowerBound(((ReferenceBound) bound).boundType, substitution);
  }

  @Override
  public boolean isSubtypeOf(ParameterBound bound) {
    if (bound instanceof ReferenceBound) {
      return this.boundType.isSubtypeOf(((ReferenceBound) bound).boundType);
    }
    assert false : "not handling ReferenceBound subtype of other bound type";
    return false;
  }

  @Override
  boolean hasWildcard() {
    return boundType.hasWildcard();
  }

  @Override
  public ReferenceBound applyCaptureConversion() {
    return new ReferenceBound(boundType.applyCaptureConversion());
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    return boundType.getTypeParameters();
  }

  @Override
  public ReferenceBound apply(Substitution<ReferenceType> substitution) {
    return new ReferenceBound(boundType.apply(substitution));
  }
}
