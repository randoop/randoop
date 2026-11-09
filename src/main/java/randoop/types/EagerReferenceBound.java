package randoop.types;

import java.util.List;

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
    if (isUnchangedBy(referenceType)) {
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
      if (argLowerBound instanceof ReferenceBound) {
        Type argLowerBoundType = ((ReferenceBound) argLowerBound).getBoundType();
        return thisBoundType.isSubtypeOfOrEqualTo(argLowerBoundType);
      }
    }
    return thisBoundType.isSubtypeOfOrEqualTo(argType);
  }

  @Override
  public boolean isSubtypeOfOrEqualTo(ParameterBound bound) {
    if (bound instanceof ReferenceBound) {
      return this.getBoundType().isSubtypeOfOrEqualTo(((ReferenceBound) bound).getBoundType());
    }
    // TODO: change this into an unconditional exception.
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
      TypeVariable variable = (TypeVariable) thisBoundType;
      if (argType.isReferenceType() && !argType.isVariable()) {
        // The type variable can be argType itself, as in LazyReferenceBound.isUpperBound.  Checking
        // argType against the upper bound of the type variable instead would leave the type
        // variable unknown within that bound:  for `T extends Comparable<? super T>`, it would
        // accept Comparable<String>, which no choice of T is a supertype of.  The bounds of the
        // type variable are checked with the mappings of `subst`, because they can mention other
        // type variables that `subst` instantiates.
        if (variable.canBeInstantiatedBy((ReferenceType) argType, subst)) {
          return true;
        }
        // The type variable can also be a strict supertype of argType.  For `<S extends
        // Comparable<S>, U extends S>`, a class Sub that extends Base, where Base implements
        // Comparable<Base>, satisfies the bound of U, with Base for S.
        if (argType instanceof ClassOrInterfaceType) {
          for (ClassOrInterfaceType supertype :
              ((ClassOrInterfaceType) argType).getSupertypesStrict()) {
            if (variable.canBeInstantiatedBy(supertype, subst)) {
              return true;
            }
          }
        }
        return false;
      }
      return variable.getUpperTypeBound().isUpperBound(argType, subst);
    }
    if (thisBoundType.isParameterized()) {
      if (!(argType instanceof ClassOrInterfaceType)) {
        return false;
      }
      InstantiatedType boundClassType = (InstantiatedType) thisBoundType.applyCaptureConversion();
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
