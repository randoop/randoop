package randoop.types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a type variable that is a type parameter.
 * (See JLS, section 4.3)
 */
public class TypeVariable extends AbstractTypeVariable {

  /** the type parameter */
  private final java.lang.reflect.TypeVariable<?> variable;

  /** the upper typeBound on the type parameter */
  private final ParameterBound typeBound;

  /**
   * Create a {@code TypeVariable} for the given type parameter
   *
   * @param variable  the type parameter
   * @param bound  the upper boundon the parameter
   */
  private TypeVariable(java.lang.reflect.TypeVariable<?> variable, ParameterBound bound) {
    this.variable = variable;
    this.typeBound = bound;
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
    if (!(obj instanceof TypeVariable)) {
      return isAssignableFrom(null);
    }
    TypeVariable t = (TypeVariable) obj;
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

  @Override
  public boolean isGeneric() {
    return true;
  }

  /**
   * Return the upper bound type for this type variable.
   *
   * @return the upper bound type for this variable
   */
  public ParameterBound getTypeBound() {
    return typeBound;
  }

  /**
   * Creates a {@code TypeVariable} object for a given {@code java.lang.reflect.Type}
   * reference, which must be a {@code java.lang.reflect.TypeVariable}.
   *
   * @param type  the type reference
   * @return the {@code TypeVariable} for the given type
   */
  public static TypeVariable forType(Type type) {
    if (! (type instanceof java.lang.reflect.TypeVariable<?>)) {
      throw new IllegalArgumentException("type must be a type variable, got " + type);
    }
    java.lang.reflect.TypeVariable<?> v = (java.lang.reflect.TypeVariable)type;
    return new TypeVariable(v, ParameterBound.forTypes(v.getBounds()));
  }

  @Override
  public List<AbstractTypeVariable> getTypeParameters() {
    List<AbstractTypeVariable> paramList = new ArrayList<>();
    paramList.add(this);
    return paramList;
  }

  /**
   * Return the underlying reflection object.
   *
   * @return the reflection object.
   */
  Type getReflectionTypeVariable() {
    return variable;
  }
}
