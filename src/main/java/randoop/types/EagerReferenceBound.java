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
  public boolean isSubtypeOfOrEqualTo(ParameterBound bound) {
    if (bound instanceof EagerReferenceBound) {
      return this.getBoundType().isSubtypeOfOrEqualTo(((EagerReferenceBound) bound).getBoundType());
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
    if (thisBoundType.isVariable() || argType.isVariable()) {
      if (!argType.isReferenceType()) {
        return false;
      }
      ReferenceType argReferenceType = (ReferenceType) argType;
      if (thisBoundType.isVariable() && thisBoundType.equals(getBoundType())) {
        // The substitution does not replace the type variable, so it stands for an unknown type.
        return canBeSupertype((TypeVariable) thisBoundType, argReferenceType);
      }
      // A type variable that the substitution put in the bound stands for itself.  A type variable
      // argument satisfies this bound if its own upper bound does.
      return isUpperBound(new EagerReferenceBound(argReferenceType), subst);
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

  /**
   * Returns true if the unknown type that the given type variable stands for can be a supertype of,
   * or equal to, the given type: that is, if the type variable can be instantiated by the type, by
   * its lower bound, if the type is a subtype of it, or, for a type parameter, by one of the type's
   * supertypes. A capture variable stands for a particular type, which only its lower bound is
   * known to be a supertype of.
   *
   * @param variable a type variable that stands for an unknown type
   * @param type a type
   * @return true if {@code variable} can be instantiated by a supertype of {@code type}
   */
  private static boolean canBeSupertype(TypeVariable variable, ReferenceType type) {
    if (type.equals(variable) || variable.canBeInstantiatedBy(type)) {
      return true;
    }
    if (variable instanceof ExplicitTypeVariable && type instanceof ClassOrInterfaceType) {
      for (ClassOrInterfaceType supertype : ((ClassOrInterfaceType) type).getSupertypesStrict()) {
        if (variable.canBeInstantiatedBy(supertype)) {
          return true;
        }
      }
    }
    ParameterBound lowerBound = variable.getLowerTypeBound();
    if (lowerBound instanceof ReferenceBound) {
      ReferenceType lowerBoundType = ((ReferenceBound) lowerBound).getBoundType();
      return !lowerBoundType.equals(JavaTypes.NULL_TYPE)
          && type.isSubtypeOfOrEqualTo(lowerBoundType)
          && variable.canBeInstantiatedBy(lowerBoundType);
    }
    return false;
  }
}
