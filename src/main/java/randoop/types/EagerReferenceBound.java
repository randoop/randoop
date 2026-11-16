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
  /*package*/ EagerReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  @SuppressWarnings("ReferenceEquality") // detects any change; see Type#substitute
  @Override
  public EagerReferenceBound substitute(Substitution substitution) {
    // There is no array of the null type, so leave the bound unresolved.
    if (createsNullTypeArray(getBoundType(), substitution)) {
      return this;
    }
    ReferenceType referenceType = getBoundType().substitute(substitution);
    // Copies of a type variable are equal even if their bounds differ, so `equals` would not
    // detect a substitution that changes only the bounds of a type variable.
    if (referenceType == getBoundType()) {
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
    if (createsNullTypeArray(getBoundType(), subst)) {
      // There is no array of the null type, so no type satisfies this bound.
      return false;
    }
    ReferenceType thisBoundType = this.getBoundType().substitute(subst);
    if (thisBoundType.equals(JavaTypes.NULL_TYPE)) {
      return true;
    }
    if (thisBoundType.isVariable()) {
      return thisBoundType.isSubtypeOfOrEqualTo(argType);
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
    if (createsNullTypeArray(getBoundType(), subst)) {
      // There is no array of the null type, so no type satisfies this bound.
      return false;
    }
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
        if (variable.canBeSupertypeOf((ReferenceType) argType, subst)) {
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
      // The substitution does not map the variable, so the variable can still be chosen.  argType
      // is within this bound if it is within the variable's upper bound, which the variable can be.
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
