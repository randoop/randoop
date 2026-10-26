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
        return thisBoundType.isSubtypeOfOrEqualTo(argLowerBoundType);
      }
    }
    return thisBoundType.isSubtypeOfOrEqualTo(argType);
  }

  @Override
  boolean isLowerBound(ParameterBound bound, Substitution substitution) {
    if (bound instanceof ReferenceBound) {
      return isLowerBound(((ReferenceBound) bound).getBoundType(), substitution);
    }
    // `bound` is an intersection or a LazyParameterBound.  An intersection is never a type
    // variable's lower bound, and a LazyParameterBound cannot be compared without first
    // substituting for its type variables.
    return false;
  }

  @Override
  public boolean isUpperBound(Type argType, Substitution subst) {
    ReferenceType thisBoundType = this.getBoundType().substitute(subst);
    if (thisBoundType.equals(JavaTypes.OBJECT_TYPE)) {
      return true;
    }
    if (thisBoundType.isVariable()) {
      if (!argType.isReferenceType()) {
        return false;
      }
      // A type variable is an upper bound of itself.  canBeInstantiatedBy does not test this, and
      // it is false for a type variable with a recursive bound such as `W extends Comparable<W>`.
      if (TypeVariable.isSameVariable((ReferenceType) argType, thisBoundType)) {
        return true;
      }
      // The substitution does not replace the type variable, so it stands for an unknown type.
      return ((TypeVariable) thisBoundType).canBeInstantiatedBy((ReferenceType) argType);
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
    return argType.isSubtypeOfOrEqualTo(thisBoundType);
  }
}
