package randoop.types;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Represents a type parameter bound that is a type variable.
 */
class VariableTypeBound extends ParameterBound {

  /** The {@link TypeVariable} that is this bound */
  private final TypeVariable typeVariable;

  /**
   * Constructs a bound for the given {@link TypeVariable}.
   *
   * @param typeVariable  the type variable
   */
  VariableTypeBound(TypeVariable typeVariable) {
    this.typeVariable = typeVariable;
  }

  /**
   * {@inheritDoc}
   * @return true if the {@link TypeVariable} objects are equal, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof VariableTypeBound)) {
      return false;
    }
    VariableTypeBound var = (VariableTypeBound)obj;
    return this.typeVariable.equals(var.typeVariable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeVariable);
  }

  @Override
  public String toString() {
    return typeVariable.toString();
  }

  /**
   * {@inheritDoc}
   * A type variable parameter bound is satisfied by a type if the instantiation of the bound by the
   * given substitution is an upper bound of the type.
   *
   * @return
   */
  @Override
  public boolean isSatisfiedBy(GeneralType argType, Substitution<ReferenceType> substitution) {
    ParameterBound bound = apply(substitution);
    if (bound != null) {
      return bound.isSatisfiedBy(argType, substitution);
    } else {
      return isSatisfiedBy(argType);
    }
  }

  /**
   * {@inheritDoc}
   * The relaxed evaluation of a type variable bound is to check whether the bound of the variable
   * is satisfied.
   *
   * @return true if the bound of this {@link TypeVariable} is satisfied, false otherwise
   */
  @Override
  public boolean isSatisfiedBy(GeneralType argType) {
    ParameterBound bound = typeVariable.getTypeBound();
    return bound.isSatisfiedBy(argType);
  }

  /**
   * Applies a substitution to this type variable parameter, and returns a new parameter bound for
   * the substituted type.

   * @param substitution  the substitution
   * @return a {@link ParameterBound} for the type substituted for this variable, or null if there is none
   */
  public ParameterBound apply(Substitution<ReferenceType> substitution) {
    ReferenceType type = typeVariable.apply(substitution);
    if (type == null) {
      return null;
    }
    return ParameterBound.forType(type);
  }

  /**
   * Creates a type bound that is a variable from the given type.
   *
   * @param type  the type for the constructed bound
   * @return the type bound
   */
  public static VariableTypeBound forType(Type type) {
    return new VariableTypeBound(TypeVariable.forType(type));
  }

  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    return typeVariable.isSubtypeOf(otherType);
  }
}
