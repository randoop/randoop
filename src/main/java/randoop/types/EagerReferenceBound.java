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
    // Not `argType.isParameterized()`:  a member class with no type parameters of its own, such as
    // Outer<String>.Inner, is parameterized but is not an InstantiatedType.  The subtype check
    // below handles it.
    if (argType instanceof InstantiatedType) {
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
      // This bound is a type variable, which the substitution does not instantiate with a
      // non-variable type, so the variable can still be chosen, within its own upper bound.
      TypeVariable thisBoundVariable = (TypeVariable) thisBoundType;
      ParameterBound variableUpperBound = thisBoundVariable.getUpperTypeBound();
      if (subst.get(thisBoundVariable) != null || !(argType instanceof ReferenceType)) {
        // One of two cases holds:
        //  * The substitution maps `thisBoundVariable` (for example, to itself), so the
        //    substitution cannot be extended with a choice for `thisBoundVariable`.
        //  * `argType` is not a reference type, so `argType` cannot instantiate a type variable.
        // In either case, `argType` is within this bound if it is within `variableUpperBound`,
        // which `thisBoundVariable` can be.
        return variableUpperBound.isUpperBound(argType, subst);
      }
      // `variableUpperBound` can mention `thisBoundVariable` itself, as in `<T extends
      // Comparable<? super T>>`, so checking it requires a choice for `thisBoundVariable`.
      // `argType` is within this bound if `thisBoundVariable` can be some supertype S of `argType`
      // that is within `variableUpperBound` when `thisBoundVariable` is S.  Try `argType` itself
      // first.  If it fails, try its other supertypes:  for `<T extends Marker<T>>`, if Child
      // extends Base and Base implements Marker<Base>, then T can be Base though not Child.
      ReferenceType argRefType = (ReferenceType) argType;
      if (variableUpperBound.isUpperBound(
          argRefType, subst.extend(new Substitution(thisBoundVariable, argRefType)))) {
        return true;
      }
      // If `variableUpperBound` does not mention `thisBoundVariable`, then no supertype of
      // `argType` is within `variableUpperBound`, because `argType` is not.
      if (!(argRefType instanceof ClassOrInterfaceType)
          || !variableUpperBound.getTypeParameters().contains(thisBoundVariable)) {
        return false;
      }
      for (ClassOrInterfaceType supertype :
          ((ClassOrInterfaceType) argRefType).getSupertypesStrict()) {
        if (variableUpperBound.isUpperBound(
            supertype, subst.extend(new Substitution(thisBoundVariable, supertype)))) {
          return true;
        }
      }
      return false;
    }
    // Not `thisBoundType.isParameterized()`:  a member class with no type parameters of its own,
    // such as Outer<String>.Inner, is parameterized but is not an InstantiatedType.  The subtype
    // check below handles it.
    if (thisBoundType instanceof InstantiatedType) {
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
