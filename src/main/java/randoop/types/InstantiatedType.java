package randoop.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a parameterized type as a generic class instantiated with
 * concrete type arguments.
 * <p>
 * Note that {@link java.lang.reflect.ParameterizedType} is an interface that
 * can represent either a parameterized type in the sense meant here, or a
 * generic class.
 * Conversion to this type from this and other {@link java.lang.reflect.Type}
 * interfaces is handled by
 * {@link randoop.types.GeneralType#forType(java.lang.reflect.Type)}.
 */
class InstantiatedType extends ParameterizedType {

  private final List<TypeArgument> argumentList;

  /** The generic class for this type */
  private final GenericClassType instantiatedType;

  /**
   * Create a parameterized type from the generic class type.
   *
   * @param instantiatedType  the generic class type
   * @param argumentList  the list of argument types
   * @throws IllegalArgumentException if either argument is null
   */
  InstantiatedType(GenericClassType instantiatedType, List<TypeArgument> argumentList) {
    if (instantiatedType == null) {
      throw new IllegalArgumentException("instantiated type must be non-null");
    }

    this.instantiatedType = instantiatedType;
    this.argumentList = argumentList;
  }

  /**
   * {@inheritDoc}
   * Test if the given object is equal to this parameterized type.
   * Two parameterized types are equal if they have the same raw type and
   * the same type arguments.
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof InstantiatedType)) {
      return false;
    }
    InstantiatedType t = (InstantiatedType) obj;
    return instantiatedType.equals(t.instantiatedType)
            && argumentList.equals(t.argumentList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(instantiatedType, argumentList);
  }


  /**
   * {@inheritDoc}
   * Handles specific cases of supertypes of a parameterized type
   *  <code>C&lt;T<sub>1</sub>,&hellip;,T<sub>n</sub>&gt;</code>
   * instantiating the generic type
   *  <code>C&lt;F<sub>1</sub>,&hellip;,F<sub>n</sub>&gt;</code>
   * by substitution
   *  <code>&#952; =[F<sub>1</sub>/T<sub>1</sub>,&hellip;,F<sub>n</sub>]</code>
   * for which direct supertypes are:
   * <ol>
   *   <li> <code>D&lt;U<sub>1</sub>&#952;,&hellip;,U<sub>k</sub>&#952;&gt;</code>
   *        where <code>D&lt;U<sub>1</sub>,&hellip;,U<sub>k</sub>&gt;</code> is a
   *        supertype of <code>C&lt;F<sub>1</sub>,&hellip;,F<sub>n</sub>&gt;</code>.
   *   <li> <code>C&lt;S<sub>1</sub>,&hellip;,S<sub>n</sub>&gt;</code> where
   *        S<sub>i</sub> <i>contains</i> T<sub>i</sub> (JLS section 4.5.1).
   *
   *   <li> The rawtype <code>C</code>.
   *   <li> <code>Object</code> if generic form is interface with no
   *   interfaces as supertypes.
   * </ol>
   * Note that the full definition of the <i>contains</i> relation in the second
   * clause involves wildcards, which are not currently implemented, and so the
   * relation reduces to equality on the parameter bounds.
   * (Tested using {@link GenericClassType#equals(Object)}.)
   */
  @Override
  public boolean isSubtypeOf(GeneralType otherType) {

    if (super.isSubtypeOf(otherType)) {
      return true;
    }

    // rawtype is a direct supertype (see JLS section 4.10.2)
    if (otherType.isRawtype()) {
      return otherType.hasRuntimeClass(this.getRuntimeClass());
    }

    if (!otherType.isParameterized()) {
      return false;
    }

    // second clause: rawtype same and parameters S_i of otherType contains T_i of this
    if (otherType.hasRuntimeClass(this.getRuntimeClass())) {
      ParameterizedType otherParameterizedType = (ParameterizedType) otherType;
      List<TypeArgument> otherTypeArguments = otherParameterizedType.getTypeArguments();
      List<TypeArgument> thisTypeArguments = this.getTypeArguments();
      assert otherTypeArguments.size() == thisTypeArguments.size();
      int i = 0;
      while (i < thisTypeArguments.size() && otherTypeArguments.get(i).contains(thisTypeArguments.get(i))) {
        i++;
      }
      if (i == thisTypeArguments.size()) {
        return true;
      }
    }

    // first clause.
    // Extra fragile because this.substitution only applies to the type
    // variables of this.instantiatedType, which are shared by supertype objects
    // created by Class.getGenericInterfaces() and Class.getGenericSuperClass().
    // This is how GenericClassType.getMatchingSupertype(GenericClassType) works.
    // If we get GenericClassType supertype via other means, the type variables
    // will be distinct and the substitution will return null values even if the
    // variable names and type bounds are the same.

    InstantiatedType pt = (InstantiatedType) otherType;
    GenericClassType genericSuperType;
    genericSuperType = this.instantiatedType.getMatchingSupertype(pt.instantiatedType);
    if (genericSuperType == null) { // no matching supertype
      return false;
    }
    Substitution<ReferenceType> substitution = getTypeSubstitution();
    if (substitution != null) {
      GeneralType superType = genericSuperType.apply(substitution);
      if (pt.equals(superType)) {
        return true; // found type
      }

      // otherwise, non-null superType is potentially on transitive chain to type
      return superType.isSubtypeOf(otherType);
    }
    return false;
  }

  /**
   * Creates the type substitution of the type arguments of this type for the type variables of the
   * instantiated class, if the type arguments are reference types.
   * If any type argument is a wildcard, then null is returned.
   *
   * @return the type substitution of the type arguments of this class for the type variables of the instantiated type
   */
  private Substitution<ReferenceType> getTypeSubstitution() {
    List<ReferenceType> arguments = new ArrayList<>();
    for (TypeArgument arg : this.getTypeArguments()) {
      if (! arg.isWildcard()) {
        arguments.add(((ReferenceArgument)arg).getReferenceType());
      }
    }
    Substitution<ReferenceType> substitution = null;
    if (arguments.size() == this.getTypeArguments().size()) {
      substitution = Substitution.forArgs(instantiatedType.getTypeParameters(), arguments);
    }
    return substitution;
  }

  /**
   * {@inheritDoc}
   * @return true, since this is a parameterized type
   */
  @Override
  public boolean isParameterized() {
    return true;
  }

  @Override
  public InstantiatedType apply(Substitution<ReferenceType> substitution) {
    return null;
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
   * @return the rawtype of the generic type that this type instantiates
   */
  @Override
  public Class<?> getRuntimeClass() {
    return instantiatedType.getRuntimeClass();
  }

  /**
   * Returns the type arguments for this type.
   *
   * @return the list of type arguments
   */
  public List<TypeArgument> getTypeArguments() {
    return argumentList;
  }

  /**
   * Checks whether this parameterized type is an instantiation of the given
   * generic class type.
   *
   * @param genericClassType  the generic class type
   * @return true if this type is an instantiation of the generic class, false otherwise
   */
  public boolean isInstantiationOf(GenericClassType genericClassType) {
    return instantiatedType.isInstantiationOf(genericClassType);
  }

  /**
   * Constructs the superclass type for this parameterized type.
   *
   * @return the superclass type for this parameterized type
   */
  @Override
  public ClassOrInterfaceType getSuperclass(){
    ClassOrInterfaceType superclass = this.instantiatedType.getSuperclass();
    if (superclass == null) {
      return null;
    }

    if (superclass instanceof GenericClassType) {
      //replace type variables of superclass with type argument from this class
      // use values from this.argumentList
      Substitution<TypeArgument> substitution = Substitution.forArgs(instantiatedType.getTypeParameters(), argumentList);
      GenericClassType superType = (GenericClassType) superclass;
      List<TypeVariable> typeParameters = superType.getTypeParameters();
      List<TypeArgument> typeArguments = new ArrayList<>();
      for (TypeVariable var : typeParameters) {
        typeArguments.add(substitution.get(var));
      }

      return new InstantiatedType(superType, typeArguments);
    }

    return superclass;
  }
}
