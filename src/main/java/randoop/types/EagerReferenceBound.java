package randoop.types;

import java.util.List;
import randoop.types.LazyParameterBound.LazyBoundException;

/**
 * Represents a bound on a type variable where the bound is a {@link ReferenceType} that can be used
 * directly. Contrast with {@link LazyReferenceBound}.
 */
class EagerReferenceBound extends ReferenceBound {

  /**
   * Creates a bound for the given reference type.
   *
   * @param boundType the reference boundType
   */
  EagerReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  @Override
  public EagerReferenceBound substitute(Substitution substitution) {
    ReferenceType referenceType = getBoundType().substitute(substitution);
    if (referenceType.equals(getBoundType())) {
      return this;
    }
    return new EagerReferenceBound(referenceType);
  }

  @Override
  public EagerReferenceBound applyCaptureConversion() {
    ReferenceType referenceType = getBoundType().applyCaptureConversion();
    if (referenceType.equals(getBoundType())) {
      return this;
    }
    return new EagerReferenceBound(referenceType);
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    return getBoundType().getTypeParameters();
  }

  @Override
  public boolean isLowerBound(Type argType, Substitution subst) {
    ReferenceType thisBoundType = this.getBoundType().substitute(subst);
    if (thisBoundType.equals(JavaTypes.NULL_TYPE)) {
      return true;
    }
    if (thisBoundType.isVariable()) {
      return ((TypeVariable) thisBoundType).getLowerTypeBound().isLowerBound(argType, subst);
    }
    if (argType.isParameterized()) {
      if (!(thisBoundType instanceof ClassOrInterfaceType)) {
        return false;
      }
      InstantiatedType argClassType = (InstantiatedType) argType.applyCaptureConversion();
      InstantiatedType boundSuperType =
          ((ClassOrInterfaceType) thisBoundType)
              .getMatchingSupertype(argClassType.getGenericClassType());
      if (boundSuperType == null) {
        return false;
      }
      boundSuperType = boundSuperType.applyCaptureConversion();
      return boundSuperType.isInstantiationOf(argClassType);
    }
    if (argType instanceof CaptureTypeVariable) {
      WildcardArgument wildcard = ((CaptureTypeVariable) argType).getWildcard();
      if (wildcard.hasUpperBound()) {
        return false;
      }
      ParameterBound argLowerBound = wildcard.getTypeBound();
      if (argLowerBound instanceof EagerReferenceBound) {
        Type argLowerBoundType = ((EagerReferenceBound) argLowerBound).getBoundType();
        return thisBoundType.isSubtypeOf(argLowerBoundType);
      }
    }
    return thisBoundType.isSubtypeOf(argType);
  }

  @Override
  boolean isLowerBound(ParameterBound bound, Substitution substitution) {
    assert bound instanceof EagerReferenceBound : "only handling reference bounds";
    return isLowerBound(((EagerReferenceBound) bound).getBoundType(), substitution);
  }

  @Override
  public boolean isSubtypeOf(ParameterBound bound) {
    if (bound instanceof EagerReferenceBound) {
      return this.getBoundType().isSubtypeOf(((EagerReferenceBound) bound).getBoundType());
    }
    assert false : "not handling EagerReferenceBound subtype of other bound type";
    return false;
  }

  @Override
  public boolean isUpperBound(Type argType, Substitution subst) {
    ReferenceType thisBoundType = this.getBoundType().substitute(subst);
    if (thisBoundType.equals(JavaTypes.OBJECT_TYPE)) {
      return true;
    }
    if (thisBoundType.isVariable()) {
      return ((TypeVariable) thisBoundType).getUpperTypeBound().isUpperBound(argType, subst);
    }
    if (thisBoundType.isParameterized()) {
      if (!(argType instanceof ClassOrInterfaceType)) {
        return false;
      }
      InstantiatedType boundClassType;
      try {
        boundClassType = (InstantiatedType) thisBoundType.applyCaptureConversion();
      } catch (LazyBoundException e) {
        // Capture conversion does not (currently?) work for a lazy bound.
        return false;
      }
      InstantiatedType argSuperType =
          ((ClassOrInterfaceType) argType)
              .getMatchingSupertype(boundClassType.getGenericClassType());
      if (argSuperType == null) {
        return false;
      }
      argSuperType = argSuperType.applyCaptureConversion();
      return argSuperType.isInstantiationOf(boundClassType);
    }
    return argType.isSubtypeOf(thisBoundType);
  }

  @Override
  boolean isUpperBound(ParameterBound bound, Substitution substitution) {
    return isUpperBound(getBoundType(), substitution);
  }
}
