package randoop.types;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a bound on a type variable where the bound is a {@link ReferenceType} that can be used
 * directly. Contrast with {@link LazyReferenceBound}.
 */
class EagerReferenceBound extends ReferenceBound {

  /**
   * The (bound type, argument type) pairs whose {@link #isLowerBound(Type, Substitution)} check of
   * a parameterized argument type is executing on the current thread. The check captures the
   * argument type, and a capture variable's lower bound can lead back to the same check. For
   * example, checking that {@code C} is a lower bound of {@code G<? super C>}, where {@code C}
   * implements {@code G<G<? super C>>}, checks that {@code G<? super C>} is within the bounds of
   * the capture variable of {@code ? super C}, which checks that {@code C} is a lower bound of
   * {@code G<? super C>} again. The repeated check returns true: it succeeds if the rest of the
   * original check succeeds.
   */
  @SuppressWarnings("nullness:type.argument") // withInitial: get() never returns null
  private static final ThreadLocal<Set<List<Type>>> lowerBoundChecksInProgress =
      ThreadLocal.withInitial(HashSet::new);

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
      List<Type> check = Arrays.asList(thisBoundType, argType);
      Set<List<Type>> inProgress = lowerBoundChecksInProgress.get();
      if (!inProgress.add(check)) {
        // This check depends on itself, so assume it holds. Any other condition still holds or
        // fails on its own.
        return true;
      }
      try {
        InstantiatedType argClassType = ((InstantiatedType) argType).applyCaptureConversion();
        InstantiatedType boundSuperType =
            ((ClassOrInterfaceType) thisBoundType)
                .getMatchingSupertype(argClassType.getGenericClassType());
        if (boundSuperType == null) {
          return false;
        }
        boundSuperType = boundSuperType.applyCaptureConversion();
        return boundSuperType.isInstantiationOf(argClassType);
      } finally {
        inProgress.remove(check);
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
      return argSuperType.isInstantiationOf(boundClassType);
    }
    return argType.isSubtypeOfOrEqualTo(thisBoundType);
  }
}
