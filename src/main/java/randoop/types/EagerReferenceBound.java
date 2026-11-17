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
    ReferenceType thisBoundType = this.getBoundType().substitute(subst);
    if (thisBoundType.equals(JavaTypes.NULL_TYPE)) {
      return true;
    }
    if (thisBoundType.isVariable()) {
      return thisBoundType.isSubtypeOfOrEqualTo(argType);
    }
    // A member class that declares no type parameters, of a parameterized type, such as
    // Outer<String>.Inner, is parameterized but is not an InstantiatedType, and neither is its
    // capture conversion.  It is compared by a subtype test.
    if (argType instanceof NonParameterizedType && argType.isParameterized()) {
      if (!(thisBoundType instanceof ClassOrInterfaceType)) {
        return false;
      }
      return Subtyping.isSubtypeOrEqual(thisBoundType, (ReferenceType) argType, subst);
    }
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
    if (thisBoundType.isVariable() || argType.isVariable()) {
      if (!argType.isReferenceType()) {
        return false;
      }
      ReferenceType argReferenceType = (ReferenceType) argType;
      if (thisBoundType.isVariable() && thisBoundType.equals(getBoundType())) {
        // The substitution does not replace the type variable.  Mapping it to itself makes it an
        // unknown if it is a type parameter of a declaration that the substitution instantiates,
        // such as a sibling of the type parameter whose bound this is.  A substitution that
        // instantiates no type parameter, such as an empty one or one that maps only a capture
        // variable, does not say which type variables are fixed, so the type variable is an
        // unknown then too.
        Substitution completeSubstitution = subst.extendWithIdentity(getTypeParameters());
        if (!subst.instantiatesTypeParameters() || completeSubstitution.isUnknown(thisBoundType)) {
          // Checking argType against the upper bound of the type variable instead would leave the
          // type variable unknown within that bound:  for `T extends Comparable<? super T>`, it
          // would accept Comparable<String>, which no choice of T is a supertype of.  The bounds of
          // the type variable are checked with the mappings of `subst`, because they can mention
          // other type variables that `subst` instantiates.
          return ((TypeVariable) thisBoundType).canBeSupertypeOf(argReferenceType, subst);
        }
        // The type variable stands for itself, such as a type parameter of an enclosing class.
        return Subtyping.isSubtypeOrEqual(argReferenceType, thisBoundType, completeSubstitution);
      }
      // A type variable that the substitution put in the bound stands for itself.  A type variable
      // argument satisfies this bound if its own upper bound does.
      return isUpperBound(new EagerReferenceBound(argReferenceType), subst);
    }
    // As in isLowerBound, a parameterized type that is not an InstantiatedType, such as
    // Outer<String>.Inner, is compared by a subtype test.
    if (thisBoundType instanceof NonParameterizedType && thisBoundType.isParameterized()) {
      if (!(argType instanceof ClassOrInterfaceType)) {
        return false;
      }
      return Subtyping.isSubtypeOrEqual((ReferenceType) argType, thisBoundType, subst);
    }
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
