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
    // A non-generic member class of a generic class, as in `Outer<String>.Inner`, is parameterized
    // but is not an InstantiatedType.  It is compared by the subtype test below, which requires its
    // enclosing type to equal that of the other member class (or that member class to be the
    // generic form of its class), rather than to be contained in it.  For example, the test does
    // not report that `Outer<Integer>.Inner` is within the bound `Outer<? extends Number>.Inner`.
    if (argType instanceof InstantiatedType) {
      if (!(thisBoundType instanceof ClassOrInterfaceType)) {
        return false;
      }
      InstantiatedType argClassType = ((InstantiatedType) argType).applyCaptureConversion();
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
      return ((TypeVariable) thisBoundType).getUpperTypeBound().isUpperBound(argType, subst);
    }
    // A non-generic member class of a generic class, as in `Outer<String>.Inner`, is parameterized
    // but is not an InstantiatedType.  It is compared by the subtype test below, which requires its
    // enclosing type to equal that of the other member class (or that member class to be the
    // generic form of its class), rather than to be contained in it.  For example, the test does
    // not report that `Outer<Integer>.Inner` is within the bound `Outer<? extends Number>.Inner`.
    if (thisBoundType instanceof InstantiatedType) {
      if (!(argType instanceof ClassOrInterfaceType)) {
        return false;
      }
      InstantiatedType boundClassType = ((InstantiatedType) thisBoundType).applyCaptureConversion();
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
