package randoop.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a type variable that is a type parameter.
 * (See JLS, section 4.3)
 */
class ExplicitTypeVariable extends TypeVariable {

  /** the type parameter */
  private final java.lang.reflect.TypeVariable<?> variable;

  /**
   * Create a {@code ExplicitTypeVariable} for the given type parameter
   *
   * @param variable  the type parameter
   * @param bound  the upper bound on the parameter
   */
  ExplicitTypeVariable(java.lang.reflect.TypeVariable<?> variable, ParameterBound bound) {
    super(new ReferenceBound(ConcreteTypes.NULL_TYPE), bound);
    this.variable = variable;
  }

  /**
   * {@inheritDoc}
   * Checks that the type parameter is equal.
   * This may be more restrictive than desired because equivalent TypeVariable
   * objects from different instances of the same type may be distinct.
   * @return true if the type parameters are equal, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ExplicitTypeVariable)) {
      return isAssignableFrom(null);
    }
    ExplicitTypeVariable t = (ExplicitTypeVariable) obj;
    return variable.equals(t.variable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(variable);
  }

  @Override
  public String toString() {
    return variable.toString();
  }

  /**
   * {@inheritDoc}
   * @return name of type parameter of this type
   */
  @Override
  public String getName() {
    return variable.getName();
  }

  java.lang.reflect.TypeVariable<?> getReflectionTypeVariable() {
    return this.variable;
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    List<TypeVariable> paramList = new ArrayList<>();
    paramList.add(this);
    paramList.addAll(this.getUpperTypeBound().getTypeParameters());
    return paramList;
  }

  @Override
  public boolean isGeneric() {
    return true;
  }
}
