package randoop.types;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

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
   * The upper-bound checks of a parameterized bound that are executing on the current thread. The
   * check captures the bound, and a capture variable's upper bound can be the bound itself, as for
   * {@code T extends Builder<?>} in {@code Builder<T extends Builder<?>>}. Checking that the
   * argument instantiates the capture variable then repeats the same check. The repeated check
   * returns true: it succeeds if the rest of the original check succeeds.
   *
   * <p>Checks are compared by {@code equals}, which ignores the bounds of a type variable. They
   * cannot be compared by identity: when a bound mentions a type variable, as in {@code D<T, S
   * extends D<T, ?>>}, each repetition of a check has a new bound object, so a check compared by
   * identity would never repeat.
   */
  @SuppressWarnings("nullness:type.argument") // withInitial: get() never returns null
  private static final ThreadLocal<Set<UpperBoundCheck>> upperBoundChecksInProgress =
      ThreadLocal.withInitial(HashSet::new);

  /**
   * A check, by {@link #isUpperBound(Type, Substitution)}, that an argument type is within a
   * parameterized bound type. Two checks are equal if their bound types are equal and their
   * argument types are equal.
   */
  private static final class UpperBoundCheck {

    /** The bound type, after substitution. */
    private final ReferenceType boundType;

    /** The argument type that must be within {@link #boundType}. */
    private final Type argType;

    /**
     * Creates a check that an argument type is within a bound type.
     *
     * @param boundType a bound type, after substitution
     * @param argType the argument type that must be within the bound type
     */
    UpperBoundCheck(ReferenceType boundType, Type argType) {
      this.boundType = boundType;
      this.argType = argType;
    }

    @Override
    public boolean equals(@Nullable Object object) {
      if (this == object) {
        return true;
      }
      if (!(object instanceof UpperBoundCheck)) {
        return false;
      }
      UpperBoundCheck other = (UpperBoundCheck) object;
      return boundType.equals(other.boundType) && argType.equals(other.argType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(boundType, argType);
    }

    @Override
    public String toString() {
      return String.format("UpperBoundCheck{boundType=%s, argType=%s}", boundType, argType);
    }
  }

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
    // argType can be a member class of a parameterized type, such as `Outer<String>.Inner`.  The
    // subtype test compares the enclosing types by subtyping.
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
      if (argType.isReferenceType() && !argType.isVariable()) {
        // `thisBoundVariable` can be argType itself or one of its supertypes, as in
        // LazyReferenceBound.isUpperBound.  Checking argType against the upper bound of
        // `thisBoundVariable` instead would leave `thisBoundVariable` unknown within that bound:
        // for `T extends Comparable<? super T>`, it would accept Comparable<String>, which no
        // choice of T is a supertype of.  The bounds of `thisBoundVariable` are checked with the
        // mappings of `subst`, because they can mention other type variables that `subst`
        // instantiates.
        return thisBoundVariable.canBeSupertypeOf((ReferenceType) argType, subst);
      }
      // `argType` is not a reference type, or is a type variable, so it is not tried as a choice
      // for `thisBoundVariable`.  `argType` is within this bound if it is within the upper bound
      // of `thisBoundVariable`, which `thisBoundVariable` can be.
      return thisBoundVariable.getUpperTypeBound().isUpperBound(argType, subst);
    }
    if (thisBoundType instanceof InstantiatedType) {
      if (!(argType instanceof ClassOrInterfaceType)) {
        return false;
      }
      UpperBoundCheck check = new UpperBoundCheck(thisBoundType, argType);
      Set<UpperBoundCheck> inProgress = upperBoundChecksInProgress.get();
      if (!inProgress.add(check)) {
        // This check depends on itself, so assume it holds. Any other condition still holds or
        // fails on its own.
        return true;
      }
      try {
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
    // thisBoundType can be a member class of a parameterized type, such as `Outer<String>.Inner`.
    // The subtype test compares the enclosing types by subtyping.
    return argType.isSubtypeOfOrEqualTo(thisBoundType);
  }
}
