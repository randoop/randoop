package randoop.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/** An abstract class representing type variables. */
public abstract class TypeVariable extends ParameterType {

  /**
   * The pairs of a type variable and a type for which {@link #canBeInstantiatedBy(ReferenceType)}
   * is in progress, in the current thread.
   */
  @SuppressWarnings("nullness:type.argument") // withInitial: get() never returns null
  private static final ThreadLocal<Set<List<ReferenceType>>> instantiationChecks =
      ThreadLocal.withInitial(HashSet::new);

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
    Set<java.lang.reflect.TypeVariable<?>> variableSet = new HashSet<>(1);
    variableSet.add(v);
    return new ExplicitTypeVariable(v, ParameterBound.forTypes(variableSet, v.getBounds()));
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
      // Compare this variable, not its bound, so that an unknown in otherVariable's bound can be
      // this variable.
      return otherVariable
              .getUpperTypeBound()
              .isUpperBound(new EagerReferenceBound(this), substitution)
          && otherVariable
              .getLowerTypeBound()
              .isLowerBound(getResolvedLowerTypeBound(), substitution);
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
      // substitutes otherType for this variable within the bound.  For a type parameter, such a
      // bound is a LazyParameterBound, so convert it to a ReferenceBound to compare it.
      ParameterBound upperBound = getUpperTypeBound();
      if (upperBound instanceof LazyParameterBound) {
        ReferenceBound converted = ((LazyParameterBound) upperBound).toReferenceBound();
        if (converted != null) {
          upperBound = converted;
        }
      }
      if (upperBound instanceof ReferenceBound
          && ((ReferenceBound) upperBound).getBoundType().equals(otherType)) {
        return true;
      }
      if (hasSelfBoundWithin(otherType)) {
        return true;
      }
      Substitution substitution = getSubstitution((ReferenceType) otherType);
      return this.getUpperTypeBound().isLowerBound(otherType, substitution);
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
   * Returns true if the upper bound of this variable mentions no type variable other than this one,
   * is not itself a type variable, and, as declared, is a subtype of the given type. For example,
   * the upper bound of the capture of {@code ?} in {@code SelfComparableBound<?>}, where {@code
   * SelfComparableBound<T extends Comparable<T>>}, is {@code Comparable<capture>}, which is a
   * subtype of {@code Comparable<capture>}. Then this variable is a subtype of the given type.
   *
   * <p>The upper bound is compared without a substitution, so an occurrence of this variable in it
   * stands for this variable. Substituting the given type for this variable instead, as {@link
   * #isSubtypeOfOrEqualTo(Type)} otherwise does, compares {@code Comparable<Comparable<capture>>}
   * with {@code Comparable<capture>}.
   *
   * @param otherType a reference type
   * @return true if the declared upper bound of this variable, which mentions no other type
   *     variable, is a subtype of {@code otherType}
   */
  private boolean hasSelfBoundWithin(Type otherType) {
    ParameterBound upperBound = getUpperTypeBound();
    if (upperBound.isVariable()) {
      return false;
    }
    for (TypeVariable variable : upperBound.getTypeParameters()) {
      if (!variable.equals(this)) {
        return false;
      }
    }
    return upperBound.isLowerBound(otherType, new Substitution());
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
   * instantiated by a class that implements {@code Map<A, B>}. A lower bound of this variable that
   * is an uninstantiated type variable does not restrict the given type at all, since determining
   * whether some instantiation of that type variable is a subtype of the given type requires a
   * search. A client that instantiates all the type variables should check the bounds again with
   * all of them instantiated, as {@link randoop.reflection.TypeInstantiator} does.
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
    // Checking a bound can check the same instantiation again, through mutually recursive bounds
    // such as `<X extends Comparable<Y>, Y extends List<X>>`.  As for any recursive bound, assume
    // that the inner check succeeds.
    Set<List<ReferenceType>> inProgress = instantiationChecks.get();
    List<ReferenceType> check = Arrays.asList(this, otherType);
    if (!inProgress.add(check)) {
      return true;
    }
    try {
      return canBeInstantiatedByUnchecked(otherType, context);
    } finally {
      inProgress.remove(check);
    }
  }

  /**
   * Returns true if this {@link TypeVariable} can be instantiated by the {@link ReferenceType}.
   * Does not guard against recursion; see {@link #canBeInstantiatedBy(ReferenceType,
   * Substitution)}.
   *
   * @param otherType a possibly instantiating type, not a variable
   * @param context a substitution for other type variables
   * @return true if the given type can instantiate this variable, false otherwise
   */
  private boolean canBeInstantiatedByUnchecked(ReferenceType otherType, Substitution context) {
    Substitution substitution = context.withMapping(this, otherType);
    if (getLowerTypeBound().isVariable()) {
      // The lower bound is a type variable.  If the substitution does not instantiate it, then it
      // stands for an unknown type, and `otherType` is within the lower bound if it is a supertype
      // of some type that can instantiate the unknown type variable.  For example, for
      // `<T extends Number> void consume(List<? super T>)`, Object is within the lower bound of
      // the capture variable, because T can be Number.  Finding such a type is a search, so defer
      // the check to the client, which checks the bounds again once it instantiates the unknown
      // type variable.
      if (unknownVariable(getLowerTypeBound(), substitution) == null
          && !getLowerTypeBound().isLowerBound(otherType, substitution)) {
        return false;
      }
    } else {
      if (!getLowerTypeBound().isLowerBound(otherType, substitution)) {
        return false;
      }
    }
    if (getUpperTypeBound().isVariable()) {
      TypeVariable unknown = unknownVariable(getUpperTypeBound(), substitution);
      if (unknown != null) {
        // The unknown type variable can be `otherType` itself; see
        // EagerReferenceBound.isUpperBound(Type, Substitution).
        if (!unknown.canBeInstantiatedBy(otherType, substitution)) {
          return false;
        }
      } else if (!getUpperTypeBound().isUpperBound(otherType, substitution)) {
        // The substitution maps the type variable to a type that is not a variable, as for `V
        // extends K` with K := Number, and otherType is not within that type.
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
   * Returns the type variable that the given bound of this variable is, if the substitution does
   * not instantiate it. Otherwise, returns null; then the substituted bound is a type that can be
   * compared with a candidate type.
   *
   * @param bound a bound of this variable that is a type variable
   * @param substitution the substitution of a candidate type for this variable
   * @return the uninstantiated type variable that {@code bound} is, or null if {@code substitution}
   *     instantiates it
   */
  private static @Nullable TypeVariable unknownVariable(
      ParameterBound bound, Substitution substitution) {
    ParameterBound substituted = bound.substitute(substitution);
    if (substituted instanceof ReferenceBound
        && ((ReferenceBound) substituted).getBoundType().isVariable()) {
      return (TypeVariable) ((ReferenceBound) substituted).getBoundType();
    } else if (substituted instanceof LazyParameterBound) {
      // The substitution does not instantiate the type variable.
      return substituted.getTypeParameters().get(0);
    } else {
      return null;
    }
  }

  /**
   * Returns the type parameters in this type, which is this variable.
   *
   * @return this variable
   */
  @Override
  public List<TypeVariable> getTypeParameters() {
    Set<TypeVariable> parameters = new LinkedHashSet<>(super.getTypeParameters());
    parameters.add(this);
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
