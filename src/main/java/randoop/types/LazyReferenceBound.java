package randoop.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A lazy representation of a type bound in which a type variable occurs. Similar in purpose to
 * {@link LazyParameterBound}, but this class uses {@link ReferenceType} as the bound instead of
 * {@code java.lang.reflect.Type}.
 *
 * <p>The bounds of a type variable in the bound type might refer back to this bound. This class
 * behaves like {@link EagerReferenceBound}, except that {@link #getTypeParameters()} does not
 * examine the bounds of those type variables, and {@link #substitute(Substitution)} does not
 * substitute into this bound again when it reaches this bound through those type variables. Either
 * would otherwise result in nonterminating calls. Also, its methods that test bounds treat
 * specially a bound type that is a type variable that the substitution maps to no type or to a type
 * variable.
 *
 * <p>Objects of this class are created by {@link
 * ReferenceBound#forTypeLazyIfGeneric(ReferenceType)} when the bound type contains a type variable.
 * A substitution leaves a bound unresolved if it maps the element type of an array in the bound,
 * such as {@code T[]} or {@code List<T[]>}, to the null type, because there is no array of the null
 * type.
 */
class LazyReferenceBound extends EagerReferenceBound {

  /** The bounds that {@link #substitute(Substitution)} is currently substituting into. */
  private static final ThreadLocal<Set<LazyReferenceBound>> beingSubstituted =
      ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

  /**
   * The bounds that {@link #hasLowerBound(ParameterBound, Substitution)} is currently examining.
   */
  private static final ThreadLocal<Set<LazyReferenceBound>> beingComparedWithLowerBound =
      ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

  /**
   * Creates a lazy bound with the given type.
   *
   * @param boundType the type of this bound
   */
  LazyReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  @Override
  public EagerReferenceBound substitute(Substitution substitution) {
    if (substitution.isEmpty()) {
      return this;
    }
    // There is no array of the null type, so leave the bound unresolved.
    if (hasArrayOfNullType(getBoundType(), substitution)) {
      return this;
    }

    // Substituting into a type variable substitutes into its bounds, which might be this bound.
    Set<LazyReferenceBound> inProgress = beingSubstituted.get();
    if (!inProgress.add(this)) {
      return this;
    }
    ReferenceType referenceType;
    try {
      referenceType = getBoundType().substitute(substitution);
    } finally {
      inProgress.remove(this);
    }

    // Copies of a type variable are equal even if their bounds differ, so compare the bounds too.
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
    Set<TypeVariable> parameters = new LinkedHashSet<>();
    addTypeVariables(getBoundType(), parameters);
    return new ArrayList<>(parameters);
  }

  /**
   * Adds the type variables that occur in the given type to {@code parameters}. Does not examine
   * the bounds of those type variables, which might refer back to this bound.
   *
   * @param type the type
   * @param parameters the set to which to add the type variables
   */
  private static void addTypeVariables(ReferenceType type, Set<TypeVariable> parameters) {
    if (type.isVariable()) {
      parameters.add((TypeVariable) type);
    } else if (type.isArray()) {
      Type componentType = ((ArrayType) type).getComponentType();
      if (componentType.isReferenceType()) {
        addTypeVariables((ReferenceType) componentType, parameters);
      }
    } else if (type instanceof ClassOrInterfaceType) {
      ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
      if (classType.isMemberClass()) {
        addTypeVariables(classType.enclosingType, parameters);
      }
      for (TypeArgument argument : classType.getTypeArguments()) {
        if (argument instanceof ReferenceArgument) {
          addTypeVariables(((ReferenceArgument) argument).getReferenceType(), parameters);
        } else if (argument instanceof WildcardArgument) {
          ParameterBound bound = ((WildcardArgument) argument).getTypeBound();
          if (bound instanceof ReferenceBound) {
            addTypeVariables(((ReferenceBound) bound).getBoundType(), parameters);
          } else {
            parameters.addAll(bound.getTypeParameters());
          }
        }
      }
    }
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
    if (hasArrayOfNullType(getBoundType(), substitution)) {
      // There is no array of the null type, so no type satisfies this bound.
      return false;
    }
    TypeVariable unknown = getUnknownTypeVariable(argType, substitution);
    if (unknown != null) {
      // The unknown type variable can be argType itself.  Its bounds are checked with the mappings
      // of the substitution, because they can mention type variables that it instantiates.
      return unknown.canBeInstantiatedBy((ReferenceType) argType, substitution);
    }
    return super.isLowerBound(argType, substitution);
  }

  /**
   * {@inheritDoc}
   *
   * <p>The bound type can still contain type variables after the substitution is applied. It is
   * compared as an {@link EagerReferenceBound} would be, except as described in {@link
   * #getUnknownTypeVariable(Type, Substitution)}. For an upper bound, the unknown type variable can
   * be {@code argType} or any of its supertypes.
   */
  @Override
  public boolean isUpperBound(Type argType, Substitution substitution) {
    if (hasArrayOfNullType(getBoundType(), substitution)) {
      // There is no array of the null type, so no type satisfies this bound.
      return false;
    }
    TypeVariable unknown = getUnknownTypeVariable(argType, substitution);
    if (unknown != null) {
      // The unknown type variable can be argType itself, its lower bound, or one of argType's
      // supertypes.  Its bounds are checked with the mappings of the substitution, because they can
      // mention type variables that it instantiates.
      return unknown.canBeInstantiatedBySupertypeOf((ReferenceType) argType, substitution);
    }
    return super.isUpperBound(argType, substitution);
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
   * For an upper bound, it is also enough that the type variable can be instantiated by a supertype
   * of {@code argType}, or that {@code argType} is a subtype of the lower bound of the type
   * variable; see {@link TypeVariable#canBeInstantiatedBySupertypeOf}. Comparing {@code argType}
   * with the bounds of the type variable instead would accept any type as within a lower bound,
   * since the lower bound of the type variable is usually the null type.
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

  /**
   * {@inheritDoc}
   *
   * <p>Because the type of this bound contains type variables, returns true only if {@code
   * lowerBound} is the null type, or if the type of this bound is a capture variable and {@code
   * lowerBound} is a lower bound on the capture variable's lower bound. A capture variable stands
   * for a particular type, which is a supertype of its lower bound.
   */
  @Override
  boolean hasLowerBound(ParameterBound lowerBound, Substitution substitution) {
    if (lowerBound.isNullType(substitution)) {
      return true;
    }
    if (!(getBoundType() instanceof CaptureTypeVariable)) {
      return false;
    }
    // The lower bound of the capture variable might refer back to this bound.
    Set<LazyReferenceBound> inProgress = beingComparedWithLowerBound.get();
    if (!inProgress.add(this)) {
      return false;
    }
    try {
      return ((CaptureTypeVariable) getBoundType())
          .getLowerTypeBound()
          .hasLowerBound(lowerBound, substitution);
    } finally {
      inProgress.remove(this);
    }
  }
}
