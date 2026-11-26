package randoop.types;

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
    // argType can be a member class of a parameterized type, such as `Outer<String>.Inner`, which
    // is parameterized but is not an InstantiatedType.  This subtype test compares such types.
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
    // thisBoundType can be a member class of a parameterized type, such as `Outer<String>.Inner`,
    // which is parameterized but is not an InstantiatedType.  This subtype test compares such
    // types.
    return argType.isSubtypeOfOrEqualTo(thisBoundType);
  }
}
