package randoop.types;

import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A lazy representation of a type bound in which a type variable occurs. Similar in purpose to
 * {@link LazyParameterBound}, but this class uses {@link ReferenceType} as the bound instead of
 * {@code java.lang.reflect.Type}. Its methods that test bounds compare a type with the bound type
 * as an {@link EagerReferenceBound} would, except when the bound type is a type variable that the
 * substitution maps to no type or to a type variable.
 *
 * <p>Objects of this class are created by {@link
 * ReferenceBound#forTypeLazyIfGeneric(ReferenceType)} when the bound type contains a type variable.
 * A substitution leaves the bound unresolved if applying it would create an array of the null type,
 * because there is no such array.
 */
class LazyReferenceBound extends ReferenceBound {

  /**
   * Creates a bound for the given reference type.
   *
   * @param boundType the bound type
   */
  /*package*/ LazyReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  @SuppressWarnings("ReferenceEquality") // detects any change; see Type#substitute
  @Override
  public ReferenceBound substitute(Substitution substitution) {
    if (substitution.isEmpty()) {
      return this;
    }
    // There is no array of the null type, so leave the bound unresolved.
    if (createsNullTypeArray(getBoundType(), substitution)) {
      return this;
    }

    ReferenceType referenceType = getBoundType().substitute(substitution);
    // Copies of a type variable are equal even if their bounds differ, so `equals` would not
    // detect a substitution that changes only the bounds of a type variable.
    if (referenceType == getBoundType()) {
      return this;
    }
    return ReferenceBound.forTypeLazyIfGeneric(referenceType);
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    return getBoundType().getTypeParameters();
  }

  /**
   * {@inheritDoc}
   *
   * <p>The bound type can still contain type variables after the substitution is applied. It is
   * compared as an {@link EagerReferenceBound} would be, except as described in {@link
   * #getUnknownTypeVariable(Type, Substitution)}.
   */
  @Override
  public boolean isLowerBound(Type argType, Substitution substitution) {
    if (createsNullTypeArray(getBoundType(), substitution)) {
      // There is no array of the null type, so no type satisfies this bound.
      return false;
    }
    TypeVariable unknown = getUnknownTypeVariable(argType, substitution);
    if (unknown != null) {
      // The unknown type variable can be argType itself.
      return unknown.canBeInstantiatedBy((ReferenceType) argType, substitution);
    }
    return new EagerReferenceBound(getBoundType()).isLowerBound(argType, substitution);
  }

  @Override
  public boolean isSubtypeOfOrEqualTo(ParameterBound bound) {
    if (bound instanceof ReferenceBound) {
      return getBoundType().isSubtypeOfOrEqualTo(((ReferenceBound) bound).getBoundType());
    }
    assert false : "not handling LazyReferenceBound subtype of other bound type";
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The bound type can still contain type variables after the substitution is applied. It is
   * compared as an {@link EagerReferenceBound} would be, except as described in {@link
   * #getUnknownTypeVariable(Type, Substitution)}.
   */
  @Override
  public boolean isUpperBound(Type argType, Substitution substitution) {
    if (createsNullTypeArray(getBoundType(), substitution)) {
      // There is no array of the null type, so no type satisfies this bound.
      return false;
    }
    TypeVariable unknown = getUnknownTypeVariable(argType, substitution);
    if (unknown != null) {
      // The unknown type variable can be argType itself, or its lower bound.
      return unknown.canBeSupertypeOf((ReferenceType) argType, substitution);
    }
    return new EagerReferenceBound(getBoundType()).isUpperBound(argType, substitution);
  }

  /**
   * Returns the bound type after the given substitution is applied, if the bound type is a type
   * variable, the substitution maps it to no type or to a type variable, and {@code argType} is a
   * reference type that is not a type variable. Otherwise, returns null.
   *
   * <p>The substitution does not determine the type that the returned type variable will be. This
   * class treats {@code argType} as within this bound if the type variable can be instantiated by
   * {@code argType}, with the other type variables instantiated as by the substitution, because
   * instantiating the type variable by {@code argType} would put {@code argType} within this bound.
   * For an upper bound, it also treats {@code argType} as within this bound if {@code argType} is a
   * subtype of the lower bound of the type variable; see {@link TypeVariable#canBeSupertypeOf}. For
   * a lower bound, {@link EagerReferenceBound} instead compares {@code argType} with the upper
   * bound of the type variable, so the two classes can differ. For example, for a type variable
   * {@code U extends Integer} as a lower bound, {@link EagerReferenceBound} accepts {@code Number},
   * because {@code Integer} is a subtype of {@code Number}, but this class does not, because {@code
   * U} cannot be {@code Number}.
   *
   * @param argType a type to compare with this bound
   * @param substitution a type substitution
   * @return the bound type after substitution, if it is a type variable as described above;
   *     otherwise null
   */
  private @Nullable TypeVariable getUnknownTypeVariable(Type argType, Substitution substitution) {
    if (!getBoundType().isVariable() || !argType.isReferenceType() || argType.isVariable()) {
      return null;
    }
    ReferenceType boundType = getBoundType().substitute(substitution);
    return boundType.isVariable() ? (TypeVariable) boundType : null;
  }
}
