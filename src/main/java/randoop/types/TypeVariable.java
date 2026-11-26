package randoop.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/** An abstract class representing type variables. */
public abstract class TypeVariable extends ParameterType {

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
      Substitution substitution = getSubstitution(otherVariable, this);
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
      // A type variable is a subtype of every supertype of its upper bound.
      return isSubtypeOfOrEqualTo(getUpperTypeBound(), otherType);
    }
    return false;
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
   * Creates a substitution of the given {@link ReferenceType} for the {@link TypeVariable}.
   *
   * @param variable the variable
   * @param otherType the replacement type
   * @return a substitution that replaces {@code variable} with {@code otherType}
   */
  private static Substitution getSubstitution(TypeVariable variable, ReferenceType otherType) {
    return new Substitution(variable, otherType);
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
  /*package*/ boolean canBeInstantiatedBy(ReferenceType otherType) {
    Substitution substitution;
    if (getLowerTypeBound().isVariable()) {
      substitution = getSubstitution(this, otherType);
      ParameterBound boundType = getLowerTypeBound().substitute(substitution);
      TypeVariable checkType = (TypeVariable) ((ReferenceBound) boundType).getBoundType();
      if (!checkType.canBeInstantiatedBy(otherType)) {
        return false;
      }
    } else {
      substitution = getSubstitution(this, otherType);
      if (!getLowerTypeBound().isLowerBound(otherType, substitution)) {
        return false;
      }
    }
    if (getUpperTypeBound().isVariable()) {
      substitution = getSubstitution(this, otherType);
      ParameterBound boundType = getUpperTypeBound().substitute(substitution);
      TypeVariable checkType = (TypeVariable) ((ReferenceBound) boundType).getBoundType();
      if (!checkType.canBeInstantiatedBy(otherType)) {
        return false;
      }
    } else {
      substitution = getSubstitution(this, otherType);
      if (!getUpperTypeBound().isUpperBound(otherType, substitution)) {
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
