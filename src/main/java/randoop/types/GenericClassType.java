package randoop.types;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.CollectionsPlume;

/**
 * Represents the type of a generic class. The type parameters are all type variables. Related to
 * concrete {@link InstantiatedType} by instantiating with a {@link Substitution}.
 */
public class GenericClassType extends ParameterizedType {

  /** The rawtype of the generic class. */
  private final Class<?> rawType;

  /**
   * The type parameters that the generic class declares, not including those of an enclosing class.
   * Null until first needed: computing them computes their bounds, which may refer to this class,
   * as in {@code Builder<T extends Builder<?>>}. Computing them in the constructor would recurse
   * infinitely for such a class. Use {@link #parameters()} instead of reading this field.
   */
  // Volatile so that a thread that reads a non-null value also sees the contents of the list.
  private volatile @Nullable List<TypeVariable> parameters = null;

  /**
   * Creates a {@link GenericClassType} for the given raw type.
   *
   * <p>The type parameters are computed when they are first needed, not here. Computing the bound
   * of a type parameter can create this generic class type, as for {@code class Outer<T extends
   * Outer<?>.Inner>} or {@code class Node<T extends Node<?>>}. If this constructor computed the
   * type parameters, then {@link ParameterizedType#forClass(Class)} would recurse without end:
   * computing the bound of {@code T} would construct this type again, because the cache does not
   * yet contain it, and constructing it would compute the bound of {@code T} again, because {@link
   * TypeVariable#forType(java.lang.reflect.Type)} does not yet cache {@code T}.
   *
   * @param rawType the {@code Class} raw type
   */
  /*package*/ GenericClassType(Class<?> rawType) {
    super();
    this.rawType = rawType;
  }

  /**
   * Returns the type parameters that this generic class declares, computing them if necessary.
   * Unlike {@link #getTypeParameters()}, the result omits the type parameters of enclosing classes.
   * For example, for {@code Outer<T>.Inner<U>}, the result is {@code [U]}.
   *
   * @return the type parameters that this generic class declares
   */
  private List<TypeVariable> parameters() {
    List<TypeVariable> result = parameters;
    if (result == null) {
      result = CollectionsPlume.mapList(TypeVariable::forType, rawType.getTypeParameters());
      parameters = result;
    }
    return result;
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
  public boolean equals(@Nullable Object obj) {
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
    return Objects.hashCode(rawType);
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
            parameters());
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
   * Returns the directly-implemented interface types for this generic class type, instantiated by
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
  /*package*/ List<ClassOrInterfaceType> getInterfaces(Substitution substitution) {
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
    return superclass == null ? null : ClassOrInterfaceType.forClass(superclass);
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
  /*package*/ ClassOrInterfaceType getSuperclass(Substitution substitution) {
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
    return CollectionsPlume.mapList(TypeArgument::forType, parameters());
  }

  /**
   * Returns the type parameters that this generic class declares. Unlike {@link
   * #getTypeParameters()}, the result omits the type parameters of enclosing classes. For example,
   * for {@code Outer<T>.Inner<U>}, the result is {@code [U]}. The result corresponds to the type
   * arguments of an instantiation of this class.
   *
   * @return the type parameters that this generic class declares
   */
  /*package*/ List<TypeVariable> getDeclaredTypeParameters() {
    return parameters();
  }

  /**
   * Returns the list of type parameters of this generic class.
   *
   * @return the list of type parameters of this generic class
   */
  @Override
  public List<TypeVariable> getTypeParameters() {
    return CollectionsPlume.concatenate(super.getTypeParameters(), parameters());
  }

  /**
   * Creates a type substitution using the given type arguments and applies it to this type.
   *
   * @param typeArguments the type arguments, one per element of {@link #getTypeParameters()} and in
   *     the same order, so the arguments for the type parameters of enclosing classes come first.
   *     For example, for {@code Outer<T>.Inner<U>}, the arguments {@code String, Integer} yield
   *     {@code Outer<String>.Inner<Integer>}.
   * @return a type which is this type parameterized by the given type arguments
   * @throws IllegalArgumentException if the number of type arguments differs from the number of
   *     type parameters, or if a type argument is not within the bound of its type parameter
   * @see #substitute(Substitution)
   * @see #areWithinBounds(List)
   */
  public InstantiatedType instantiate(ReferenceType... typeArguments) {
    return instantiate(Arrays.asList(typeArguments));
  }

  /**
   * Creates a type substitution using the given type arguments and applies it to this type.
   *
   * @param typeArguments the type arguments, one per element of {@link #getTypeParameters()} and in
   *     the same order, so the arguments for the type parameters of enclosing classes come first.
   *     For example, for {@code Outer<T>.Inner<U>}, the arguments {@code [String, Integer]} yield
   *     {@code Outer<String>.Inner<Integer>}.
   * @return the type that is this type instantiated by the given type arguments
   * @throws IllegalArgumentException if the number of type arguments differs from the number of
   *     type parameters, or if a type argument is not within the bound of its type parameter
   * @see #substitute(Substitution)
   * @see #areWithinBounds(List)
   */
  public InstantiatedType instantiate(List<ReferenceType> typeArguments) {
    List<TypeVariable> typeParameters = this.getTypeParameters();
    Substitution substitution = substitutionFor(typeParameters, typeArguments);
    int violation = indexOfBoundViolation(typeParameters, typeArguments, substitution);
    if (violation != -1) {
      throw new IllegalArgumentException(
          "type argument "
              + typeArguments.get(violation)
              + " does not match parameter bound "
              + typeParameters.get(violation).getUpperTypeBound());
    }
    return this.substitute(substitution);
  }

  /**
   * Returns true if each of the given type arguments is within the bound of its type parameter, so
   * that {@link #instantiate(List)} accepts them.
   *
   * @param typeArguments the type arguments, one per element of {@link #getTypeParameters()} and in
   *     the same order, as for {@link #instantiate(List)}
   * @return true if each type argument is within the bound of its type parameter
   * @throws IllegalArgumentException if the number of type arguments differs from the number of
   *     type parameters
   */
  public boolean areWithinBounds(List<ReferenceType> typeArguments) {
    List<TypeVariable> typeParameters = this.getTypeParameters();
    Substitution substitution = substitutionFor(typeParameters, typeArguments);
    return indexOfBoundViolation(typeParameters, typeArguments, substitution) == -1;
  }

  /**
   * Returns the substitution of the given type arguments for the given type parameters.
   *
   * @param typeParameters a list of type parameters
   * @param typeArguments the type arguments, corresponding to {@code typeParameters}
   * @return the substitution of {@code typeArguments} for {@code typeParameters}
   * @throws IllegalArgumentException if the lists have different lengths
   */
  private static Substitution substitutionFor(
      List<TypeVariable> typeParameters, List<ReferenceType> typeArguments) {
    if (typeArguments.size() != typeParameters.size()) {
      throw new IllegalArgumentException("number of arguments and parameters must match");
    }
    return new Substitution(typeParameters, typeArguments);
  }

  /**
   * Returns the index of the first type argument that is not within the upper bound of its type
   * parameter, or -1 if every type argument is within its bound.
   *
   * @param typeParameters the type parameters of a generic class, as returned by {@link
   *     #getTypeParameters()}
   * @param typeArguments the type arguments, corresponding to {@code typeParameters}
   * @param substitution the substitution of {@code typeArguments} for {@code typeParameters}
   * @return the index of the first type argument that is not within its bound, or -1 if none
   */
  private static int indexOfBoundViolation(
      List<TypeVariable> typeParameters,
      List<ReferenceType> typeArguments,
      Substitution substitution) {
    // For a member class, the type parameters of the enclosing types precede those of this class,
    // so each type argument is checked against the type parameter at the same index.
    for (int i = 0; i < typeParameters.size(); i++) {
      ParameterBound bound = typeParameters.get(i).getUpperTypeBound();
      if (!bound.isUpperBound(typeArguments.get(i), substitution)) {
        return i;
      }
    }
    return -1;
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
  public boolean isSubtypeOfOrEqualTo(Type otherType) {
    if (otherType == null) {
      throw new IllegalArgumentException("type must be non-null");
    }

    if (super.isSubtypeOfOrEqualTo(otherType)) {
      return true;
    }
    if (otherType.runtimeClassIs(this.getRuntimeClass())) {
      if (otherType.isRawtype()
          || (otherType instanceof ClassOrInterfaceType
              && ((ClassOrInterfaceType) otherType).isGenericForm())) {
        return true;
      }
      if (otherType instanceof InstantiatedType) {
        boolean allWildcards = true;
        for (TypeArgument argument : ((InstantiatedType) otherType).getTypeArguments()) {
          if (!(argument instanceof WildcardArgument)) {
            allWildcards = false;
            break;
          }
        }
        if (allWildcards) {
          return true;
        }
      }
    }
    return false;
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
