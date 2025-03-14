package randoop.types;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import org.plumelib.util.CollectionsPlume;

/**
 * Represents the type of a generic class. Related to concrete {@link InstantiatedType} by
 * instantiating with a {@link Substitution}.
 */
public class GenericClassType extends ParameterizedType {

  /** The rawtype of the generic class. */
  private Class<?> rawType;

  /** The type parameters of the generic class. */
  private List<TypeVariable> parameters;

  /**
   * Creates a {@link GenericClassType} for the given raw type.
   *
   * @param rawType the {@code Class} raw type
   */
  GenericClassType(Class<?> rawType) {
    this.rawType = rawType;
    this.parameters = CollectionsPlume.mapList(TypeVariable::forType, rawType.getTypeParameters());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Checks that the rawtypes are the same. This is sufficient since the type parameters and
   * their bounds can be reconstructed from the Class object. Also, parameters can be distinct
   * depending on how this object is constructed.
   *
   * @return true if two generic classes have the same rawtype, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
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
   * Instantiates this generic class using the substitution to replace the type parameters.
   *
   * @param substitution the type substitution
   * @return a {@link ParameterizedType} instantiating this generic class by the given substitution
   */
  @Override
  public InstantiatedType substitute(Substitution substitution) {
    List<TypeArgument> argumentList =
        CollectionsPlume.mapList(
            (TypeVariable variable) ->
                TypeArgument.forType(substitution.getOrDefault(variable, variable)),
            parameters);
    return (InstantiatedType)
        substitute(substitution, new InstantiatedType(new GenericClassType(rawType), argumentList));
  }

  @Override
  public GenericClassType applyCaptureConversion() {
    return (GenericClassType) applyCaptureConversion(this);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Note that this method uses {@code Class.getInterfaces()} and does not preserve the
   * relationship between the type parameters of a class and its interfaces, and should not be used
   * when finding supertypes of types represented as {@link InstantiatedType} objects.
   */
  @Override
  public List<ClassOrInterfaceType> getInterfaces() {
    return CollectionsPlume.mapList(ClassOrInterfaceType::forClass, rawType.getInterfaces());
  }

  /**
   * Return the directly-implemented interface types for this generic class type, instantiated by
   * the given type {@link Substitution}.
   *
   * <p><i>This method is not public.</i> It is used when finding the interfaces of an {@link
   * InstantiatedType} using {@link InstantiatedType#getInterfaces()}, where it is important that
   * the relationship between type variables is preserved. The reflection method {@code
   * Class.getGenericInterfaces()} ensures the type variable objects are the same from a class to
   * its interfaces, which allows the use of the same substitution for both types.
   *
   * @param substitution the type substitution
   * @return the list of instantiated directly-implemented interface types of this type
   */
  List<ClassOrInterfaceType> getInterfaces(Substitution substitution) {
    return CollectionsPlume.mapList(
        (java.lang.reflect.Type type) ->
            ClassOrInterfaceType.forType(type).substitute(substitution),
        rawType.getGenericInterfaces());
  }

  @Override
  public GenericClassType getGenericClassType() {
    return this;
  }

  @Override
  public Class<?> getRuntimeClass() {
    return rawType;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Note that this method uses {@code Class.getSuperclass()} and does not preserve the
   * relationship between the type parameters of a class and its superclass, and should not be used
   * when finding supertypes of types represented as {@link InstantiatedType} objects.
   */
  @Override
  public ClassOrInterfaceType getSuperclass() {
    Class<?> superclass = rawType.getSuperclass();
    if (superclass != null) {
      return ClassOrInterfaceType.forClass(superclass);
    } else {
      return JavaTypes.OBJECT_TYPE;
    }
  }

  /**
   * Returns the superclass type for this generic class type instantiated by the given type {@link
   * Substitution}.
   *
   * <p><i>This method is not public.</i> It is used when finding the superclass of an {@link
   * InstantiatedType} using {@link InstantiatedType#getSuperclass()}, where it is important that
   * the relationship between type variables is preserved. The reflection method {@code
   * Class.getGenericSuperclass()} ensures the type variable objects are the same from subclass to
   * superclass, which allows the use of the same substitution for both types.
   *
   * @param substitution the type substitution
   * @return the instantiated type
   */
  ClassOrInterfaceType getSuperclass(Substitution substitution) {
    java.lang.reflect.Type superclass = this.rawType.getGenericSuperclass();
    if (superclass == null) {
      return JavaTypes.OBJECT_TYPE;
    }
    return ClassOrInterfaceType.forType(superclass).substitute(substitution);
  }

  // TODO: When would one want to call this?  Should we move getTypeArguments from
  // ParameterizedType to InstantiatedType?
  @Override
  public List<TypeArgument> getTypeArguments() {
    return CollectionsPlume.mapList(TypeArgument::forType, parameters);
  }

  /**
   * Returns the list of type parameters of this generic class.
   *
   * @return the list of type parameters of this generic class
   */
  @Override
  public List<TypeVariable> getTypeParameters() {
    List<TypeVariable> params = super.getTypeParameters();
    params.addAll(parameters);
    return params;
  }

  /**
   * Creates a type substitution using the given type arguments and applies it to this type.
   *
   * @param typeArguments the type arguments
   * @return a type which is this type parameterized by the given type arguments
   * @see #substitute(Substitution)
   */
  public InstantiatedType instantiate(ReferenceType... typeArguments) {
    if (typeArguments.length != this.getTypeParameters().size()) {
      throw new IllegalArgumentException("number of arguments and parameters must match");
    }

    Substitution substitution = new Substitution(this.getTypeParameters(), typeArguments);
    for (int i = 0; i < parameters.size(); i++) {
      if (!parameters.get(i).getUpperTypeBound().isUpperBound(typeArguments[i], substitution)) {
        throw new IllegalArgumentException(
            "type argument "
                + typeArguments[i]
                + " does not match parameter bound "
                + parameters.get(i).getUpperTypeBound());
      }
    }
    return this.substitute(substitution);
  }

  /**
   * Creates a type substitution using the given type arguments and applies it to this type.
   *
   * @param typeArguments the type arguments
   * @return the type that is this type instantiated by the given type arguments
   * @see #substitute(Substitution)
   */
  public InstantiatedType instantiate(List<ReferenceType> typeArguments) {
    if (typeArguments.size() != this.getTypeParameters().size()) {
      throw new IllegalArgumentException("number of arguments and parameters must match");
    }

    Substitution substitution = new Substitution(this.getTypeParameters(), typeArguments);
    for (int i = 0; i < parameters.size(); i++) {
      if (!parameters.get(i).getUpperTypeBound().isUpperBound(typeArguments.get(i), substitution)) {
        throw new IllegalArgumentException(
            "type argument "
                + typeArguments.get(i)
                + " does not match parameter bound "
                + parameters.get(i).getUpperTypeBound());
      }
    }
    return this.substitute(substitution);
  }

  @Override
  public boolean isAbstract() {
    return Modifier.isAbstract(Modifier.classModifiers() & rawType.getModifiers());
  }

  @Override
  public boolean isGeneric(boolean ignoreWildcards) {
    return true;
  }

  @Override
  public boolean isInterface() {
    return rawType.isInterface();
  }

  @Override
  public boolean isStatic() {
    return Modifier.isStatic(rawType.getModifiers() & Modifier.classModifiers());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Handles the specific cases of supertypes of a generic class {@code C<F1,...,Fn>} for which
   * the direct supertypes are:
   *
   * <ol>
   *   <li>the direct superclass,
   *   <li>the direct superinterfaces,
   *   <li>the type {@code Object}, and
   *   <li>the raw type {@code C}
   * </ol>
   */
  @Override
  public boolean isSubtypeOf(Type otherType) {
    if (otherType == null) {
      throw new IllegalArgumentException("type must be non-null");
    }

    if (super.isSubtypeOf(otherType)) {
      return true;
    }
    return otherType.isRawtype() && otherType.runtimeClassIs(this.getRuntimeClass());
  }

  /**
   * Returns the rawtype {@code Type} for this generic class.
   *
   * @return the rawtype for this generic class
   */
  @Override
  public NonParameterizedType getRawtype() {
    return NonParameterizedType.forClass(rawType);
  }
}
