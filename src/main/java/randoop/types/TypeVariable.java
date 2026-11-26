package randoop.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

/** An abstract class representing type variables. */
public abstract class TypeVariable extends ParameterType {

  /**
   * The checks by {@link #canBeInstantiatedBy(ReferenceType, Substitution)} that are in progress in
   * the current thread, outermost first.
   */
  @SuppressWarnings("nullness:type.argument") // withInitial: get() never returns null
  private static final ThreadLocal<List<InstantiationCheck>> instantiationChecks =
      ThreadLocal.withInitial(ArrayList::new);

  /**
   * The comparisons by {@link #isIdenticalTo(TypeVariable)} that are in progress in the current
   * thread, outermost first. Each element is a pair of the receiver and the argument, compared by
   * object identity.
   */
  @SuppressWarnings("nullness:type.argument") // withInitial: get() never returns null
  private static final ThreadLocal<List<TypeVariable[]>> identityChecks =
      ThreadLocal.withInitial(ArrayList::new);

  /**
   * A check of whether a type variable can be instantiated by a type, with the other type variables
   * instantiated as by a substitution.
   */
  private static final class InstantiationCheck {
    /** The type variable. */
    final TypeVariable variable;

    /** The type that might instantiate the variable. */
    final ReferenceType type;

    /** The substitution for all the type variables, including {@code variable}. */
    final Substitution substitution;

    /**
     * Creates a check of whether a type variable can be instantiated by a type.
     *
     * @param variable a type variable
     * @param type the type that might instantiate the variable
     * @param substitution the substitution for all the type variables, including {@code variable}
     */
    InstantiationCheck(TypeVariable variable, ReferenceType type, Substitution substitution) {
      this.variable = variable;
      this.type = type;
      this.substitution = substitution;
    }

    /**
     * Returns true if this check asks the same question as the given one: the type variables are
     * identical, including their bounds, and the types and substitutions are equal.
     *
     * @param variable a type variable
     * @param type the type that might instantiate the variable
     * @param substitution the substitution for all the type variables, including {@code variable}
     * @return true if this check asks the same question as the given one
     */
    boolean isSameCheck(TypeVariable variable, ReferenceType type, Substitution substitution) {
      return this.type.equals(type)
          && this.substitution.equals(substitution)
          && this.variable.isIdenticalTo(variable);
    }
  }

  /**
   * The type variable that {@link #forType(java.lang.reflect.Type)} returns for each type
   * parameter, with the bounds that the type parameter declares.
   *
   * <p>Reusing these objects keeps finite the structure that {@link #isIdenticalTo(TypeVariable)}
   * compares: {@link LazyParameterBound#getTypeParameters()} returns the type variables of its
   * bound by calling {@link #forType(java.lang.reflect.Type)}, so creating new objects there would
   * create new bounds to compare, without end, for bounds such as {@code <V extends Map<W, A>, W
   * extends List<V>, A extends V>}.
   */
  private static final Map<java.lang.reflect.TypeVariable<?>, TypeVariable> declaredTypeVariables =
      new ConcurrentHashMap<>();

  /**
   * The upper bound of this variable, with references to type variables resolved; null if not yet
   * computed. Use {@link #getResolvedUpperTypeBound()} instead of reading this field.
   */
  private @Nullable ParameterBound resolvedUpperBound = null;

  /**
   * The lower bound of this variable, with references to type variables resolved; null if not yet
   * computed. Use {@link #getResolvedLowerTypeBound()} instead of reading this field.
   */
  private @Nullable ParameterBound resolvedLowerBound = null;

  /**
   * Creates a type variable with {@link NullReferenceType} as the lower bound, and the {@code
   * Object} type as upper bound.
   */
  /*package*/ TypeVariable() {
    super();
  }

  /**
   * Creates a type variable with the given type bounds. Assumes the bounds are consistent and does
   * not check for the subtype relationship.
   *
   * @param lowerBound the lower type bound on this variable
   * @param upperBound the upper type bound on this variable
   */
  /*package*/ TypeVariable(ParameterBound lowerBound, ParameterBound upperBound) {
    super(lowerBound, upperBound);
  }

  /**
   * Creates a {@code TypeVariable} object for a given {@code java.lang.reflect.Type} reference,
   * which must be a {@code java.lang.reflect.TypeVariable}.
   *
   * @param type the type reference
   * @return the {@code TypeVariable} for the given type
   */
  public static TypeVariable forType(java.lang.reflect.Type type) {
    if (!(type instanceof java.lang.reflect.TypeVariable<?>)) {
      throw new IllegalArgumentException("type must be a type variable, got " + type);
    }
    java.lang.reflect.TypeVariable<?> v = (java.lang.reflect.TypeVariable) type;
    TypeVariable result = declaredTypeVariables.get(v);
    if (result != null) {
      return result;
    }
    Set<java.lang.reflect.TypeVariable<?>> variableSet = new HashSet<>(1);
    variableSet.add(v);
    // Computing the bound can call this method for other type variables, so this method does not
    // use ConcurrentHashMap.computeIfAbsent, which forbids such a recursive update.
    result = new ExplicitTypeVariable(v, ParameterBound.forTypes(variableSet, v.getBounds()));
    TypeVariable previous = declaredTypeVariables.putIfAbsent(v, result);
    return previous != null ? previous : result;
  }

  @Override
  public ReferenceType substitute(Substitution substitution) {
    ReferenceType type = substitution.get(this);
    if (type != null) {
      return type;
    }
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns false, since an uninstantiated type variable may not be assigned to.
   */
  @Override
  public boolean isAssignableFrom(Type sourceType) {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * <p>A copy of a type variable with other bounds is equal to it, but is an instantiation of it
   * only if the copy is within the bounds of the type variable. For example, for {@code <X, Y
   * extends List<X>>}, the copy of {@code Y} whose bound is {@code List<Integer>} is not an
   * instantiation of the copy whose bound is {@code List<String>}.
   */
  @SuppressWarnings("ReferenceEquality") // fast path for the same object
  @Override
  public boolean isInstantiationOf(ReferenceType otherType) {
    if (this == otherType) {
      return true;
    }
    if (otherType.isVariable() && this.equals(otherType)) {
      // ReferenceType.isInstantiationOf would accept a copy because it is equal.
      TypeVariable otherVariable = (TypeVariable) otherType;
      return isIdenticalTo(otherVariable) || isWithinBoundsOf(otherVariable);
    }

    if (super.isInstantiationOf(otherType)) {
      return true;
    }

    if (otherType.isVariable()) {
      return isWithinBoundsOf((TypeVariable) otherType);
    }

    return false;
  }

  /**
   * Returns true if this type variable is within the bounds of the given type variable.
   *
   * @param otherVariable a type variable
   * @return true if this type variable is within the bounds of {@code otherVariable}
   */
  private boolean isWithinBoundsOf(TypeVariable otherVariable) {
    Substitution substitution = otherVariable.getSubstitution(this);
    // Compare this variable, not its bound, so that an unknown in otherVariable's bound can be
    // this variable.
    return otherVariable
            .getUpperTypeBound()
            .isUpperBound(new EagerReferenceBound(this), substitution)
        && otherVariable
            .getLowerTypeBound()
            .isLowerBound(getResolvedLowerTypeBound(), substitution);
  }

  @Override
  public boolean isSubtypeOfOrEqualTo(Type otherType) {
    if (super.isSubtypeOfOrEqualTo(otherType)) {
      return true;
    }
    if (otherType.isReferenceType()) {
      // A type variable is a subtype of every supertype of its upper bound.
      return isSubtypeOfOrEqualTo(getUpperTypeBound(), otherType);
    }
    return false;
  }

  /**
   * Returns the upper bound of this variable, with each type variable in its lazy parts substituted
   * for itself. For example, the bound of {@code T extends Comparable<T>} is a {@link
   * LazyParameterBound}; the result is a {@link ReferenceBound} whose type, {@code Comparable<T>},
   * can be compared with other types.
   *
   * @return the upper bound of this variable, with references to type variables resolved
   */
  ParameterBound getResolvedUpperTypeBound() {
    ParameterBound result = resolvedUpperBound;
    if (result == null) {
      result = getUpperTypeBound().substitute(getIdentitySubstitution());
      resolvedUpperBound = result;
    }
    return result;
  }

  /**
   * Returns the lower bound of this variable, with each type variable in its lazy parts substituted
   * for itself.
   *
   * @return the lower bound of this variable, with references to type variables resolved
   * @see #getResolvedUpperTypeBound()
   */
  ParameterBound getResolvedLowerTypeBound() {
    ParameterBound result = resolvedLowerBound;
    if (result == null) {
      result = getLowerTypeBound().substitute(getIdentitySubstitution());
      resolvedLowerBound = result;
    }
    return result;
  }

  /**
   * Returns a substitution of each type variable in the bounds of this variable, and of this
   * variable, for itself. Applying it to a bound resolves the bound's lazy parts.
   *
   * @return a substitution that replaces this variable and each type variable in its bounds with
   *     itself
   */
  private Substitution getIdentitySubstitution() {
    // A copy of a type variable equals the original, so it is not a separate entry.
    Set<TypeVariable> variables = new LinkedHashSet<>();
    variables.add(this);
    variables.addAll(getLowerTypeBound().getTypeParameters());
    variables.addAll(getUpperTypeBound().getTypeParameters());
    List<TypeVariable> parameters = new ArrayList<>(variables);
    return new Substitution(parameters, new ArrayList<ReferenceType>(parameters));
  }

  /**
   * Returns true if the type of the given bound is a subtype of the given type. An intersection
   * bound is a subtype of the type if any of its members is.
   *
   * <p>This method compares the bound type as written, without substituting for the type variables
   * in it. So, a variable {@code E} whose bound is {@code Enum<E>} is a subtype of {@code Enum<E>}.
   * Substituting {@code otherType} for {@code E} would instead compare {@code Enum<Enum<E>>} with
   * {@code Enum<E>}.
   *
   * <p>A {@link LazyParameterBound}, which can mention type variables other than the bounded one
   * (as in {@code T extends Comparable<U>}), is converted to the equivalent {@link ReferenceBound},
   * in which every type variable remains a type variable.
   *
   * @param upperBound an upper bound of a type variable
   * @param otherType the possible supertype
   * @return true if the type of {@code upperBound} is a subtype of {@code otherType}
   */
  private static boolean isSubtypeOfOrEqualTo(ParameterBound upperBound, Type otherType) {
    if (upperBound instanceof LazyParameterBound) {
      ReferenceBound converted = ((LazyParameterBound) upperBound).toReferenceBound();
      return converted != null && isSubtypeOfOrEqualTo(converted, otherType);
    }
    if (upperBound instanceof ReferenceBound) {
      return ((ReferenceBound) upperBound).getBoundType().isSubtypeOfOrEqualTo(otherType);
    }
    if (upperBound instanceof IntersectionTypeBound) {
      for (ParameterBound member : ((IntersectionTypeBound) upperBound).getBounds()) {
        if (isSubtypeOfOrEqualTo(member, otherType)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns a substitution of the given {@link ReferenceType} for this variable.
   *
   * @param otherType a replacement type
   * @return a substitution that replaces this variable with {@code otherType}
   */
  private Substitution getSubstitution(ReferenceType otherType) {
    return new Substitution(this, otherType);
  }

  @Override
  void setUpperBound(ParameterBound upperBound) {
    super.setUpperBound(upperBound);
    resolvedUpperBound = null;
    resolvedLowerBound = null;
  }

  @Override
  void setLowerBound(ParameterBound lowerBound) {
    super.setLowerBound(lowerBound);
    resolvedUpperBound = null;
    resolvedLowerBound = null;
  }

  @Override
  public boolean isVariable() {
    return true;
  }

  /**
   * Returns true if this {@link TypeVariable} can be instantiated by the {@link ReferenceType}.
   * Does not require that all bounds of this variable be instantiated.
   *
   * <p>A type variable in the bounds that is not instantiated stands for an unknown type, as in
   * {@link LazyParameterBound#isUpperBound(Type, Substitution)}, and each occurrence of it is
   * checked separately. So the result can be true even if no single type for that type variable
   * satisfies every occurrence. For example, for {@code <S extends Map<T, T>, T>}, {@code S} can be
   * instantiated by a class that implements {@code Map<A, B>}. A client that instantiates all the
   * type variables should check the bounds again with all of them instantiated, as {@link
   * randoop.reflection.TypeInstantiator} does.
   *
   * @param otherType the possibly instantiating type, not a variable
   * @return true if the given type can instantiate this variable, false otherwise
   */
  /*package*/ boolean canBeInstantiatedBy(ReferenceType otherType) {
    return canBeInstantiatedBy(otherType, new Substitution());
  }

  /**
   * Returns true if this {@link TypeVariable} can be instantiated by the {@link ReferenceType},
   * when the other type variables are instantiated as by the given substitution. Like {@link
   * #canBeInstantiatedBy(ReferenceType)}, but checks this variable's bounds with the mappings of
   * {@code context} for the type variables in them, rather than treating all of those type
   * variables as unknown.
   *
   * @param otherType the possibly instantiating type, not a variable
   * @param context a substitution for other type variables; any mapping of this variable in it is
   *     ignored
   * @return true if the given type can instantiate this variable, false otherwise
   */
  boolean canBeInstantiatedBy(ReferenceType otherType, Substitution context) {
    Substitution substitution = context.withMapping(this, otherType);
    // Checking a bound can check the same instantiation again, through mutually recursive bounds.
    // As for any recursive bound, assume that the inner check succeeds.  Only an inner check of
    // the same question can be assumed to succeed:  a check with other bounds or another
    // substitution can fail when this one does not.
    List<InstantiationCheck> inProgress = instantiationChecks.get();
    for (InstantiationCheck check : inProgress) {
      if (check.isSameCheck(this, otherType, substitution)) {
        return true;
      }
    }
    inProgress.add(new InstantiationCheck(this, otherType, substitution));
    try {
      return canBeInstantiatedByUnchecked(otherType, substitution);
    } finally {
      inProgress.remove(inProgress.size() - 1);
    }
  }

  /**
   * Returns true if this {@link TypeVariable} can be instantiated by the {@link ReferenceType}.
   * Does not guard against recursion; see {@link #canBeInstantiatedBy(ReferenceType,
   * Substitution)}.
   *
   * @param otherType a possibly instantiating type, not a variable
   * @param substitution a substitution for the other type variables, which maps this variable to
   *     {@code otherType}
   * @return true if the given type can instantiate this variable, false otherwise
   */
  private boolean canBeInstantiatedByUnchecked(ReferenceType otherType, Substitution substitution) {
    if (getLowerTypeBound().isVariable()) {
      if (!variableBoundAllows(getLowerTypeBound(), true, substitution, otherType)) {
        return false;
      }
    } else {
      if (!getLowerTypeBound().isLowerBound(otherType, substitution)) {
        return false;
      }
    }
    if (getUpperTypeBound().isVariable()) {
      if (!variableBoundAllows(getUpperTypeBound(), false, substitution, otherType)) {
        return false;
      }
    } else {
      if (!getUpperTypeBound().isUpperBound(otherType, substitution)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if {@code otherType} is within the given bound of this variable, which is a type
   * variable. If the substitution instantiates that type variable with a type that is not a
   * variable, compares {@code otherType} with that type. If the substitution replaces that type
   * variable by a type variable, even a distinct copy of it, that replacement is fixed, and {@code
   * otherType} must be a subtype (for an upper bound) or supertype (for a lower bound) of it.
   * Otherwise, that type variable stands for an unknown type. For a lower bound, the result is true if {@code otherType}
   * can instantiate it; for an upper bound, the result is true if it can be a supertype of {@code
   * otherType}, as determined by {@link #canBeSupertypeOf}.
   *
   * @param bound a bound of this variable that is a type variable
   * @param isLower true if {@code bound} is the lower bound of this variable, false if it is the
   *     upper bound
   * @param substitution the substitution of {@code otherType} for this variable
   * @param otherType the possibly instantiating type, not a variable
   * @return true if {@code otherType} is within {@code bound}
   */
  private boolean variableBoundAllows(
      ParameterBound bound, boolean isLower, Substitution substitution, ReferenceType otherType) {
    // If the substitution replaces the bound's type variable by a type variable, as for
    // `<S, T extends S>` with S := X, then X is fixed:  it is not chosen to suit otherType.  For
    // example, with `X extends Number`, Integer is not known to be a subtype of X.  This holds
    // also for a distinct copy of the bound's type variable, with other bounds, which equals it;
    // getSubstitutedType returns null if the substitution maps the type variable to itself.
    TypeVariable boundVariable = null;
    if (bound instanceof ReferenceBound) {
      boundVariable = (TypeVariable) ((ReferenceBound) bound).getBoundType();
    } else if (bound instanceof LazyParameterBound) {
      boundVariable = bound.getTypeParameters().get(0);
    }
    ReferenceType replacement =
        boundVariable == null ? null : boundVariable.getSubstitutedType(substitution);
    if (replacement != null && replacement.isVariable()) {
      return isLower
          ? Subtyping.isSubtypeOrEqual(replacement, otherType, substitution)
          : Subtyping.isSubtypeOrEqual(otherType, replacement, substitution);
    }
    ParameterBound substituted = bound.substitute(substitution);
    TypeVariable variable;
    if (substituted instanceof ReferenceBound
        && ((ReferenceBound) substituted).getBoundType().isVariable()) {
      variable = (TypeVariable) ((ReferenceBound) substituted).getBoundType();
    } else if (substituted instanceof LazyParameterBound) {
      // The substitution does not instantiate the type variable.
      variable = substituted.getTypeParameters().get(0);
    } else if (substituted instanceof ReferenceBound) {
      // The substitution instantiates the type variable with a type that is not a variable.
      return isLower
          ? substituted.isLowerBound(otherType, substitution)
          : substituted.isUpperBound(otherType, substitution);
    } else {
      return false;
    }
    return isLower
        ? variable.canBeInstantiatedBy(otherType, substitution)
        : variable.canBeSupertypeOf(otherType, substitution);
  }

  /**
   * Returns true if some instantiation of this type variable can be a supertype of the given type,
   * when the other type variables are instantiated as by the given substitution. The instantiations
   * that this considers are the lower bound of this variable, which is a supertype of every subtype
   * of it, {@code otherType} itself, and, if this variable is a type parameter, each of the strict
   * supertypes of {@code otherType} as computed by {@link #getStrictSupertypes}. For example, for
   * {@code <T extends Comparable<T>, V extends T>}, {@code java.sql.Date} can instantiate {@code
   * V}, because {@code java.util.Date} can instantiate {@code T}. Each instantiation other than the
   * lower bound is checked as by {@link #canBeInstantiatedBy(ReferenceType, Substitution)}.
   *
   * @param otherType a type, not a variable
   * @param context a substitution for other type variables
   * @return true if some instantiation of this variable can be a supertype of {@code otherType}
   */
  boolean canBeSupertypeOf(ReferenceType otherType, Substitution context) {
    if (getLowerTypeBound().isUpperBound(otherType, context)
        || canBeInstantiatedBy(otherType, context)) {
      return true;
    }
    // A capture variable stands for a particular type, so it is not searched for among the strict
    // supertypes of otherType.  For example, the capture of `? super Number` can be Object, a
    // supertype of String, but String is not known to be a subtype of it.
    if (!(this instanceof ExplicitTypeVariable)) {
      return false;
    }
    List<TypeVariable> otherTypeParameters = otherType.getTypeParameters();
    for (ReferenceType supertype : getStrictSupertypes(otherType)) {
      // A supertype that mentions a type variable that otherType does not is not a supertype of
      // otherType.  It arises when otherType is a GenericClassType, whose supertypes are those of
      // its raw class and do not preserve the relationship between their type parameters.  A
      // supertype that mentions only the type variables of otherType, such as List<E> for
      // ArrayList<E>, is a supertype of otherType.
      if (otherTypeParameters.containsAll(supertype.getTypeParameters())
          && canBeInstantiatedBy(supertype, context)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the strict supertypes of the given type, if it is a class, interface, array, or type
   * variable type. The supertypes of every class or interface type other than {@code Object}
   * include {@code Object}. The supertypes of an array type {@code C[]} are {@code Object}, {@code
   * Cloneable}, {@code Serializable}, and, if {@code C} is a reference type, {@code S[]} for each
   * strict supertype {@code S} of {@code C}. The supertypes of a type variable are the types in its
   * upper bound and their supertypes; this lets an array type such as {@code U[]}, for {@code U
   * extends Number}, have supertypes such as {@code Number[]} and {@code Object[]}. For any other
   * type, returns an empty list.
   *
   * @param type a type
   * @return the strict supertypes of {@code type}
   */
  private static List<ReferenceType> getStrictSupertypes(ReferenceType type) {
    List<ReferenceType> result = new ArrayList<>();
    if (type instanceof ClassOrInterfaceType) {
      result.addAll(((ClassOrInterfaceType) type).getSupertypesStrict());
      // An interface has no superclass, so its supertypes as computed above do not include Object.
      if (!type.isObject() && !result.contains(JavaTypes.OBJECT_TYPE)) {
        result.add(JavaTypes.OBJECT_TYPE);
      }
    } else if (type instanceof ArrayType) {
      result.add(JavaTypes.OBJECT_TYPE);
      result.add(JavaTypes.CLONEABLE_TYPE);
      result.add(JavaTypes.SERIALIZABLE_TYPE);
      Type componentType = ((ArrayType) type).getComponentType();
      if (componentType instanceof ReferenceType) {
        for (ReferenceType componentSupertype :
            getStrictSupertypes((ReferenceType) componentType)) {
          result.add(ArrayType.ofComponentType(componentSupertype));
        }
      }
    } else if (type instanceof TypeVariable) {
      ParameterBound upperBound = ((TypeVariable) type).getUpperTypeBound();
      for (ReferenceType boundType : getUpperBoundTypes(upperBound)) {
        if (!result.contains(boundType)) {
          result.add(boundType);
        }
        for (ReferenceType boundSupertype : getStrictSupertypes(boundType)) {
          if (!result.contains(boundSupertype)) {
            result.add(boundSupertype);
          }
        }
      }
    }
    return result;
  }

  /**
   * Returns the types in the given upper bound of a type variable: the type of a {@link
   * ReferenceBound}, the equivalent type of a {@link LazyParameterBound}, or the types of the
   * members of an {@link IntersectionTypeBound}.
   *
   * @param upperBound an upper bound of a type variable
   * @return the types in {@code upperBound}
   */
  private static List<ReferenceType> getUpperBoundTypes(ParameterBound upperBound) {
    List<ReferenceType> result = new ArrayList<>();
    if (upperBound instanceof LazyParameterBound) {
      ReferenceBound converted = ((LazyParameterBound) upperBound).toReferenceBound();
      if (converted != null) {
        result.add(converted.getBoundType());
      }
    } else if (upperBound instanceof ReferenceBound) {
      result.add(((ReferenceBound) upperBound).getBoundType());
    } else if (upperBound instanceof IntersectionTypeBound) {
      for (ParameterBound member : ((IntersectionTypeBound) upperBound).getBounds()) {
        result.addAll(getUpperBoundTypes(member));
      }
    }
    return result;
  }

  /**
   * Returns true if this type variable and the given one are the same type variable with identical
   * bounds. Unlike {@link #equals(Object)}, which treats copies of a type variable as equal even if
   * their bounds differ, this compares the bounds, including the bounds of the type variables in
   * them.
   *
   * @param other a type variable
   * @return true if {@code other} is this type variable with identical bounds
   */
  @SuppressWarnings("ReferenceEquality") // fast path; pairs are compared by identity
  boolean isIdenticalTo(TypeVariable other) {
    if (this == other) {
      return true;
    }
    if (!this.equals(other)) {
      return false;
    }
    // The bounds of a type variable, such as a capture variable, can refer to the type variable
    // itself, so the comparison of the bounds can compare the same pair of type variables again.
    // As for a recursive bound in canBeInstantiatedBy, assume that the inner comparison succeeds:
    // any difference is found by the comparison of the other parts of the bounds.
    List<TypeVariable[]> inProgress = identityChecks.get();
    for (TypeVariable[] pair : inProgress) {
      if (pair[0] == this && pair[1] == other) {
        return true;
      }
    }
    inProgress.add(new TypeVariable[] {this, other});
    try {
      return areIdentical(getLowerTypeBound(), other.getLowerTypeBound())
          && areIdentical(getUpperTypeBound(), other.getUpperTypeBound());
    } finally {
      inProgress.remove(inProgress.size() - 1);
    }
  }

  /**
   * Returns true if the given types are equal and the type variables in them are identical, as
   * determined by {@link #isIdenticalTo(TypeVariable)}. The types are compared structurally, so
   * that the type variables at corresponding positions are compared: for {@code Pair<Y, Y>} and
   * {@code Pair<Y, Y'>}, where {@code Y'} is a copy of {@code Y} with another bound, the result is
   * false. The enclosing types of member classes are compared too.
   *
   * <p>This compares the types structurally, occurrence by occurrence. Comparing the lists returned
   * by {@link ReferenceType#getTypeParameters()} would not suffice: those lists contain only one of
   * several copies of a type variable, because the copies are equal. So, for copies {@code XS} and
   * {@code XI} of a type variable {@code X} with other bounds, they would not distinguish {@code
   * Map<X, XS>} from {@code Map<X, XI>}.
   *
   * <p>Class, interface, array, and wildcard types are compared structurally rather than first by
   * {@link Object#equals(Object)}, so that a wildcard bound that is a {@link LazyParameterBound} is
   * identical to the {@link ReferenceBound} that it converts to, as {@link
   * #areIdentical(ParameterBound, ParameterBound)} determines. {@code equals} distinguishes the two
   * representations of a wildcard bound, as in {@code List<? extends Comparable<T>>}, or as in
   * {@code List<? extends Outer<String>.Inner>} before and after a substitution.
   *
   * @param type1 a type
   * @param type2 a type
   * @return true if the types are equal and the type variables in them are identical
   */
  @SuppressWarnings("ReferenceEquality") // fast path for the same object
  static boolean areIdentical(ReferenceType type1, ReferenceType type2) {
    if (type1 == type2) {
      return true;
    }
    if (type1 instanceof WildcardType && type2 instanceof WildcardType) {
      WildcardType wildcard1 = (WildcardType) type1;
      WildcardType wildcard2 = (WildcardType) type2;
      return wildcard1.hasUpperBound() == wildcard2.hasUpperBound()
          && areIdentical(wildcard1.getTypeBound(), wildcard2.getTypeBound());
    }
    if (type1.isArray() && type2.isArray()) {
      Type componentType1 = ((ArrayType) type1).getComponentType();
      Type componentType2 = ((ArrayType) type2).getComponentType();
      if (componentType1.isReferenceType() && componentType2.isReferenceType()) {
        return areIdentical((ReferenceType) componentType1, (ReferenceType) componentType2);
      }
      // Equal component types that are not reference types, such as int, are identical.
      return componentType1.equals(componentType2);
    }
    if (type1 instanceof ClassOrInterfaceType && type2 instanceof ClassOrInterfaceType) {
      // The same kind of type, as `equals` requires: for example, a GenericClassType is not equal
      // to an InstantiatedType.
      if (type1.getClass() != type2.getClass()
          || !type1.getRuntimeClass().equals(type2.getRuntimeClass())) {
        return false;
      }
    } else if (!type1.equals(type2)) {
      return false;
    }
    if (type1 instanceof TypeVariable && type2 instanceof TypeVariable) {
      return ((TypeVariable) type1).isIdenticalTo((TypeVariable) type2);
    }
    if (type1 instanceof ClassOrInterfaceType && type2 instanceof ClassOrInterfaceType) {
      ClassOrInterfaceType classType1 = (ClassOrInterfaceType) type1;
      ClassOrInterfaceType classType2 = (ClassOrInterfaceType) type2;
      if (classType1.isMemberClass() != classType2.isMemberClass()) {
        return false;
      }
      if (classType1.isMemberClass()
          && classType2.isMemberClass()
          && !areIdentical(classType1.enclosingType, classType2.enclosingType)) {
        return false;
      }
      List<TypeArgument> arguments1 = classType1.getTypeArguments();
      List<TypeArgument> arguments2 = classType2.getTypeArguments();
      if (arguments1.size() != arguments2.size()) {
        return false;
      }
      for (int i = 0; i < arguments1.size(); i++) {
        if (!areIdentical(arguments1.get(i), arguments2.get(i))) {
          return false;
        }
      }
      return true;
    }
    return areIdentical(type1.getTypeParameters(), type2.getTypeParameters());
  }

  /**
   * Returns true if the given type arguments are equal and the type variables in them are
   * identical, as determined by {@link #areIdentical(ReferenceType, ReferenceType)}.
   *
   * @param argument1 a type argument
   * @param argument2 a type argument
   * @return true if the type arguments are equal and the type variables in them are identical
   */
  private static boolean areIdentical(TypeArgument argument1, TypeArgument argument2) {
    if (argument1 instanceof ReferenceArgument && argument2 instanceof ReferenceArgument) {
      return areIdentical(
          ((ReferenceArgument) argument1).getReferenceType(),
          ((ReferenceArgument) argument2).getReferenceType());
    }
    if (argument1 instanceof WildcardArgument && argument2 instanceof WildcardArgument) {
      WildcardArgument wildcard1 = (WildcardArgument) argument1;
      WildcardArgument wildcard2 = (WildcardArgument) argument2;
      return wildcard1.hasUpperBound() == wildcard2.hasUpperBound()
          && areIdentical(wildcard1.getTypeBound(), wildcard2.getTypeBound());
    }
    return argument1.equals(argument2);
  }

  /**
   * Returns true if the given bounds represent the same type and the type variables in them are
   * identical, as determined by {@link #isIdenticalTo(TypeVariable)}.
   *
   * @param bound1 a bound
   * @param bound2 a bound
   * @return true if the bounds represent the same type and the type variables in them are identical
   */
  @SuppressWarnings("ReferenceEquality") // fast path for the same object
  static boolean areIdentical(ParameterBound bound1, ParameterBound bound2) {
    if (bound1 == bound2) {
      return true;
    }
    if (bound1 instanceof LazyParameterBound && bound2 instanceof LazyParameterBound) {
      // The type variables in each are created from the same reflection type.
      return bound1.equals(bound2);
    }
    if (bound1 instanceof LazyParameterBound) {
      ParameterBound converted = ((LazyParameterBound) bound1).toReferenceBound();
      return converted != null && areIdentical(converted, bound2);
    }
    if (bound2 instanceof LazyParameterBound) {
      ParameterBound converted = ((LazyParameterBound) bound2).toReferenceBound();
      return converted != null && areIdentical(bound1, converted);
    }
    // As in areIdentical(ReferenceType, ReferenceType), compare the bounds structurally, rather
    // than comparing their getTypeParameters(), which contain only one of several copies of a type
    // variable.  Do not first compare the bounds with equals():  a wildcard bound nested in the
    // bound type can be a LazyParameterBound in one and the equivalent ReferenceBound in the other.
    // The members of intersection bounds are compared by this method, so that a lazy member can be
    // identical to an eager one.
    if (bound1 instanceof ReferenceBound && bound2 instanceof ReferenceBound) {
      return areIdentical(
          ((ReferenceBound) bound1).getBoundType(), ((ReferenceBound) bound2).getBoundType());
    }
    if (bound1 instanceof IntersectionTypeBound && bound2 instanceof IntersectionTypeBound) {
      List<ParameterBound> members1 = ((IntersectionTypeBound) bound1).getBounds();
      List<ParameterBound> members2 = ((IntersectionTypeBound) bound2).getBounds();
      if (members1.size() != members2.size()) {
        return false;
      }
      for (int i = 0; i < members1.size(); i++) {
        if (!areIdentical(members1.get(i), members2.get(i))) {
          return false;
        }
      }
      return true;
    }
    return bound1.equals(bound2)
        && areIdentical(bound1.getTypeParameters(), bound2.getTypeParameters());
  }

  /**
   * Returns true if the given lists have the same length and their corresponding elements are
   * identical, as determined by {@link #isIdenticalTo(TypeVariable)}.
   *
   * @param variables1 a list of type variables
   * @param variables2 a list of type variables
   * @return true if the lists have identical elements
   */
  private static boolean areIdentical(
      List<TypeVariable> variables1, List<TypeVariable> variables2) {
    if (variables1.size() != variables2.size()) {
      return false;
    }
    for (int i = 0; i < variables1.size(); i++) {
      if (!variables1.get(i).isIdenticalTo(variables2.get(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the type that the given substitution maps this type variable to, or null if the
   * substitution does not map this type variable or maps it to itself, as determined by {@link
   * Substitution#mapsToItself(TypeVariable)}.
   *
   * <p>When the substitution maps this type variable to itself, the substitution's key can be a
   * copy of this type variable with other bounds. Returning null keeps this copy and its bounds,
   * rather than the substitution's copy. When the substitution maps this type variable to a copy of
   * it with other bounds, the result is that copy.
   *
   * @param substitution a substitution
   * @return the type that {@code substitution} maps this type variable to, or null if {@code
   *     substitution} does not map this type variable or maps it to itself
   */
  /*package*/ @Nullable ReferenceType getSubstitutedType(Substitution substitution) {
    ReferenceType type = substitution.get(this);
    if (type != null && substitution.mapsToItself(this)) {
      return null;
    }
    return type;
  }

  /**
   * Returns the type parameters in this type: this variable, followed by the type variables in its
   * bounds.
   *
   * <p>This variable comes first so that it, rather than a copy of it with other bounds that occurs
   * in its bounds, represents it in the result. Copies of a type variable are equal, so the result
   * contains only one of them.
   *
   * @return this variable and the type variables in its bounds
   */
  @Override
  public List<TypeVariable> getTypeParameters() {
    Set<TypeVariable> parameters = new LinkedHashSet<>();
    parameters.add(this);
    parameters.addAll(super.getTypeParameters());
    return new ArrayList<>(parameters);
  }

  /**
   * Returns a copy of this type variable, but with the given bounds. An implementation may ignore
   * {@code lowerBound} if its kind of type variable has a fixed lower bound.
   *
   * @param lowerBound the lower bound for the new type variable
   * @param upperBound the upper bound for the new type variable
   * @return a copy of this type variable with the given bounds
   */
  public abstract TypeVariable createCopyWithBounds(
      ParameterBound lowerBound, ParameterBound upperBound);

  @Override
  public Type getRawtype() {
    return JavaTypes.OBJECT_TYPE;
  }

  @Override
  public String toString() {
    ParameterBound lowerBound = getLowerTypeBound();
    ParameterBound upperBound = getUpperTypeBound();
    StringBuilder result = new StringBuilder(getSimpleName());
    if (!(lowerBound instanceof ReferenceBound
        && ((ReferenceBound) lowerBound).getBoundType() instanceof NullReferenceType)) {
      result.append(" super ").append(lowerBound);
    }
    if (!(upperBound instanceof ReferenceBound
        && ((ReferenceBound) upperBound).getBoundType().isObject())) {
      result.append(" extends ").append(upperBound);
    }
    return result.toString();
  }
}
