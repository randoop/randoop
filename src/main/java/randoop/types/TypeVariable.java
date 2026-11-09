package randoop.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
      if (hasSelfBoundWithin(otherType)) {
        return true;
      }
      Substitution substitution = getSubstitution((ReferenceType) otherType);
      return this.getUpperTypeBound().isLowerBound(otherType, substitution);
    }
    return false;
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
      if (!variableBoundAllows(getLowerTypeBound(), false, substitution, otherType)) {
        return false;
      }
    } else {
      if (!getLowerTypeBound().isLowerBound(otherType, substitution)) {
        return false;
      }
    }
    if (getUpperTypeBound().isVariable()) {
      if (!variableBoundAllows(getUpperTypeBound(), true, substitution, otherType)) {
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
   * Returns true if the given type is within the given bound of this variable, which is a type
   * variable. If the substitution does not instantiate that type variable, it stands for an unknown
   * type, and the result is true if the given type can instantiate it. If the substitution maps it
   * to another type, the given type is compared with that type.
   *
   * @param bound a bound of this variable that is a type variable
   * @param isUpper true if {@code bound} is the upper bound of this variable, false if it is the
   *     lower bound
   * @param substitution the substitution of {@code otherType} for this variable
   * @param otherType the possibly instantiating type, not a variable
   * @return true if {@code otherType} is within {@code bound}
   */
  private boolean variableBoundAllows(
      ParameterBound bound, boolean isUpper, Substitution substitution, ReferenceType otherType) {
    ParameterBound substituted = bound.substitute(substitution);
    TypeVariable variable;
    if (substituted instanceof ReferenceBound
        && ((ReferenceBound) substituted).getBoundType().isVariable()) {
      variable = (TypeVariable) ((ReferenceBound) substituted).getBoundType();
    } else if (substituted instanceof LazyParameterBound) {
      // The substitution does not instantiate the type variable.
      variable = substituted.getTypeParameters().get(0);
    } else {
      // The substitution maps the type variable to a type that is not a variable, as for `V
      // extends K` with K := Number.  Compare otherType with that type.
      return isUpper
          ? substituted.isUpperBound(otherType, substitution)
          : substituted.isLowerBound(otherType, substitution);
    }
    return variable.canBeInstantiatedBy(otherType, substitution);
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
