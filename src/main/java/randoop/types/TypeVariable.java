package randoop.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
   * Creates a type variable with {@link NullReferenceType} as the lower bound, and the {@code
   * Object} type as upper bound.
   */
  TypeVariable() {
    super();
  }

  /**
   * Creates a type variable with the given type bounds. Assumes the bounds are consistent and does
   * not check for the subtype relationship.
   *
   * @param lowerBound the lower type bound on this variable
   * @param upperBound the upper type bound on this variable
   */
  TypeVariable(ParameterBound lowerBound, ParameterBound upperBound) {
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

  @Override
  public boolean isInstantiationOf(ReferenceType otherType) {
    if (super.isInstantiationOf(otherType)) {
      return true;
    }

    if (otherType.isVariable()) {
      TypeVariable otherVariable = (TypeVariable) otherType;
      Substitution substitution = otherVariable.getSubstitution(this);
      boolean lowerboundOk =
          otherVariable.getLowerTypeBound().isLowerBound(getLowerTypeBound(), substitution);
      boolean upperboundOk =
          otherVariable.getUpperTypeBound().isUpperBound(getUpperTypeBound(), substitution);
      return lowerboundOk && upperboundOk;
    }

    return false;
  }

  @Override
  public boolean isSubtypeOfOrEqualTo(Type otherType) {
    if (super.isSubtypeOfOrEqualTo(otherType)) {
      return true;
    }
    if (otherType.isReferenceType()) {
      // A type variable is a subtype of its upper bound.  The test below does not always recognize
      // that:  if the bound refers to this variable, as in `T extends Comparable<T>`, it
      // substitutes otherType for this variable within the bound.
      ParameterBound upperBound = getUpperTypeBound();
      if (upperBound instanceof ReferenceBound
          && ((ReferenceBound) upperBound).getBoundType().equals(otherType)) {
        return true;
      }
      Substitution substitution = getSubstitution((ReferenceType) otherType);
      return this.getUpperTypeBound().isLowerBound(otherType, substitution);
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
  boolean canBeInstantiatedBy(ReferenceType otherType) {
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
   * variable, compares {@code otherType} with that type. Otherwise, that type variable stands for
   * an unknown type. For a lower bound, the result is true if {@code otherType} can instantiate it;
   * for an upper bound, the result is true if it can be a supertype of {@code otherType}, as
   * determined by {@link #canBeSupertypeOf}.
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
   * of it, and {@code otherType} itself, as {@link #canBeInstantiatedBy( ReferenceType,
   * Substitution)} checks. So the result can be false even if some other supertype of {@code
   * otherType} can instantiate this variable.
   *
   * @param otherType a type, not a variable
   * @param context a substitution for other type variables
   * @return true if some instantiation of this variable can be a supertype of {@code otherType}
   */
  boolean canBeSupertypeOf(ReferenceType otherType, Substitution context) {
    return getLowerTypeBound().isUpperBound(otherType, context)
        || canBeInstantiatedBy(otherType, context);
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
  @SuppressWarnings("ReferenceEquality") // fast path for the same object
  boolean isIdenticalTo(TypeVariable other) {
    if (this == other) {
      return true;
    }
    if (!this.equals(other)) {
      return false;
    }
    if (!(this instanceof ExplicitTypeVariable)) {
      // The bounds of a capture variable can refer to the capture variable itself, so comparing
      // them deeply would not terminate.
      return getLowerTypeBound().equals(other.getLowerTypeBound())
          && getUpperTypeBound().equals(other.getUpperTypeBound());
    }
    return areIdentical(getLowerTypeBound(), other.getLowerTypeBound())
        && areIdentical(getUpperTypeBound(), other.getUpperTypeBound());
  }

  /**
   * Returns true if the given types are equal and the type variables in them are identical, as
   * determined by {@link #isIdenticalTo(TypeVariable)}.
   *
   * @param type1 a type
   * @param type2 a type
   * @return true if the types are equal and the type variables in them are identical
   */
  @SuppressWarnings("ReferenceEquality") // fast path for the same object
  static boolean areIdentical(ReferenceType type1, ReferenceType type2) {
    return type1 == type2
        || (type1.equals(type2)
            && areIdentical(type1.getTypeParameters(), type2.getTypeParameters()));
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
