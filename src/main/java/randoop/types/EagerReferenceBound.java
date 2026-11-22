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
    // but is not an InstantiatedType.  It is compared by the subtype test below.
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
      // isInstantiationOf does not compare enclosing types, so compare them too.
      return boundSuperType.isInstantiationOf(argClassType)
          && ClassOrInterfaceType.enclosingTypesInstantiate(boundSuperType, argClassType);
    }
    return isSubtypeIncludingEnclosingTypes(thisBoundType, argType);
  }

  /**
   * Returns true if {@code type} is a subtype of {@code otherType}, as determined by {@link
   * Type#isSubtypeOfOrEqualTo(Type)}, and, if {@code otherType} is a member class that declares no
   * type parameters, of a parameterized type, such as {@code Outer<String>.Middle<Integer>.Leaf},
   * the enclosing types of the supertype of {@code type} of that class instantiate those of {@code
   * otherType} at every level. {@link Type#isSubtypeOfOrEqualTo(Type)} compares such types with
   * {@code equals}, which does not compare the enclosing types of the enclosing types.
   *
   * @param type the possible subtype
   * @param otherType the possible supertype
   * @return true if {@code type} is a subtype of {@code otherType}, including its enclosing types
   */
  private static boolean isSubtypeIncludingEnclosingTypes(ReferenceType type, Type otherType) {
    if (!type.isSubtypeOfOrEqualTo(otherType)) {
      return false;
    }
    if (!(type instanceof ClassOrInterfaceType)
        || !(otherType instanceof ClassOrInterfaceType)
        || otherType instanceof InstantiatedType
        || !otherType.isParameterized()) {
      return true;
    }
    ClassOrInterfaceType otherClassType = (ClassOrInterfaceType) otherType;
    ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
    if (!classType.runtimeClassIs(otherClassType.getRuntimeClass())) {
      for (ClassOrInterfaceType supertype : classType.getSupertypesStrict()) {
        if (supertype.runtimeClassIs(otherClassType.getRuntimeClass())) {
          return ClassOrInterfaceType.enclosingTypesInstantiate(supertype, otherClassType);
        }
      }
      return true;
    }
    return ClassOrInterfaceType.enclosingTypesInstantiate(classType, otherClassType);
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
        // The type variable can be argType itself or one of its supertypes, as in
        // LazyReferenceBound.isUpperBound.  Checking argType against the upper bound of the type
        // variable instead would leave the type variable unknown within that bound:  for
        // `T extends Comparable<? super T>`, it would accept Comparable<String>, which no choice of
        // T is a supertype of.  The bounds of the type variable are checked with the mappings of
        // `subst`, because they can mention other type variables that `subst` instantiates.
        return variable.canBeSupertypeOf((ReferenceType) argType, subst);
      }
      // The substitution does not map the variable, so the variable can still be chosen.  argType
      // is within this bound if it is within the variable's upper bound, which the variable can be.
      return variable.getUpperTypeBound().isUpperBound(argType, subst);
    }
    // A non-generic member class of a generic class, as in `Outer<String>.Inner`, is parameterized
    // but is not an InstantiatedType.  It is compared by the subtype test below.
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
      // isInstantiationOf does not compare enclosing types, so compare them too:  for the bound
      // Outer<String>.Inner<Integer>, Outer<Number>.Inner<Integer> is not within the bound.
      return argSuperType.isInstantiationOf(boundClassType)
          && ClassOrInterfaceType.enclosingTypesInstantiate(argSuperType, boundClassType);
    }
    if (argType.isReferenceType()) {
      return isSubtypeIncludingEnclosingTypes((ReferenceType) argType, thisBoundType);
    }
    return argType.isSubtypeOfOrEqualTo(thisBoundType);
  }
}
