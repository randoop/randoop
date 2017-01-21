package randoop.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstract class representing type variables.
 */
public abstract class TypeVariable extends ParameterType {

  /**
   * Creates a type variable with {@link NullReferenceType} as the lower bound, and
   * the {@code Object} type as upper bound.
   */
  TypeVariable() {
    super();
  }

  /**
   * Creates a type variable with the given type bounds.
   * Assumes the bounds are consistent and does not check for the subtype relationship.
   *
   * @param lowerBound  the lower type bound on this variable
   * @param upperBound  the upper type bound on this variable
   */
  TypeVariable(ParameterBound lowerBound, ParameterBound upperBound) {
    super(lowerBound, upperBound);
  }

  /**
   * Creates a {@code TypeVariable} object for a given {@code java.lang.reflect.Type}
   * reference, which must be a {@code java.lang.reflect.TypeVariable}.
   *
   * @param type  the type reference
   * @return the {@code TypeVariable} for the given type
   */
  public static TypeVariable forType(java.lang.reflect.Type type) {
    if (!(type instanceof java.lang.reflect.TypeVariable<?>)) {
      throw new IllegalArgumentException("type must be a type variable, got " + type);
    }
    java.lang.reflect.TypeVariable<?> v = (java.lang.reflect.TypeVariable) type;
    Set<java.lang.reflect.TypeVariable<?>> variableSet = new HashSet<>();
    variableSet.add(v);
    return new ExplicitTypeVariable(v, ParameterBound.forTypes(variableSet, v.getBounds()));
  }

  @Override
  public ReferenceType apply(Substitution<ReferenceType> substitution) {
    ReferenceType type = substitution.get(this);
    if (type != null) {
      return type;
    }
    return this;
  }

  /**
   * Indicate whether this type has a wildcard either as or in a type argument.
   *
   * @return true if this type has a wildcard, and false otherwise
   */
  public boolean hasWildcard() {
    return false;
  }

  /**
   * {@inheritDoc}
   * Returns false, since an uninstantiated type variable may not be assigned to.
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
      TypeVariable variable = (TypeVariable) otherType;
      List<TypeVariable> typeParameters = new ArrayList<>();
      typeParameters.add(variable);
      Substitution<ReferenceType> substitution =
          Substitution.forArgs(typeParameters, (ReferenceType) this);
      boolean lowerbound =
          variable.getLowerTypeBound().isLowerBound(getLowerTypeBound(), substitution);
      boolean upperbound =
          variable.getUpperTypeBound().isUpperBound(getUpperTypeBound(), substitution);
      return lowerbound && upperbound;
    }
    return false;
  }

  @Override
  public boolean isSubtypeOf(Type otherType) {
    if (super.isSubtypeOf(otherType)) {
      return true;
    }
    if (otherType.isReferenceType()) {
      List<TypeVariable> variableList = new ArrayList<>();
      variableList.add(this);
      Substitution<ReferenceType> substitution =
          Substitution.forArgs(variableList, (ReferenceType) otherType);
      return this.getUpperTypeBound().isLowerBound(otherType, substitution);
    }
    return false;
  }

  @Override
  boolean isVariable() {
    return true;
  }
}
