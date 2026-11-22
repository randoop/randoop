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

  @Override
  public EagerReferenceBound substitute(Substitution substitution) {
    ReferenceType referenceType = getBoundType().substitute(substitution);
    if (Type.sameType(referenceType, getBoundType())) {
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
    ReferenceType thisBoundType = this.getBoundType().substitute(subst);
    if (thisBoundType.equals(JavaTypes.OBJECT_TYPE)) {
      return true;
    }
    if (thisBoundType.isVariable()) {
      // The substitution does not map the variable, so the variable can still be chosen.  argType
      // is within this bound if it is within the variable's upper bound, which the variable can be.
      TypeVariable boundVariable = (TypeVariable) thisBoundType;
      ParameterBound variableBound = boundVariable.getUpperTypeBound();
      if (subst.get(boundVariable) != null || !(argType instanceof ReferenceType)) {
        return variableBound.isUpperBound(argType, subst);
      }
      // The bound of `boundVariable` can mention `boundVariable` itself, as in `<T extends
      // Comparable<? super T>>`, so checking it requires a choice for `boundVariable`.  `argType`
      // is within this bound if `boundVariable` can be some supertype S of `argType` that is
      // within the bound of `boundVariable` when `boundVariable` is S.  Try `argType` itself
      // first.  If it fails, try its other supertypes:  for `<T extends Marker<T>>`, if Child
      // extends Base and Base implements Marker<Base>, then T can be Base though not Child.
      ReferenceType argRefType = (ReferenceType) argType;
      if (variableBound.isUpperBound(
          argRefType, subst.extend(new Substitution(boundVariable, argRefType)))) {
        return true;
      }
      // If the bound does not mention `boundVariable`, then no supertype of `argType` satisfies the
      // bound, because `argType` does not.
      if (!(argRefType instanceof ClassOrInterfaceType)
          || !variableBound.getTypeParameters().contains(boundVariable)) {
        return false;
      }
      for (ClassOrInterfaceType supertype :
          ((ClassOrInterfaceType) argRefType).getSupertypesStrict()) {
        if (variableBound.isUpperBound(
            supertype, subst.extend(new Substitution(boundVariable, supertype)))) {
          return true;
        }
      }
      return false;
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
