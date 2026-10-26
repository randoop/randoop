package randoop.types;

import java.util.ArrayList;
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
    // A type variable is a subtype of any supertype of its upper bound, in which the type variable
    // stands for itself.  For example, `W extends Comparable<W>` is a subtype of `Comparable<W>`.
    return otherType.isReferenceType()
        && ReferenceBound.isSubtypeOrEqual(this, (ReferenceType) otherType);
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
      result = getUpperTypeBound().substitute(getSubstitution(this));
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
      result = getLowerTypeBound().substitute(getSubstitution(this));
      resolvedLowerBound = result;
    }
    return result;
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
    resolvedUpperBound = null;
    resolvedLowerBound = null;
  }

  @Override
  void setLowerBound(ParameterBound lowerBound) {
    super.setLowerBound(lowerBound);
    boundVariables = null;
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
   * @param otherType the possibly instantiating type, not a variable
   * @return true if the given type can instantiate this variable, false otherwise
   */
  boolean canBeInstantiatedBy(ReferenceType otherType) {
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
