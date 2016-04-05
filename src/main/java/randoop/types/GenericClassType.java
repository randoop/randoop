package randoop.types;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import plume.UtilMDE;

/**
 * Represents the type of a generic class as can occur in a class declaration,
 * formal parameter or return type.
 * Related to concrete {@link ParameterizedType} by instantiating with a
 * {@link Substitution}.
 */
public class GenericClassType extends GenericType {

  /** The rawtype of the generic class. */
  private Class<?> rawType;

  /** the type parameters of the generic class */
  private List<TypeVariable<?>> parameters;

  /** The (upper) bounds on the type parameters of the generic class */
  private List<TypeBound> bounds;

  /**
   * Create a {@code GenericClassType} object for the given rawtype.
   * Uses the {@link java.lang.reflect.GenericDeclaration} methods to access
   * the type parameters for the generic class.
   * <p>
   * This is the standard constructor to use when only a {@code Class} object
   * is available. It should not be used when creating instances for supertypes
   * where type variables are needed to be the same, such as during testing
   * the subtype relation.
   * @see ParameterizedType#isSubtypeOf(ConcreteType)
   * @see #getMatchingSupertype(GenericClassType)
   * @see randoop.types.GeneralType#forType(Type)
   *
   * @param rawType  the raw type for the generic class
   * @throws IllegalArgumentException if the class is not generic
   */
  public GenericClassType(Class<?> rawType) {
    if (rawType.getTypeParameters().length == 0) {
      throw new IllegalArgumentException("class must be a generic type");
    }

    this.rawType = rawType;
    this.bounds = new ArrayList<>();
    this.parameters = new ArrayList<>();

    for (TypeVariable<?> v : rawType.getTypeParameters()) {
      this.parameters.add(v);
      this.bounds.add(TypeBound.fromTypes(v.getBounds()));
    }
  }

  /**
   * Create a {@code GenericClassType} for the given rawtype with the parameters,
   * and parameter type bounds.
   * <p>
   * This constructor is intended to mainly be used by
   * {@link randoop.types.GeneralType#forType(Type)} where the full set of arguments is
   * collected before creating the type object.
   *
   * @param rawType  the rawtype for the generic class
   * @param parameters  the type parameters for the generic class
   * @param bounds  the bounds on the type parameters
   */
  public GenericClassType(
      Class<?> rawType, List<TypeVariable<?>> parameters, List<TypeBound> bounds) {
    if (rawType.getTypeParameters().length != parameters.size()) {
      throw new IllegalArgumentException("number of parameters should be equal");
    }
    if (parameters.size() != bounds.size()) {
      throw new IllegalArgumentException("number of parameters and bounds should be same");
    }

    this.rawType = rawType;
    this.parameters = parameters;
    this.bounds = bounds;
  }

  /**
   * {@inheritDoc}
   * Checks that the rawtypes are the same. This is sufficient since the
   * type parameters and their bounds can be reconstructed from the Class object.
   *
   * @return true if two generic classes have the same rawtype, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GenericClassType)) {
      return false;
    }
    GenericClassType t = (GenericClassType) obj;
    return this.rawType.equals(t.rawType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rawType);
  }

  /**
   * {@inheritDoc}
   * @return the name of this type
   */
  @Override
  public String toString() {
    return this.getName();
  }

  /**
   * {@inheritDoc}
   * @return the fully qualified name of this type with type parameters
   */
  @Override
  public String getName() {
    return rawType.getCanonicalName() + "<" + UtilMDE.join(parameters, ",") + ">";
  }

  @Override
  public boolean isInterface() {
    return rawType.isInterface();
  }

  /**
   * {@inheritDoc}
   * @return a {@link ParameterizedType} instantiating this generic class with
   * the given type arguments
   * @throws IllegalArgumentException if a type argument does not match the
   * corresponding type parameter bounds
   */
  @Override
  public ConcreteType instantiate(ConcreteType... typeArguments) {
    if (typeArguments == null) {
      throw new IllegalArgumentException("type arguments cannot be null");
    }

    Substitution substitution = Substitution.forArgs(parameters, typeArguments);
    for (int i = 0; i < bounds.size(); i++) {
      if (!bounds.get(i).isSatisfiedBy(typeArguments[i], substitution)) {
        throw new IllegalArgumentException("type argument does not match parameter bound");
      }
    }
    return new ParameterizedType(this, substitution);
  }

  /**
   * Instantiates this generic class using the substitution to replace the type
   * parameters.
   *
   * @param substitution  the type substitution
   * @return a {@link ParameterizedType} instantiating this generic class by the
   * given substitution
   */
  @Override
  public ConcreteType apply(Substitution substitution) {
    if (substitution == null) {
      throw new IllegalArgumentException("substitution must be non-null");
    }

    for (int i = 0; i < bounds.size(); i++) {
      if (!bounds.get(i).isSatisfiedBy(substitution.get(parameters.get(i)), substitution)) {
        throw new IllegalArgumentException(
            "type argument from substitution does not match parameter bound");
      }
    }
    return new ParameterizedType(this, substitution);
  }

  /**
   * {@inheritDoc}
   * @return the type parameter bounds for this generic class
   */
  @Override
  public List<TypeBound> getBounds() {
    return this.bounds;
  }

  /**
   * {@inheritDoc}
   * @return the rawtype of this generic class
   */
  @Override
  public Class<?> getRuntimeClass() {
    return rawType;
  }

  /**
   * Returns the list of type parameters of this generic class
   *
   * @return the list of type parameters of this generic class
   */
  public List<TypeVariable<?>> getTypeParameters() {
    return parameters;
  }

  /**
   * Returns a direct supertype of this type that either matches the given type,
   * or has a rawtype assignable to (and so could be subtype of) the given type.
   * Returns null if no such supertype is found.
   * Construction guarantees that substitution on this generic class type will
   * work on returned generic supertype as required by
   * {@link ParameterizedType#isSubtypeOf(ConcreteType)}.
   *
   * @param type  the potential supertype
   * @return a supertype of this type that matches the given type or
   * has an assignable rawtype; or null otherwise
   * @throws IllegalArgumentException if type is null
   */
  GenericClassType getMatchingSupertype(GenericClassType type) {
    if (type == null) {
      throw new IllegalArgumentException("type may not be null");
    }

    // minimally, underlying Class should be assignable
    Class<?> otherRawType = type.getRuntimeClass();
    if (!otherRawType.isAssignableFrom(this.rawType)) {
      return null;
    }

    // if other type is an interface, check interfaces first
    if (otherRawType.isInterface()) {
      Type[] interfaces = this.rawType.getGenericInterfaces();
      for (Type t : interfaces) {
        GenericType genericType = GenericType.forType(t);
        if (type.equals(genericType)) { // found the type
          return (GenericClassType) genericType;
        }
      }
    }

    // otherwise, check superclass
    Type superclass = this.rawType.getGenericSuperclass();
    if (superclass != null) {
      GeneralType superType = GeneralType.forType(superclass);
      if (type.equals(superType)) { // found the type
        return (GenericClassType) superType;
      }
      if (superType.isObject()) {
        return null;
      }
      if (otherRawType.isAssignableFrom(superType.getRuntimeClass())) {
        return (GenericClassType) superType;
      }
    }

    return null;
  }

  GeneralType getSuperclass() {
    Type superclass = this.rawType.getGenericSuperclass();
    if (superclass == null) {
      return null;
    }

    return GeneralType.forType(superclass);
  }
}
