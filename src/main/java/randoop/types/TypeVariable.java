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
  private static final ThreadLocal<Set<List<Object>>> instantiationChecks =
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

  /**
   * Returns an object that identifies this type variable. Substitution can create a copy of a type
   * variable whose bounds differ, such as by having a lazy part resolved. {@link #equals(Object)}
   * distinguishes the copy from the original, but they have the same identity.
   *
   * @return an object that identifies this type variable
   */
  Object getIdentity() {
    return this;
  }

  /**
   * Returns true if the two types are equal or are the same type variable, possibly with different
   * bounds.
   *
   * @param type a type
   * @param otherType a type
   * @return true if the two types are equal or are the same type variable
   * @see #getIdentity()
   */
  static boolean isSameVariable(ReferenceType type, ReferenceType otherType) {
    // Comparing identities is cheaper than equals(), which compares bounds.
    if (type instanceof TypeVariable && otherType instanceof TypeVariable) {
      return ((TypeVariable) type).getIdentity().equals(((TypeVariable) otherType).getIdentity());
    }
    return type.equals(otherType);
  }

  /**
   * Returns true if a type variable in the bounds of this variable that a substitution does not
   * replace stands for an unknown type when the bounds are checked, as in {@link
   * LazyParameterBound#isUpperBound(Type, Substitution)}. Such a type variable is usually another
   * type parameter of the declaration of this variable.
   *
   * @return true if an uninstantiated type variable in the bounds of this variable stands for an
   *     unknown type
   */
  boolean hasUnknownBoundVariables() {
    return true;
  }

  @Override
  public boolean isVariable() {
    return true;
  }

  /**
   * Returns true if this {@link TypeVariable} can be instantiated by the {@link ReferenceType}.
   * Does not require that all bounds of this variable be instantiated.
   *
   * <p>A type variable in the bounds that is not instantiated stands for an unknown type (see
   * {@link #hasUnknownBoundVariables()}), and each occurrence of it is checked separately. So the
   * result can be true even if no single type for that type variable satisfies every occurrence.
   * For example, for {@code <S extends Map<T, T>, T>}, {@code S} can be instantiated by a class
   * that implements {@code Map<A, B>}. A client that instantiates all the type variables should
   * check the bounds again with all of them instantiated, as {@link
   * randoop.reflection.TypeInstantiator} does.
   *
   * @param otherType the possibly instantiating type, not a variable
   * @return true if the given type can instantiate this variable, false otherwise
   */
  boolean canBeInstantiatedBy(ReferenceType otherType) {
    // Checking a bound can check the same instantiation again, through mutually recursive bounds
    // such as `<X extends Comparable<Y>, Y extends List<X>>`.  As for any recursive bound, assume
    // that the inner check succeeds.
    // Identify type variables by identity, because substitution creates copies of them.
    Set<List<Object>> inProgress = instantiationChecks.get();
    List<Object> check =
        Arrays.asList(
            getIdentity(),
            otherType instanceof TypeVariable
                ? ((TypeVariable) otherType).getIdentity()
                : otherType);
    if (!inProgress.add(check)) {
      return true;
    }
    try {
      return canBeInstantiatedByUnchecked(otherType);
    } finally {
      inProgress.remove(check);
    }
  }

  /**
   * Returns true if this {@link TypeVariable} can be instantiated by the {@link ReferenceType}.
   * Does not guard against recursion; see {@link #canBeInstantiatedBy(ReferenceType)}.
   *
   * @param otherType a possibly instantiating type, not a variable
   * @return true if the given type can instantiate this variable, false otherwise
   */
  private boolean canBeInstantiatedByUnchecked(ReferenceType otherType) {
    Substitution substitution = getSubstitution(otherType);
    if (getLowerTypeBound().isVariable()) {
      if (!variableBoundAllows(getLowerTypeBound(), substitution, otherType)) {
        return false;
      }
    } else {
      if (!getLowerTypeBound().isLowerBound(otherType, substitution)) {
        return false;
      }
    }
    if (getUpperTypeBound().isVariable()) {
      if (!variableBoundAllows(getUpperTypeBound(), substitution, otherType)) {
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
   * Returns true if the given type can instantiate the type variable that is the given bound of
   * this variable. That type variable might not be instantiated by the substitution; then, if
   * {@link #hasUnknownBoundVariables()} is true, it stands for an unknown type, and otherwise this
   * method returns false.
   *
   * @param bound a bound of this variable that is a type variable
   * @param substitution the substitution of {@code otherType} for this variable
   * @param otherType the possibly instantiating type, not a variable
   * @return true if {@code otherType} can instantiate the type variable that is {@code bound}
   */
  private boolean variableBoundAllows(
      ParameterBound bound, Substitution substitution, ReferenceType otherType) {
    ParameterBound substituted = bound.substitute(substitution);
    TypeVariable variable;
    if (substituted instanceof ReferenceBound
        && ((ReferenceBound) substituted).getBoundType().isVariable()) {
      variable = (TypeVariable) ((ReferenceBound) substituted).getBoundType();
    } else if (substituted instanceof LazyParameterBound && hasUnknownBoundVariables()) {
      // The substitution does not instantiate the type variable.
      variable = substituted.getTypeParameters().get(0);
    } else {
      return false;
    }
    return variable.canBeInstantiatedBy(otherType);
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
