package randoop.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/** An abstract class representing type variables. */
public abstract class TypeVariable extends ParameterType {

  /**
   * The type variables in the lazy parts of the bounds of this variable, other than this variable;
   * null if not yet computed. Use {@link #getBoundVariables()} instead of reading this field.
   */
  private @Nullable List<TypeVariable> boundVariables = null;

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
   * Creates a substitution of the given {@link ReferenceType} for this variable. Every other type
   * variable in the lazy parts of the bounds of this variable, such as another type parameter of
   * its declaration, is substituted for itself, so the substitution instantiates the bounds of this
   * variable.
   *
   * @param otherType the replacement type
   * @return a substitution that replaces this variable with {@code otherType}
   */
  private Substitution getSubstitution(ReferenceType otherType) {
    List<TypeVariable> parameters = new ArrayList<>(getBoundVariables());
    List<ReferenceType> arguments = new ArrayList<>(parameters);
    parameters.add(this);
    arguments.add(otherType);
    return new Substitution(parameters, arguments);
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
    if (type.equals(otherType)) {
      return true;
    }
    return type instanceof TypeVariable
        && otherType instanceof TypeVariable
        && ((TypeVariable) type).getIdentity().equals(((TypeVariable) otherType).getIdentity());
  }

  /**
   * Returns the type variables in the lazy parts of the bounds of this variable, other than this
   * variable.
   *
   * @return the type variables in the lazy parts of the bounds of this variable, other than this
   *     variable
   */
  private List<TypeVariable> getBoundVariables() {
    List<TypeVariable> result = boundVariables;
    if (result == null) {
      // Keyed by identity, so that a copy of a type variable is not a separate entry.
      Map<Object, TypeVariable> variables = new LinkedHashMap<>();
      for (TypeVariable variable : getLowerTypeBound().getLazyTypeParameters()) {
        variables.putIfAbsent(variable.getIdentity(), variable);
      }
      for (TypeVariable variable : getUpperTypeBound().getLazyTypeParameters()) {
        variables.putIfAbsent(variable.getIdentity(), variable);
      }
      variables.remove(getIdentity());
      result = new ArrayList<>(variables.values());
      boundVariables = result;
    }
    return result;
  }

  @Override
  void setUpperBound(ParameterBound upperBound) {
    super.setUpperBound(upperBound);
    boundVariables = null;
  }

  @Override
  void setLowerBound(ParameterBound lowerBound) {
    super.setLowerBound(lowerBound);
    boundVariables = null;
  }

  @Override
  public boolean isVariable() {
    return true;
  }

  /**
   * Returns true if this {@link TypeVariable} can be instantiated by the {@link ReferenceType}.
   * Does not require that all bounds of this variable be instantiated.
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
   * @param otherType the possibly instantiating type, not a variable
   * @return true if the given type can instantiate this variable, false otherwise
   */
  private boolean canBeInstantiatedByUnchecked(ReferenceType otherType) {
    Substitution substitution = getSubstitution(otherType);
    if (getLowerTypeBound().isVariable()) {
      ParameterBound boundType = getLowerTypeBound().substitute(substitution);
      TypeVariable checkType = (TypeVariable) ((ReferenceBound) boundType).getBoundType();
      if (!checkType.canBeInstantiatedBy(otherType)) {
        return false;
      }
    } else {
      if (!getLowerTypeBound().isLowerBound(otherType, substitution)) {
        return false;
      }
    }
    if (getUpperTypeBound().isVariable()) {
      ParameterBound boundType = getUpperTypeBound().substitute(substitution);
      TypeVariable checkType = (TypeVariable) ((ReferenceBound) boundType).getBoundType();
      if (!checkType.canBeInstantiatedBy(otherType)) {
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
