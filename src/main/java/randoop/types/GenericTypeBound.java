package randoop.types;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import plume.UtilMDE;

/**
 * Represents a type bound in which a type variable occurs.
 * To evaluate this kind of bound, a substitution is needed to instantiate the
 * bound to a concrete type bound.
  * @see Substitution
 */
class GenericTypeBound extends ClassOrInterfaceBound {

  /** the rawtype for this generic bound */
  private final Class<?> rawType;

  /** the type parameters for this bound */
  private final Type[] parameters;

  /**
   * Creates a {@code GenericTypeBound} from the given rawtype and type parameters.
   *
   * @param rawType  the rawtype for the type bound
   * @param parameters  the type parameters for the type bound
   */
  private GenericTypeBound(Class<?> rawType, Type[] parameters) {
    this.rawType = rawType;
    this.parameters = parameters;
  }

  /**
   * {@inheritDoc}
   * @return true if argument is a {@code GenericTypeBound}, and the rawtype
   *         and parameters are identical, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GenericTypeBound)) {
      return false;
    }
    GenericTypeBound b = (GenericTypeBound) obj;
    if (!this.rawType.equals(b.rawType)) {
      return false;
    }
    if (this.parameters.length != b.parameters.length) {
      return false;
    }
    for (int i = 0; i < this.parameters.length; i++) {
      if (!this.parameters[i].equals(b.parameters[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(rawType, parameters);
  }

  /**
   * {@inheritDoc}
   * @return the name of the type bound
   */
  @Override
  public String toString() {
    return rawType.toString() + "<" + UtilMDE.join(parameters, ",") + ">";
  }

  /**
   * {@inheritDoc}
   * This generic type bound is satisfied by a concrete type if the concrete type
   * formed by applying the substitution to this generic bound is satisfied by
   * the concrete type.
   */
  @Override
  public boolean isSatisfiedBy(GeneralType argType, Substitution<ReferenceType> substitution) {
    ClassOrInterfaceTypeBound b = this.apply(substitution);

    return b.isSatisfiedBy(argType, substitution);
  }

  @Override
  public boolean isSatisfiedBy(GeneralType argType) {
    return true;
  }

  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    return false;
  }

  @Override
  public ClassOrInterfaceTypeBound apply(Substitution<ReferenceType> substitution) {
    List<TypeArgument> argumentList = new ArrayList<>();
    for (Type parameter : parameters) {
      if (!(parameter instanceof java.lang.reflect.TypeVariable)) {
        throw new IllegalArgumentException("unexpected type parameter " + parameter);
      }
      ReferenceType type = substitution.get(parameter);
      if (type == null) {
        throw new IllegalArgumentException("substitution does not instantiate parameter " + parameter);
      }
      argumentList.add(new ReferenceArgument(type));
    }
    GenericClassType classType = GenericClassType.forClass(rawType);
    InstantiatedType boundType = new InstantiatedType(classType, argumentList);
    return new ClassOrInterfaceTypeBound(boundType);
  }

  /**
   * Creates a {@link GenericTypeBound} for the given reflective type.
   *
   * @param type  the type
   * @return the bound for the given type
   */
  static GenericTypeBound fromType(Type type) {
    if (! (type instanceof java.lang.reflect.ParameterizedType)) {
      throw new IllegalArgumentException("type must be generic");
    }

    java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) type;
    Type rawType = pt.getRawType();
    assert rawType instanceof Class<?> : "rawtype must be class";
    Class<?> runtimeType = (Class<?>) rawType;
    return new GenericTypeBound(runtimeType, pt.getActualTypeArguments());
  }
}
