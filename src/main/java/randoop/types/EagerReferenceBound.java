package randoop.types;

import java.util.ArrayList;
import java.util.Collections;
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
    // Copies of a type variable are equal even if their bounds differ, so compare the bounds too.
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
    if (thisBoundType.isVariable() || argType.isVariable()) {
      if (!argType.isReferenceType()) {
        return false;
      }
      ReferenceType argReferenceType = (ReferenceType) argType;
      if (thisBoundType.isVariable() && thisBoundType.equals(getBoundType())) {
        // The substitution does not replace the type variable, so it stands for an unknown type.
        // Checking argType against the upper bound of the type variable instead would leave the
        // type variable unknown within that bound:  for `T extends Comparable<? super T>`, it would
        // accept Comparable<String>, which no choice of T is a supertype of.  The bounds of the
        // type variable are checked with the mappings of `subst`, because they can mention other
        // type variables that `subst` instantiates.
        return canBeSupertype((TypeVariable) thisBoundType, argReferenceType, subst);
      }
      // A type variable that the substitution put in the bound stands for itself.  A type variable
      // argument satisfies this bound if its own upper bound does.
      return isUpperBound(new EagerReferenceBound(argReferenceType), subst);
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

  /**
   * Returns true if the unknown type that the given type variable stands for can be a supertype of,
   * or equal to, the given type: that is, if the type variable can be instantiated by the type, by
   * its lower bound, if the type is a subtype of it, or, for a type parameter, by one of the type's
   * supertypes. A capture variable stands for a particular type, which only its lower bound is
   * known to be a supertype of.
   *
   * @param variable a type variable that stands for an unknown type
   * @param type a type
   * @param substitution a substitution for the other type variables in the bounds of {@code
   *     variable}
   * @return true if {@code variable} can be instantiated by a supertype of {@code type}
   */
  private static boolean canBeSupertype(
      TypeVariable variable, ReferenceType type, Substitution substitution) {
    if (type.equals(variable) || variable.canBeInstantiatedBy(type, substitution)) {
      return true;
    }
    if (variable instanceof ExplicitTypeVariable) {
      if (type instanceof ClassOrInterfaceType) {
        // The type variable can be a strict supertype of the type.  For `<S extends
        // Comparable<S>, U extends S>`, a class Sub that extends Base, where Base implements
        // Comparable<Base>, satisfies the bound of U, with Base for S.
        for (ClassOrInterfaceType supertype : ((ClassOrInterfaceType) type).getSupertypesStrict()) {
          if (variable.canBeInstantiatedBy(supertype, substitution)) {
            return true;
          }
        }
      } else if (type.isVariable()) {
        // A supertype of a type variable's upper bound is a supertype of the type variable.  For
        // `X extends Comparable<X>` and `V extends Integer`, X can be Integer.  This recursion
        // terminates, because the bounds of type variables cannot form a cycle of type variables.
        for (ReferenceType boundType : upperBoundTypes((TypeVariable) type)) {
          if (canBeSupertype(variable, boundType, substitution)) {
            return true;
          }
        }
      }
    }
    ParameterBound lowerBound = variable.getLowerTypeBound();
    if (lowerBound instanceof ReferenceBound) {
      ReferenceType lowerBoundType = ((ReferenceBound) lowerBound).getBoundType();
      return !lowerBoundType.equals(JavaTypes.NULL_TYPE)
          && type.isSubtypeOfOrEqualTo(lowerBoundType)
          && variable.canBeInstantiatedBy(lowerBoundType, substitution);
    }
    return false;
  }

  /**
   * Returns the types in the resolved upper bound of the given type variable: the bound type, or
   * each member type of an intersection bound.
   *
   * @param variable a type variable
   * @return the types in the upper bound of {@code variable}
   */
  private static List<ReferenceType> upperBoundTypes(TypeVariable variable) {
    ParameterBound bound = variable.getResolvedUpperTypeBound();
    List<ParameterBound> bounds =
        bound instanceof IntersectionTypeBound
            ? ((IntersectionTypeBound) bound).getBounds()
            : Collections.singletonList(bound);
    List<ReferenceType> result = new ArrayList<>(bounds.size());
    for (ParameterBound b : bounds) {
      if (b instanceof ReferenceBound) {
        ReferenceType boundType = ((ReferenceBound) b).getBoundType();
        if (!boundType.isObject()) {
          result.add(boundType);
        }
      }
    }
    return result;
  }
}
