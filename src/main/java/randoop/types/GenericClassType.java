package randoop.types;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import plume.UtilMDE;

/**
 * Represents the type of a generic class.
 * Related to concrete {@link InstantiatedType} by instantiating with a
 * {@link Substitution}.
 */
public class GenericClassType extends ParameterizedType {

  /** The rawtype of the generic class. */
  private Class<?> rawType;

  /** the type parameters of the generic class */
  private List<TypeVariable> parameters;

  GenericClassType(Class<?> rawType) {
    this.rawType = rawType;
    this.parameters = new ArrayList<>();

    for (java.lang.reflect.TypeVariable<?> v : rawType.getTypeParameters()) {
      TypeVariable variable = TypeVariable.forType(v);
      this.parameters.add(variable);
    }
  }

  /**
   * Create a {@code GenericClassType} for the given rawtype with the parameters,
   * and parameter type bounds.
   * <p>
   * This constructor is used for constructing supertypes for of underlying class of
   * {@link InstantiatedType} objects in order to preserve relationship of type parameters
   * for application of substitution build from subclass.
   *
   * @param rawType  the rawtype for the generic class
   * @param parameters  the type parameters for the generic class
   */
  private GenericClassType(Class<?> rawType, List<TypeVariable> parameters) {
    if (rawType.getTypeParameters().length != parameters.size()) {
      throw new IllegalArgumentException("number of parameters should be equal");
    }

    this.rawType = rawType;
    this.parameters = parameters;
  }

  /**
   * {@inheritDoc}
   * Checks that the rawtypes are the same. This is sufficient since the
   * type parameters and their bounds can be reconstructed from the Class object.
   * Also, parameters can be distinct depending on how this object is constructed.
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
  public GenericClassType getGenericClassType() {
    return this;
  }

  /**
   * {@inheritDoc}
   * @return list of {@link ReferenceArgument} for each parameter
   */
  @Override
  public List<TypeArgument> getTypeArguments() {
    List<TypeArgument> argumentList = new ArrayList<>();
    for (AbstractTypeVariable v : parameters) {
      argumentList.add(new ReferenceArgument(v));
    }
    return argumentList;
  }

  @Override
  public boolean isAbstract() {
    return Modifier.isAbstract(Modifier.classModifiers() & rawType.getModifiers());
  }

  @Override
  public boolean isMemberClass() {
    return rawType.isMemberClass();
  }

  @Override
  public boolean isInstantiationOf(GenericClassType genericClassType) {
    return this.equals(genericClassType);
  }

  @Override
  public boolean isStatic() {
    return Modifier.isStatic(rawType.getModifiers() & Modifier.classModifiers());
  }

  @Override
  public boolean isInterface() {
    return rawType.isInterface();
  }

  @Override
  public boolean isGeneric() {
    return true;
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
  public InstantiatedType apply(Substitution<ReferenceType> substitution) {
    if (substitution == null) {
      throw new IllegalArgumentException("substitution must be non-null");
    }
    List<TypeArgument> argumentList = new ArrayList<>();
    for (AbstractTypeVariable variable : parameters) {
      ReferenceType referenceType = substitution.get(variable);
      if (referenceType == null) {
        throw new IllegalArgumentException("substitution has no value for variable " + variable.getName() + " (" + variable.hashCode() + ")");
      }
      argumentList.add(new ReferenceArgument(referenceType));
    }
    return new InstantiatedType(new GenericClassType(rawType), argumentList);
  }

  /**
   * Creates a type substitution using the given type arguments and applies it to this type.
   * @see #apply(Substitution)
   *
   * @param typeArguments  the type arguments
   * @return a type which is this type parameterized by the given type arguments
   */
  public InstantiatedType instantiate(ReferenceType... typeArguments) {
    if (typeArguments.length != this.getTypeParameters().size()) {
      throw new IllegalArgumentException("number of arguments and parameters must match");
    }

    Substitution<ReferenceType> substitution = Substitution.forArgs(this.getTypeParameters(), typeArguments);
    for (int i = 0; i < parameters.size(); i++) {
      if (!parameters.get(i).getTypeBound().isSatisfiedBy(typeArguments[i], substitution)) {
        throw new IllegalArgumentException("type argument does not match parameter bound");
      }
    }
    return this.apply(substitution);
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
  @Override
  public List<AbstractTypeVariable> getTypeParameters() {
    return new ArrayList<AbstractTypeVariable>(parameters);
  }

  /**
   * Returns the list of type parameters of this generic class as {@link TypeVariable} objects.
   *
   * @return the list of type parameters of this generic class
   */
  public List<TypeVariable> getFormalTypeParameters() {
    return parameters;
  }

  /**
   * {@inheritDoc}
   * Handles the specific cases of supertypes of a generic class
   * <code>C&lt;F<sub>1</sub>,...,F<sub>n</sub>&gt;</code>
   * for which the direct supertypes are:
   * <ol>
   *   <li>the direct superclass,</li>
   *   <li>the direct superinterfaces,</li>
   *   <li>the type <code>Object</code>, and</li>
   *   <li>the raw type <code>C</code></li>
   * </ol>
   */
  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    if (otherType == null) {
      throw new IllegalArgumentException("type must be non-null");
    }

    if (super.isSubtypeOf(otherType)) {
      return true;
    }

    return otherType.isRawtype()
            && otherType.hasRuntimeClass(this.getRuntimeClass());
  }

  // TODO make similar method that takes a substitution so that can be called from InstantiatedType.getSuperclass
  // TODO make this metod return actual superclass
  @Override
  public ClassOrInterfaceType getSuperclass() {
    Type superclass = this.rawType.getGenericSuperclass();
    if (superclass == null) {
      return null;
    }

    return ClassOrInterfaceType.forType(superclass);
  }

  // TODO make similar method that takes a substitution so that can be called from InstantiatedType.getInterfaces
  // TODO make this method return actual interfaces
  @Override
  public List<ClassOrInterfaceType> getInterfaces() {
    List<ClassOrInterfaceType> interfaces = new ArrayList<>();
    for (Type type : rawType.getGenericInterfaces()) {
      interfaces.add(ClassOrInterfaceType.forType(type));
    }
    return interfaces;
  }

}
