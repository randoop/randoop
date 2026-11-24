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
   * The (bound type, argument type) pairs whose {@link #isUpperBound(Type, Substitution)} check of
   * a parameterized bound is executing on the current thread. The check captures the bound, and a
   * capture variable's upper bound can be the bound itself, as for {@code T extends Builder<?>} in
   * {@code Builder<T extends Builder<?>>}. Checking that the argument instantiates the capture
   * variable then repeats the same check. The repeated check returns true: it succeeds if the rest
   * of the original check succeeds.
   *
   * <p>Checks are compared by {@code equals}, which ignores the bounds of a type variable. They
   * cannot be compared by identity: when a bound mentions a type variable, as in {@code D<T, S
   * extends D<T, ?>>}, each repetition of a check has a new bound object, so a check compared by
   * identity would never repeat.
   */
  @SuppressWarnings("nullness:type.argument") // withInitial: get() never returns null
  private static final ThreadLocal<Set<List<Type>>> upperBoundChecksInProgress =
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
    // A member class that declares no type parameters, of a parameterized type, such as
    // Outer<String>.Inner, is parameterized but is not an InstantiatedType, and neither is its
    // capture conversion.  It is compared by a subtype test, which compares the enclosing types by
    // containment, as isSubtypeOfOrEqualTo does not.  The type variables in argType, which the
    // substitution does not apply to, stand for themselves.
    if (isMemberOfParameterizedType(argType)) {
      if (!(thisBoundType instanceof ClassOrInterfaceType)) {
        return false;
      }
      return Subtyping.isSubtypeOrEqual(thisBoundType, (ReferenceType) argType, new Substitution());
    }
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
    if (thisBoundType.isVariable() || argType.isVariable()) {
      if (!argType.isReferenceType()) {
        return false;
      }
      ReferenceType argReferenceType = (ReferenceType) argType;
      // Test whether the substitution replaces the type variable, rather than whether the result
      // equals it:  a copy of the type variable with other bounds is equal to it, but if the
      // substitution maps the type variable to the copy, the copy stands for itself.  The result is
      // also a copy if the substitution does not replace the type variable but changes its bounds.
      if (thisBoundType.isVariable()
          && getBoundType().isVariable()
          && ((TypeVariable) getBoundType()).getSubstitutedType(subst) == null) {
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
    // Outer<String>.Inner, is compared by a subtype test.  The subtype test also handles an argType
    // that is a type variable, through its upper bound.  A type variable in the enclosing type of
    // the bound, such as U in Outer<U>.Inner, can be an unknown, such as a sibling of the type
    // parameter whose bound this is, so the substitution is completed with identity mappings.
    // The subtype test compares the enclosing types by containment;
    // ReferenceType.isSubtypeOfOrEqualTo would compare them with equals.
    if (isMemberOfParameterizedType(thisBoundType)) {
      if (!argType.isReferenceType()) {
        return false;
      }
      Substitution completeSubstitution =
          subst.extendWithIdentity(thisBoundType.getTypeParameters());
      return Subtyping.isSubtypeOrEqual(
          (ReferenceType) argType, thisBoundType, completeSubstitution);
    }
    if (thisBoundType instanceof InstantiatedType) {
      if (argType.isVariable()) {
        // A type variable is within this bound if its upper bound is, as for
        // `S extends Comparable<String>` and the bound Comparable<String>.
        return Subtyping.isSubtypeOrEqual((ReferenceType) argType, thisBoundType, subst);
      }
      if (!(argType instanceof ClassOrInterfaceType)) {
        return false;
      }
      List<Type> check = Arrays.asList(thisBoundType, argType);
      Set<List<Type>> inProgress = upperBoundChecksInProgress.get();
      if (!inProgress.add(check)) {
        // This check depends on itself, so assume it holds. Any other condition still holds or
        // fails on its own.
        return true;
      }
      try {
        // As for a type variable bound above, a type parameter in the bound that is fixed, such as
        // a type parameter of an enclosing class, stands for itself:  for `<T extends List<E>>`
        // declared in `Outer<E>`, List<String> is not known to be a List<E>.  The comparison below
        // would accept String as an instantiation of E.
        if (subst.instantiatesTypeParameters()) {
          Substitution completeSubstitution =
              subst.extendWithIdentity(thisBoundType.getTypeParameters());
          if (hasFixedTypeParameter(thisBoundType, completeSubstitution)) {
            return Subtyping.isSubtypeOrEqual(
                (ReferenceType) argType, thisBoundType, completeSubstitution);
          }
        }
        InstantiatedType boundClassType =
            ((InstantiatedType) thisBoundType).applyCaptureConversion();
        InstantiatedType argSuperType =
            ((ClassOrInterfaceType) argType)
                .getMatchingSupertype(boundClassType.getGenericClassType());
        if (argSuperType == null) {
          return false;
        }
        argSuperType = argSuperType.applyCaptureConversion();
        return argSuperType.isInstantiationOf(boundClassType);
      } finally {
        inProgress.remove(check);
      }
    }
    return argType.isSubtypeOfOrEqualTo(thisBoundType);
  }

  /**
   * Returns true if the given type contains a type parameter that stands for itself rather than for
   * an unknown type, as determined by {@link Substitution#isUnknown(ReferenceType)}. Capture
   * variables are not considered.
   *
   * @param type a type
   * @param substitution a substitution that maps each type variable in {@code type}
   * @return true if {@code type} contains a type parameter that stands for itself
   */
  private static boolean hasFixedTypeParameter(ReferenceType type, Substitution substitution) {
    for (TypeVariable variable : type.getTypeParameters()) {
      if (variable instanceof ExplicitTypeVariable && !substitution.isUnknown(variable)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if the given type is a member class that declares no type parameters, of a
   * parameterized type, such as {@code Outer<String>.Inner}. Such a type is parameterized, but is
   * not an {@link InstantiatedType}.
   *
   * @param type a type
   * @return true if {@code type} is a non-generic member class of a parameterized type
   */
  private static boolean isMemberOfParameterizedType(Type type) {
    return type instanceof NonParameterizedType && type.isParameterized();
  }
}
