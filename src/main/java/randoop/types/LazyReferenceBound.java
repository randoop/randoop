package randoop.types;

import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A lazy representation of a type bound in which a type variable occurs. Similar in purpose to
 * {@link LazyParameterBound}, but this class uses {@link ReferenceType} as the bound instead of
 * {@code java.lang.reflect.Type}. Its methods that test bounds compare a type with the bound type
 * as an {@link EagerReferenceBound} would, except when the bound type is a type variable that the
 * substitution does not instantiate.
 *
 * <p>Objects of this class are created by {@link
 * ReferenceBound#forTypeLazyIfGeneric(ReferenceType)} when the bound type contains a type variable.
 * A substitution leaves a bound unresolved if it maps the element type of an array in the bound,
 * such as {@code T[]} or {@code List<T[]>}, to the null type, because there is no array of the null
 * type.
 */
class LazyReferenceBound extends ReferenceBound {

  /**
   * Creates a bound for the given reference type.
   *
   * @param boundType the bound type
   */
  LazyReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  @Override
  public ReferenceBound substitute(Substitution substitution) {
    if (substitution.isEmpty()) {
      return this;
    }
    // There is no array of the null type, so leave the bound unresolved.
    if (hasArrayOfNullType(getBoundType(), substitution)) {
      return this;
    }

    ReferenceType referenceType = getBoundType().substitute(substitution);
    if (isUnchangedBy(referenceType)) {
      return this;
    }
    return ReferenceBound.forTypeLazyIfGeneric(referenceType);
  }

  /**
   * Returns true if applying the substitution to the given type would create an array of the null
   * type: that is, if the type has an array type, possibly nested in a type argument, whose element
   * type is a type variable that the substitution maps to the null type. An example is {@code
   * List<T[]>} with {@code T} mapped to the null type.
   *
   * @param type a type
   * @param substitution a type substitution
   * @return true if substituting into {@code type} would create an array of the null type
   */
  private static boolean hasArrayOfNullType(Type type, Substitution substitution) {
    if (type.isArray()) {
      Type elementType = ((ArrayType) type).getElementType();
      return (elementType.isVariable()
              && JavaTypes.NULL_TYPE.equals(substitution.get((TypeVariable) elementType)))
          || hasArrayOfNullType(elementType, substitution);
    }
    if (type instanceof InstantiatedType) {
      InstantiatedType instantiatedType = (InstantiatedType) type;
      for (TypeArgument argument : instantiatedType.getTypeArguments()) {
        if (argument instanceof ReferenceArgument
            && hasArrayOfNullType(
                ((ReferenceArgument) argument).getReferenceType(), substitution)) {
          return true;
        }
      }
      ClassOrInterfaceType enclosingType = instantiatedType.enclosingType;
      return enclosingType != null && hasArrayOfNullType(enclosingType, substitution);
    }
    return false;
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    return getBoundType().getTypeParameters();
  }

  /**
   * {@inheritDoc}
   *
   * <p>The bound type, after substitution, can contain type variables that the substitution does
   * not instantiate. It is compared as an {@link EagerReferenceBound} would be, except as described
   * in {@link #getUnknownVariable(Type, Substitution)}.
   */
  @Override
  public boolean isLowerBound(Type argType, Substitution substitution) {
    TypeVariable unknown = getUnknownVariable(argType, substitution);
    if (unknown != null) {
      // The unknown type variable can be argType itself.  Its bounds are checked with the mappings
      // of the substitution, because they can mention type variables that it instantiates.
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
   * <p>The bound type, after substitution, can contain type variables that the substitution does
   * not instantiate. It is compared as an {@link EagerReferenceBound} would be, except as described
   * in {@link #getUnknownVariable(Type, Substitution)}. For an upper bound, the uninstantiated type
   * variable can be {@code argType} or any of its supertypes.
   */
  @Override
  public boolean isUpperBound(Type argType, Substitution substitution) {
    TypeVariable unknown = getUnknownVariable(argType, substitution);
    if (unknown != null) {
      // The unknown type variable can be argType itself.  Its bounds are checked with the mappings
      // of the substitution, because they can mention type variables that it instantiates.
      if (unknown.canBeInstantiatedBy((ReferenceType) argType, substitution)) {
        return true;
      }
      // The unknown type variable can also be any supertype of argType.
      if (argType instanceof ClassOrInterfaceType) {
        for (ClassOrInterfaceType supertype :
            ((ClassOrInterfaceType) argType).getSupertypesStrict()) {
          if (unknown.canBeInstantiatedBy(supertype, substitution)) {
            return true;
          }
        }
      }
      return false;
    }
    return new EagerReferenceBound(getBoundType()).isUpperBound(argType, substitution);
  }

  /**
   * Returns the type variable that the bound type is after the given substitution is applied, if
   * the substitution does not instantiate it and {@code argType} is a reference type that is not a
   * variable. Otherwise, returns null.
   *
   * <p>The substitution does not determine the type that the returned type variable will be. This
   * class treats {@code argType} as within this bound if the type variable can be instantiated by
   * {@code argType}, with the other type variables instantiated as by the substitution, because
   * instantiating the type variable by {@code argType} would put {@code argType} within this bound.
   * Comparing {@code argType} with the bounds of the type variable instead would accept any type as
   * within a lower bound, since the lower bound of the type variable is usually the null type.
   *
   * @param argType a type to compare with this bound
   * @param substitution a type substitution
   * @return the uninstantiated type variable that the bound type is, or null
   */
  private @Nullable TypeVariable getUnknownVariable(Type argType, Substitution substitution) {
    if (!getBoundType().isVariable() || !argType.isReferenceType() || argType.isVariable()) {
      return null;
    }
    ReferenceType boundType = getBoundType().substitute(substitution);
    return boundType.isVariable() ? (TypeVariable) boundType : null;
  }

  /**
   * {@inheritDoc}
   *
   * <p>If the type of this bound is a capture variable, compares {@code lowerBound} with it as an
   * {@link EagerReferenceBound} would, which accounts for the lower bound of the capture variable,
   * as for a capture of {@code ? super String}. Otherwise, returns true only if {@code lowerBound}
   * is the null type, because the type of this bound contains type variables.
   */
  @Override
  boolean hasLowerBound(ParameterBound lowerBound, Substitution substitution) {
    if (getBoundType() instanceof CaptureTypeVariable) {
      return super.hasLowerBound(lowerBound, substitution);
    }
    return lowerBound.isNullType(substitution);
  }
}
