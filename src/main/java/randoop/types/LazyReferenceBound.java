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
 * {@code java.lang.reflect.Type}. Its methods that test bounds compare a type with the bound type
 * as an {@link EagerReferenceBound} would, except when the bound type is a type variable that the
 * substitution does not replace by another type.
 *
 * <p>The bounds of a type variable in the bound type might refer back to this bound. This class
 * behaves like {@link EagerReferenceBound}, except that {@link #getTypeParameters()} does not
 * examine the bounds of those type variables, and {@link #substitute(Substitution)} does not
 * substitute into this bound again when it reaches this bound through those type variables. Either
 * would otherwise result in nonterminating calls.
 *
 * <p>Objects of this class are created by {@link
 * ReferenceBound#forTypeLazyIfGeneric(ReferenceType)} when the bound type contains a type variable.
 */
class LazyReferenceBound extends EagerReferenceBound {

  /** The bounds that {@link #substitute(Substitution)} is currently substituting into. */
  private static final ThreadLocal<Set<LazyReferenceBound>> beingSubstituted =
      ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

  /**
   * Creates a lazy bound with the given type.
   *
   * @param boundType the type of this bound
   */
  /*package*/ LazyReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  @Override
  public EagerReferenceBound substitute(Substitution substitution) {
    if (substitution.isEmpty()) {
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

    if (Type.sameType(referenceType, getBoundType())) {
      return this;
    }
    return forTypeLazyIfGeneric(referenceType);
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
          addTypeVariables(((WildcardArgument) argument).getTypeBound(), parameters);
        }
      }
    } else if (type instanceof WildcardType) {
      // A substitution can replace a type variable by a wildcard type, such as `? extends T`.
      addTypeVariables(((WildcardType) type).getTypeBound(), parameters);
    }
  }

  /**
   * Adds the type variables that occur in the given wildcard bound to {@code parameters}. Does not
   * examine the bounds of those type variables, which might refer back to this bound.
   *
   * @param bound the bound of a wildcard
   * @param parameters the set to which to add the type variables
   */
  private static void addTypeVariables(ParameterBound bound, Set<TypeVariable> parameters) {
    if (bound instanceof ReferenceBound) {
      addTypeVariables(((ReferenceBound) bound).getBoundType(), parameters);
    } else {
      parameters.addAll(bound.getTypeParameters());
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
    TypeVariable unknown = getUnknownTypeVariable(argType, substitution);
    if (unknown != null) {
      // The unknown type variable can be argType itself.  Its bounds are checked with the mappings
      // of the substitution, because they can mention type variables that it instantiates.
      return unknown.canBeInstantiatedBy((ReferenceType) argType, substitution);
    }
    if (argType.isReferenceType() && substitution.instantiatesTypeParameters()) {
      // The bound type can contain unknowns, such as U in Outer<U>.Inner, where U is a sibling of
      // a type parameter that the substitution instantiates.  The bound is a lower bound if some
      // choice for the unknowns makes it one:  for `U extends Number`, Outer<U>.Inner is a lower
      // bound of Outer<Integer>.Inner, but not of Outer<String>.Inner.
      Substitution completeSubstitution = substitution.extendWithIdentity(getTypeParameters());
      ReferenceType boundType = getBoundType().substitute(completeSubstitution);
      if (Subtyping.hasUnknown(boundType, completeSubstitution)) {
        return Subtyping.canBeSubtype(boundType, (ReferenceType) argType, completeSubstitution);
      }
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
    TypeVariable unknown = getUnknownTypeVariable(argType, substitution);
    if (unknown != null) {
      // The unknown type variable can be argType itself, one of its supertypes, or its lower
      // bound.  Its bounds are checked with the mappings of the substitution, because they can
      // mention type variables that it instantiates.
      return unknown.canBeSupertypeOf((ReferenceType) argType, substitution);
    }
    return super.isUpperBound(argType, substitution);
  }

  /**
   * Returns the bound type after the given substitution is applied, if the bound type is a type
   * variable, the substitution maps it to no type or to itself (as determined by {@link
   * Substitution#mapsToItself(TypeVariable)}), the resulting type variable stands for an unknown
   * type rather than for itself (as determined by {@link Substitution#isUnknown(ReferenceType)}, or
   * because the substitution instantiates no type parameter), and {@code argType} is a reference
   * type that is not a type variable. Otherwise, returns null. A type variable that the
   * substitution replaces the bound type with stands for itself, as in {@link EagerReferenceBound}:
   * for {@code K} mapped to {@code U}, {@code String} is not known to be a subtype of {@code U}.
   *
   * <p>The substitution does not determine the type that the returned type variable will be. This
   * class treats {@code argType} as within this bound if the type variable can be instantiated by
   * {@code argType}, with the other type variables instantiated as by the substitution, because
   * instantiating the type variable by {@code argType} would put {@code argType} within this bound.
   * For an upper bound, it also treats {@code argType} as within this bound if a supertype of
   * {@code argType} can instantiate the type variable, or if {@code argType} is a subtype of the
   * lower bound of the type variable; see {@link TypeVariable#canBeSupertypeOf}. For a lower bound,
   * {@link EagerReferenceBound} instead compares {@code argType} with the upper bound of the type
   * variable, so the two classes can differ. For example, for a type variable {@code U extends
   * Integer} as a lower bound, {@link EagerReferenceBound} accepts {@code Number}, because {@code
   * Integer} is a subtype of {@code Number}, but this class does not, because {@code U} cannot be
   * {@code Number}.
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
    // A type variable that the substitution puts in place of the bound type, including a copy of
    // the bound type with other bounds, stands for itself, as in EagerReferenceBound.
    if (((TypeVariable) getBoundType()).getSubstitutedType(substitution) != null) {
      return null;
    }
    ReferenceType boundType = getBoundType().substitute(substitution);
    if (!boundType.isVariable()) {
      return null;
    }
    TypeVariable variable = (TypeVariable) boundType;
    // As in TypeVariable.variableBoundAllows, a type variable is fixed rather than unknown if the
    // substitution instantiates some type parameter and does not make the type variable an
    // unknown, as for a type variable that the substitution put in the bound.  A fixed type
    // variable stands for itself, so it is compared as an EagerReferenceBound would compare it:
    // for bound `K` with `K` mapped to an unrelated type variable `E`, `String` is not known to
    // be a subtype of `E`.
    Substitution completeSubstitution =
        substitution.extendWithIdentity(Collections.singletonList(variable));
    if (substitution.instantiatesTypeParameters() && !completeSubstitution.isUnknown(variable)) {
      return null;
    }
    return variable;
  }

  /**
   * {@inheritDoc}
   *
   * <p>If the type of this bound is a capture variable, compares {@code lowerBound} with it as an
   * {@link EagerReferenceBound} would, which accounts for the lower bound of the capture variable,
   * as for a capture of {@code ? super String}. Otherwise, returns true only if {@code lowerBound}
   * is the null type; or, after the substitution is applied to it, is a subtype of or equal to the
   * type of this bound when every type variable stands for itself, comparing the enclosing types of
   * member classes too; or is an array type whose element type is the null type and that is a
   * subtype of the type of this bound, as {@code NullType[]} is of {@code T[]}. The type of this
   * bound contains type variables, so it stands for every instantiation of them; a type such as
   * {@code List<String>} is not a subtype of each instantiation of {@code List<T>}, but {@code
   * List<T>} and {@code ArrayList<T>}, for the same {@code T}, are. Because the comparison includes
   * enclosing types, {@code Outer<Integer>.Inner<T>} is not a lower bound of {@code
   * Outer<Number>.Inner<T>}. An intersection, such as {@code List<T> & Serializable}, is a lower
   * bound if one of its members is.
   */
  @Override
  boolean hasLowerBound(ParameterBound lowerBound, Substitution substitution) {
    if (getBoundType() instanceof CaptureTypeVariable) {
      return super.hasLowerBound(lowerBound, substitution);
    }
    if (lowerBound.isNullType(substitution)) {
      return true;
    }
    if (lowerBound instanceof IntersectionTypeBound) {
      // An intersection type is a subtype of each supertype of one of its members.
      for (ParameterBound member : ((IntersectionTypeBound) lowerBound).getBounds()) {
        if (hasLowerBound(member, substitution)) {
          return true;
        }
      }
      return false;
    }
    // With an empty substitution, no type variable is an unknown.  Subtyping compares enclosing
    // types too:  GenericOuter<Integer>.GenericInner<U> is not
    // GenericOuter<Number>.GenericInner<U>.
    // A lower bound that is a LazyParameterBound, such as the reflective bound `Comparable<T>` of
    // `T extends Comparable<T>`, is first converted to the equivalent ReferenceBound.
    ReferenceType lowerType;
    if (lowerBound instanceof ReferenceBound) {
      lowerType = ((ReferenceBound) lowerBound).getBoundType().substitute(substitution);
    } else {
      @Nullable ParameterBound b = lowerBound.substitute(substitution);
      if (b instanceof LazyParameterBound) {
        b = ((LazyParameterBound) b).toReferenceBound();
      }
      if (!(b instanceof ReferenceBound)) {
        return false;
      }
      lowerType = ((ReferenceBound) b).getBoundType();
    }
    if (Subtyping.isSubtypeOrEqual(lowerType, getBoundType(), new Substitution())) {
      return true;
    }
    // The null type is a subtype of every reference type, including a type variable, so array
    // covariance makes, for example, `NullType[]` a subtype of `T[]`.
    return lowerType.isArray()
        && ((ArrayType) lowerType).getElementType().equals(JavaTypes.NULL_TYPE)
        && lowerType.isSubtypeOfOrEqualTo(getBoundType());
  }
}
